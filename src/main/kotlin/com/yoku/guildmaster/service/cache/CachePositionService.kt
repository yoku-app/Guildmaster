package com.yoku.guildmaster.service.cache

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationPositionNotFoundException
import com.yoku.guildmaster.repository.OrganisationPositionRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class CachePositionService(
    private val organisationPositionRepository: OrganisationPositionRepository,
    private val redisTemplate: RedisTemplate<String, OrganisationPosition>
) {

    @Throws(OrganisationNotFoundException::class)
    @Cacheable("organisation.position.user", key = "#organisationId + '-' + #userId")
    fun getUserPositionWithPermissions(organisationId: UUID, userId: UUID): OrganisationPosition {
        return organisationPositionRepository.findUserPositionWithPermissions(organisationId, userId)?:
        throw OrganisationPositionNotFoundException("Position not found")
    }

    /**
     * Upon a permission change within an Organisation position, we will evict the cache for all
     * relevant users within this specific position
     */
    fun evictUserPositionCache(positionId: UUID, members: List<OrgMemberDTO>): Unit{
        redisTemplate.delete(members.map { member -> "organisation.position.user::${member.organisation.id}-${member.id}" })
    }
}