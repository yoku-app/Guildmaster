package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import com.yoku.guildmaster.entity.organisation.Permission
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationMember.OrganisationMemberKey
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationMemberRepository
import com.yoku.guildmaster.service.external.UserService
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.Throws

@Service
class MemberService(private val organisationService: OrganisationService,
                    private val organisationMemberRepository: OrganisationMemberRepository,
                    private val positionMemberService: PositionMemberService,
                    private val permissionService: PermissionService,
                    private val positionService: PositionService,
                    private val userService: UserService
) {

    @Throws(InvalidArgumentException::class, OrganisationNotFoundException::class)
    fun fetchOrganisationMembers(id: UUID): List<OrgMemberDTO>{
        val organisationMembers: List<OrganisationMember> = organisationMemberRepository.findByIdOrganisationId(id)
        val memberProfiles: Map<UUID, UserPartialDTO?> = userService.fetchBatchUsersByIds(organisationMembers.map { it.id.userId })

        // No point in returning the organisation object for each member given that we are fetching the members of a specific organisation
        return organisationMembers.map { member -> member.toDTO(
            user = memberProfiles[member.id.userId],
            includeOrganisation = false
        ) }
    }

    /**
     * Fetches the organisations that a user is a member of, and all the associated details
     * @param id The user ID to fetch the organisations for
     */
    fun fetchUserOrganisations(id: UUID): List<OrgMemberDTO>{

        // Won't invoke the user service to fetch the user profile as we are only interested in the organisation details
        return organisationMemberRepository.findByIdUserId(id).map {
            it.toDTO(
                user = null,
                includeOrganisation = true
            ) }
    }

    /**
     * Fetches an Organisation Member object by the organisation and user ID
     */
    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationMember(organisationId: UUID, userId: UUID): OrganisationMember{
        return organisationMemberRepository.findById(OrganisationMemberKey(organisationId, userId))
            .orElseThrow { OrganisationNotFoundException("Organisation Member not found") }
    }

    /**
     * Removes a member from an organisation, this can occur in the following manners:
     *  - A user is trying to leave an organisation
     *  - A privileged user is removing another user from the organisation
     */
    @Throws(InvalidOrganisationPermissionException::class, OrganisationNotFoundException::class, InvalidArgumentException::class)
    fun removeMemberFromOrganisation(organisationId: UUID, userId: UUID, requesterUserId: UUID){
        // Fetch Organisation details
        val organisation: Organisation = organisationService.findOrganisationByIdOrThrow(organisationId)

        // Validate user is a member of the organisation
        val member: OrganisationMember = getOrganisationMember(organisationId, userId)

        // Validate User is not the current owner of the organisation object
        if(organisation.creatorId == userId){
            throw InvalidOrganisationPermissionException("User must transfer ownership before being removed from the organisation")
        }

        //If the requester is trying to leave on their on will, remove them from the organisation
        if(member.id.userId == requesterUserId ){
            // Remove the user from the organisation
            organisationMemberRepository.deleteById(OrganisationMemberKey(organisationId, userId))
        }

        // Validate that the requester has the necessary permissions to remove a user from the organisation and
        // is of a higher ranking than the user they are trying to remove
        val requesterPosition: OrganisationPosition = positionMemberService.getUserPositionWithPermissions(organisationId, requesterUserId)
        val memberPosition: OrganisationPosition = positionMemberService.getUserPositionWithPermissions(organisationId, userId)

        if(!permissionService.userHasPermission(requesterPosition, Permission.MEMBER_REMOVE, memberPosition)){
            throw InvalidOrganisationPermissionException("User does not have permission to remove members from the organisation, " +
                    "or is of a lower ranked role to the target")
        }

        // Remove the user from the organisation
        organisationMemberRepository.deleteById(OrganisationMemberKey(organisationId, userId))
    }

    /**
     * Adds a member to an organisation upon successfully accepting an invitation
     *
     * Prior validation should occur to ensure that the user is not already a current member of this organisation
     */
    fun addMemberToOrganisation(invite: OrganisationInvite, user: UserPartialDTO): OrganisationMember{
        // Create a new Organisation Member object
        val member: OrganisationMember = invite.toOrganisationMember(user.id)
        val position: OrganisationPosition = positionService.getOrganisationDefaultPosition(
            invite.organisation.id ?: throw OrganisationNotFoundException("Organisation not found"))

        member.apply {
            this.position = position
        }

        // Save the new member to the organisation
        return organisationMemberRepository.save(member)
    }
}