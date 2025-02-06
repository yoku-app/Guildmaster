package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.lookups.Permission
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvitationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationInviteRepository
import jakarta.transaction.Transactional
import okhttp3.Request
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class InvitationService(
    private val organisationInviteRepository: OrganisationInviteRepository,
    private val organisationService: OrganisationService,
    private val httpService: HttpService,
    private val positionMemberService: PositionMemberService,
    private val memberService: MemberService,
    private val permissionService: PermissionService
) {

    /**
     * Creates an invitation for a user to join an organisation
     *
     * @param organisationId The organisation ID to create the invitation for
     * @param email The email of the user to send the invitation to
     * @param userId The user ID of the user to send the invitation to
     *
     * @throws OrganisationNotFoundException If the organisation is not found
     */
    @Transactional
    @Throws(OrganisationNotFoundException::class)
    fun createInvitation(organisationId: UUID, email: String, invitationCreatorId: UUID, userId: UUID?): OrganisationInvite{
        // Validate organisation existence
        val organisation: Organisation = organisationService.getOrganisationByID(organisationId)

        // Validate the invitation creator is a member of the organisation, and has necessary permissions to extend invitations
        val creatorPosition: OrganisationPosition = positionMemberService.getUserPositionWithPermissions(organisationId, invitationCreatorId)
        if(!permissionService.userHasPermission(creatorPosition, Permission.MEMBER_INVITE)){
            throw InvalidArgumentException("User does not have permission to invite users")
        }

        // Validate that there is not already an active invitation for this user
        val existingInvite: OrganisationInvite? =
            organisationInviteRepository.findByOrganisationAndEmailAndInviteStatus(
                organisation = organisation,
                email = email,
                inviteStatus = OrganisationInvite.InviteStatus.PENDING
                ).orElse(null)

        if(existingInvite != null){
            throw InvalidArgumentException("An active invitation already exists for this user \n" +
                    "A user may only have one active invite for any given organisation at a time")
        }

        // Fetch User Profile from email provided from Colovia Core Data service
        val user: UserProfile? = fetchUserProfileFromEmail(email)

        val inviteCode: String = generateInviteCode()
        val invite = OrganisationInvite(
            organisation = organisation,
            user = user,
            email = email,
            token = inviteCode
        )

        // Todo: Contact Email Service with invite code which will sent to the provided email address
        // Todo: Message should also contact notification service to send an in-app notificaiton with the associated invite
        // Todo: Setup Kafka...

        return organisationInviteRepository.save(invite);
    }

    /**
     * Handles the acceptance of an invitation and will add the user to the target organisation
     *
     * @param token The token associated with the invitation
     * @param userEmail The email of the user accepting the invitation
     *
     * @throws InvitationNotFoundException If the invitation is not found
     * @throws InvalidArgumentException If the invitation is no longer valid or the email does not match the invitation
     *
     * @return OrganisationMember object representing the user that has been added to the organisation
     */
    @Transactional
    @Throws(InvitationNotFoundException::class, InvalidArgumentException::class)
    fun handleInvitationAccept(token: String, userEmail: String): OrganisationMember{
        // Validate Invitation
        val invite: OrganisationInvite = findInvitationThroughTokenOrThrow(token)

        // Fetch User Profile
        val user: UserProfile = fetchUserProfileFromEmail(userEmail) ?: throw InvalidArgumentException("User not found")

        validateInviteOwnership(invite, userEmail)

        // Consume Invite and Add member to organisation
        invite.inviteStatus = OrganisationInvite.InviteStatus.ACCEPTED
        organisationInviteRepository.save(invite)
        return memberService.addMemberToOrganisation(invite, user)
    }

    /**
     * Handles the rejection of an invitation
     *
     * @param token The token associated with the invitation
     * @param userEmail The email of the user rejecting the invitation
     *
     * @throws InvalidArgumentException If the invitation is no longer valid or the email does not match the invitation
     * @throws InvitationNotFoundException If the invitation is not found
     */
    @Transactional
    @Throws(InvalidArgumentException::class, InvitationNotFoundException::class)
    fun handleInvitationReject(token: String, userEmail: String){
        val invite: OrganisationInvite = findInvitationThroughTokenOrThrow(token);
        validateInviteOwnership(invite, userEmail)

        // Reject the invitation, no further action required
        invite.inviteStatus = OrganisationInvite.InviteStatus.REJECTED
        organisationInviteRepository.save(invite)
    }

    /**
     * Fetches all invitations for a specific organisation
     *
     * @param organisationId The organisation ID to fetch all invitations for
     * @return List of OrganisationInvite objects
     */
    fun getOrganisationInvites(organisationId: UUID, inviteStatus: OrganisationInvite.InviteStatus?): List<OrganisationInvite>{
        if(inviteStatus == null){
            return organisationInviteRepository.findByOrganisationId(organisationId)
        }

        return organisationInviteRepository.findByOrganisationIdAndStatus(organisationId, inviteStatus)
    }

    /**
     * Fetches all invitations for a specific user
     *
     * @param userId The user ID to fetch all invitations for
     * @return List of OrganisationInvite objects
     */
    fun getUserInvites(userId: UUID, status: OrganisationInvite.InviteStatus?): List<OrganisationInvite>{
        if(status == null){
            return organisationInviteRepository.findByUserId(userId)
        }

        return organisationInviteRepository.findByUserIdAndStatus(userId, status)
    }

    fun revokeInvitation(organisationId: UUID, email: String){
        // Validate there is an existing active invite for this user
        val invite: OrganisationInvite? = organisationInviteRepository.findByOrganisationAndEmailAndInviteStatus(
            organisation = organisationService.getOrganisationByID(organisationId),
            email = email,
            inviteStatus = OrganisationInvite.InviteStatus.PENDING
        ).orElse(null)

        if(invite == null){
            throw InvalidArgumentException("No active invitation found for this user")
        }

        // Revoke the invitation
        organisationInviteRepository.delete(invite)
    }

    /**
     * An automated job that will update the status of invitations that have expired
     *
     * This will query the database for any pending invites, and if the expiration date has transpired
     * the status will be updated to EXPIRED, rendering the invite unusable
     */
    fun handleInvitationExpiration(){

    }

    /**
     * Generates a random 12 character invite code
     */
    private fun generateInviteCode(): String{
        return UUID.randomUUID().toString().substring(0, 12)
    }

    /**
     * Helper function to determine the following
     *  - The invitation is still valid (ie. Awaiting Action and has not expired)
     *  - The invitation belongs to the current user being processed
     *
     *  @param invite The invitation object to validate
     *  @param userEmail The email of the user to validate against the invitation
     *
     *  @throws InvalidArgumentException If the invitation is no longer valid or the email does not match the invitation
     */
    private fun validateInviteOwnership(invite: OrganisationInvite, userEmail: String){
        // Ensure Invite is still valid
        if(!invite.isInvitationValid()){
            throw InvalidArgumentException("Invitation is no longer valid")
        }

        if(userEmail != invite.email){
            throw InvalidArgumentException("Email does not match the invitation")
        }
    }

    private fun findInvitationThroughTokenOrThrow(token: String): OrganisationInvite {
        return organisationInviteRepository.findByToken(token).orElseThrow { InvitationNotFoundException("Invitation not found") }
    }

    /**
     * Communicating with Colovia to fetch a user's profile based on the provided email, handling instances where the profile should not exist
     * @param email The email of the user to fetch the profile for
     *
     * @return UserProfile object if the user exists, null if the user does not exist
     */
    private fun fetchUserProfileFromEmail(email: String): UserProfile? {
        try{
            val request: Request.Builder = httpService.generateInternalServiceConnection(
                target = HttpService.TargetController.COLOVIA,
                endpoint = "user/email/$email"
            )
            return httpService.get(request, UserProfile::class.java)
        } catch(e: Exception){
            return null
        }
    }
}