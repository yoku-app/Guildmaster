package com.yoku.guildmaster.service.cached

import com.yoku.guildmaster.entity.dto.CachedOrgPosition
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationPositionNotFoundException
import com.yoku.guildmaster.repository.OrganisationPositionRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class CachedPositionService(
    private val organisationPositionRepository: OrganisationPositionRepository,
    private val redisTemplate: RedisTemplate<String, OrganisationPosition>
) {

    @Throws(OrganisationNotFoundException::class)
    @Cacheable("organisation.position.user", key = "#organisationId + '-' + #userId")
    fun getUserPositionWithPermissions(organisationId: UUID, userId: UUID): CachedOrgPosition {
        val position: OrganisationPosition = organisationPositionRepository.findUserPositionWithPermissions(organisationId, userId)
            ?: throw OrganisationNotFoundException("Organisation not found for user")
        return position.toCache()
    }

    /**
     * Upon a permission change within an Organisation position, we will evict the cache for all
     * relevant users within this specific position
     */
    fun evictPositionCache(positionId: UUID, members: List<OrganisationMember>): Unit{
        redisTemplate.delete(members.map { member -> "organisation.position.user::${member.id.organisationId}-${member.id.userId}" })
    }

    fun evictUserPositionCache(organisationId: UUID, userId: UUID): Unit{
        redisTemplate.delete("organisation.position.user::${organisationId}-${userId}")
    }
}