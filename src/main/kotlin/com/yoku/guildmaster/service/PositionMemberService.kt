package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.MemberNotFoundException
import com.yoku.guildmaster.repository.OrganisationMemberRepository
import com.yoku.guildmaster.service.cached.CachedPositionService
import org.springframework.stereotype.Service
import java.util.*

@Service
class PositionMemberService(
    private val organisationMemberRepository: OrganisationMemberRepository,
    private val cachedService: CachedPositionService) {

    fun getOrganisationPositionMembers(positionId: UUID): List<OrgMemberDTO>{
        return organisationMemberRepository.findByPositionId(positionId).map { it.toDTO() }
    }

    fun getUserPositionWithPermissions(organisationId: UUID, userId: UUID): OrganisationPosition {
        return cachedService.getUserPositionWithPermissions(organisationId, userId)
    }

    fun evictUserPositionCache(positionId: UUID): Unit{
        val positionMembers: List<OrgMemberDTO> = getOrganisationPositionMembers(positionId)
        cachedService.evictUserPositionCache(positionId, positionMembers)
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
        return organisationMemberRepository.save(member)
    }

}