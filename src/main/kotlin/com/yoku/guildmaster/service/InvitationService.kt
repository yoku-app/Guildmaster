package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvitationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationInviteRepository
import okhttp3.Request
import org.springframework.kafka.security.jaas.KafkaJaasLoginModuleInitializer
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class InvitationService(
    private val organisationInviteRepository: OrganisationInviteRepository,
    private val organisationService: OrganisationService,
    private val httpService: HttpService,
    private val organisationMemberService: MemberService,
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
    @Throws(OrganisationNotFoundException::class)
    fun createInvitation(organisationId: UUID, email: String, userId: UUID?): OrganisationInvite{
        // Validate organisation existence
        val organisation: Organisation = organisationService.getOrganisationByID(organisationId)

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

        val inviteCode: String = generateInviteCode()
        val invite: OrganisationInvite = OrganisationInvite(
            organisation = organisation,
            userId = userId,
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
    @Throws(InvitationNotFoundException::class, InvalidArgumentException::class)
    fun handleInvitationAccept(token: String, userEmail: String): OrganisationMember{
        // Fetch User Profile from email provided from Colovia Core Data service
        val request: Request.Builder = httpService.generateInternalServiceConnection(
            target = HttpService.TargetController.COLOVIA,
            endpoint = "p/user/email/$userEmail"
            )

        // Validate Invitation
        val invite: OrganisationInvite = findInvitationThroughTokenOrThrow(token)

        // If invitation exists, cross compare invite with target user
        val user: UserProfile = httpService.get(request, UserProfile::class.java)
            ?: throw InvalidArgumentException("User not found")

        validateInviteOwnership(invite, userEmail)

        // Consume Invite and Add member to organisation
        invite.inviteStatus = OrganisationInvite.InviteStatus.ACCEPTED
        organisationInviteRepository.save(invite)
        return organisationMemberService.addMemberToOrganisation(invite, user)
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
    fun getOrganisationInvites(organisationId: UUID): List<OrganisationInvite>{
        return organisationInviteRepository.findByOrganisationId(organisationId)
    }

    /**
     * Fetches all invitations for a specific user
     *
     * @param userId The user ID to fetch all invitations for
     * @return List of OrganisationInvite objects
     */
    fun getUserInvites(userId: UUID): List<OrganisationInvite>{
        return organisationInviteRepository.findByUserId(userId)
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
            throw InvalidArgumentException("Invitation has expired")
        }

        if(userEmail != invite.email){
            throw InvalidArgumentException("Email does not match the invitation")
        }
    }

    private fun findInvitationThroughTokenOrThrow(token: String): OrganisationInvite {
        return organisationInviteRepository.findByToken(token).orElseThrow { InvitationNotFoundException("Invitation not found") }
    }
}