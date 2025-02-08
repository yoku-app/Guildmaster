package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.MemberNotFoundException
import com.yoku.guildmaster.repository.OrganisationMemberRepository
import com.yoku.guildmaster.service.cached.CachedPositionService
import com.yoku.guildmaster.service.external.UserService
import org.springframework.stereotype.Service
import java.util.*

@Service
class PositionMemberService(
    private val organisationMemberRepository: OrganisationMemberRepository,
    private val cachedService: CachedPositionService,
    private val userService: UserService) {

    fun getOrganisationPositionMembersWithUserProfile(positionId: UUID): List<OrgMemberDTO>{
        val organisationMembers: List<OrganisationMember> = getOrganisationPositionMembers(positionId)
        val memberProfiles: Map<UUID, UserPartialDTO?> = userService.fetchBatchUsersByIds(organisationMembers.map { it.id.userId })
        return organisationMembers.map { member -> member.toDTO(
            user = memberProfiles[member.id.userId],
            includeOrganisation = false
        ) }
    }

    fun getUserPositionWithPermissions(organisationId: UUID, userId: UUID): OrganisationPosition {
        return cachedService.getUserPositionWithPermissions(organisationId, userId)
    }

    /**
     * Evicts all users of a particular position from the cache in the case of a permission change
     */
    fun evictPositionCache(positionId: UUID): Unit{
        val positionMembers: List<OrganisationMember> = getOrganisationPositionMembers(positionId)
        cachedService.evictPositionCache(positionId, positionMembers)
    }

    fun evictUserPositionCache(organisationId: UUID, userId: UUID): Unit{
        cachedService.evictUserPositionCache(organisationId, userId)
    }

    fun moveMembersToPosition(fromPositionId: UUID, toPositionId: UUID): Unit{
        organisationMemberRepository.updateByPositionId(fromPositionId, toPositionId)
    }

    fun moveMemberToPosition(memberId: UUID, fromPositionId: UUID, toPosition: OrganisationPosition): OrganisationMember{
        val member: OrganisationMember = organisationMemberRepository.findByPositionIdAndUserUserId(fromPositionId, memberId)
            ?: throw MemberNotFoundException("Member not found in position")

        member.apply {
            this.position = toPosition
        }

        evictUserPositionCache(member.id.organisationId, memberId)
        return organisationMemberRepository.save(member)
    }

    private fun getOrganisationPositionMembers(positionId: UUID): List<OrganisationMember>{
        return organisationMemberRepository.findByPositionId(positionId)
    }

}