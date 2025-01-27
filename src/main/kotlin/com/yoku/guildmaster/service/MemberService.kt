package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.organisation.OrgMemberDTO
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationMember.OrganisationMemberKey;
import com.yoku.guildmaster.entity.user.UserProfile
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationMemberRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class MemberService(private val organisationService: OrganisationService, private val organisationMemberRepository: OrganisationMemberRepository) {

    @Throws(InvalidArgumentException::class, OrganisationNotFoundException::class)
    fun fetchOrganisationMembers(id: UUID): List<OrgMemberDTO>{
        return organisationService.getOrganisationByID(id).members.map { OrgMemberDTO(it.user) }
    }

    /**
     * Removes a member from an organisation, this can occur in the following manners:
     *  - A user is trying to leave an organisation
     *  - A privileged user is removing another user from the organisation
     */
    @Throws(InvalidOrganisationPermissionException::class, OrganisationNotFoundException::class, InvalidArgumentException::class)
    fun removeMemberFromOrganisation(organisationId: UUID, userId: UUID, requesterUserId: UUID){
        // Fetch Organisation details
        val organisation: Organisation = organisationService.getOrganisationByID(organisationId)

        // Validate user is a member of the organisation
        if(!organisation.members.any { it.user.userId == userId }){
            throw InvalidArgumentException("User is not a member of this organisation")
        }

        // Validate User is not the current owner of the organisation object
        if(organisation.creator.userId == userId){
            throw InvalidOrganisationPermissionException("User must transfer ownership before being removed from the organisation")
        }

        //If this is not the user trying to self-leave, check if the user has the correct permissions to remove another user

        //todo: User Permissions..
        if(userId != organisationId && false){
                throw InvalidOrganisationPermissionException("User does not have the correct permissions to remove another user")
        }

        // Remove the user from the organisation
        organisationMemberRepository.deleteById(OrganisationMemberKey(organisationId, userId))
    }

    /**
     * Adds a member to an organisation upon successfully accepting an invitation
     *
     * Prior validation should occur to ensure that the user is not already apart of this current organisation
     */
    fun addMemberToOrganisation(invite: OrganisationInvite, user: UserProfile): OrganisationMember{
        // Create a new Organisation Member object
        val member: OrganisationMember = invite.toOrganisationMember(user)
        // Save the new member to the organisation
        return organisationMemberRepository.save(member)
    }
}