package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgInviteDTO
import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import com.yoku.guildmaster.entity.organisation.*
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvitationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationInviteRepository
import com.yoku.guildmaster.service.cached.CachedOrganisationService
import com.yoku.guildmaster.service.external.UserService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class InvitationService(
    private val organisationInviteRepository: OrganisationInviteRepository,
    private val cachedOrganisationService: CachedOrganisationService,
    private val positionMemberService: PositionMemberService,
    private val memberService: MemberService,
    private val permissionService: PermissionService,
    private val userService: UserService
) {

    /**
     * Creates an invitation for a user to join an organisation
     *
     * @param organisationId The organisation ID to create the invitation for
     * @param email The email of the user to send the invitation to
     *
     * @throws OrganisationNotFoundException If the organisation is not found
     */
    @Transactional
    @Throws(OrganisationNotFoundException::class)
    fun createInvitation(organisationId: UUID, email: String, invitationCreatorId: UUID, user: UserPartialDTO?): OrgInviteDTO{
        if(user !== null && user.email != email){
            throw InvalidArgumentException("Email does not match the provided user")
        }

        // Validate organisation existence
        val organisation: Organisation = cachedOrganisationService.findOrganisationByIdOrThrow(organisationId)

        // Validate the invitation creator is a member of the organisation, and has necessary permissions to extend invitations
        val creatorPosition: OrgPositionDTO = positionMemberService.getUserPositionWithPermissions(organisationId, invitationCreatorId)
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

        val invite = OrganisationInvite(
            organisation = organisation,
            userId = user?.id,
            email = email,
            token = generateInviteCode()
        )

        // Todo: Contact Email Service with invite code which will sent to the provided email address
        // Todo: Message should also contact notification service to send an in-app notificaiton with the associated invite
        // Todo: Setup Kafka...
        return organisationInviteRepository.save(invite).toDTO(user)
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
    fun handleInvitationAccept(token: String, userEmail: String): OrgMemberDTO{
        // Validate Invitation
        val invite: OrganisationInvite = findInvitationThroughTokenOrThrow(token)

        // Fetch User Profile
        val user: UserPartialDTO = userService.fetchUserProfileFromEmail(userEmail) ?: throw InvalidArgumentException("User not found")

        validateInviteOwnership(invite, userEmail)

        // Consume Invite and Add member to organisation
        invite.inviteStatus = OrganisationInvite.InviteStatus.ACCEPTED
        organisationInviteRepository.save(invite)
        val member: OrganisationMember = memberService.addMemberToOrganisation(invite, user)
        return member.toDTO(user)
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
    fun getOrganisationInvites(organisationId: UUID, inviteStatus: OrganisationInvite.InviteStatus?): List<OrgInviteDTO>{

        val organisationInvites = if(inviteStatus == null){
            organisationInviteRepository.findByOrganisationId(organisationId)
        } else {
            organisationInviteRepository.findByOrganisationIdAndStatus(organisationId, inviteStatus)
        }

        // Fetch User Profiles for every associated invite
        val invitationUsers: Map<UUID, UserPartialDTO?> = userService.fetchBatchUsersByIds(organisationInvites.mapNotNull { it.userId })

        return organisationInvites.map { it.toDTO(invitationUsers[it.userId]) }
    }

    /**
     * Fetches all invitations for a specific user
     * Won't bother fetching user profiles as this is a user specific request
     *
     * @param userId The user ID to fetch all invitations for
     * @return List of OrganisationInvite objects
     */
    fun getUserInvites(userId: UUID, status: OrganisationInvite.InviteStatus?): List<OrgInviteDTO>{
        if(status == null){
            return organisationInviteRepository.findByUserId(userId).map { it.toDTO(null) }
        }

        return organisationInviteRepository.findByUserIdAndStatus(userId, status).map { it.toDTO(null) }

    }

    fun revokeInvitation(organisationId: UUID, email: String){
        // Validate there is an existing active invite for this user
        val invite: OrganisationInvite = organisationInviteRepository.findByOrganisationAndEmailAndInviteStatus(
            organisation = cachedOrganisationService.findOrganisationByIdOrThrow(organisationId),
            email = email,
            inviteStatus = OrganisationInvite.InviteStatus.PENDING
        ).orElseThrow{ InvalidArgumentException("No active invitation found for this user") }

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

}