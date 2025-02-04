package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface OrganisationPositionRepository: JpaRepository<OrganisationPosition, UUID> {
    fun findByOrganisationIdAndDefaultIsTrue(organisationId: UUID): OrganisationPosition?

    @Query("""
        SELECT op FROM OrganisationMember om
        JOIN om.position op
        LEFT JOIN FETCH op.permissions
        WHERE om.organisation.id = :organisationId 
        AND om.user.userId = :userId
    """)
    fun findUserPositionWithPermissions(
         organisationId: UUID,
         userId: UUID
    ): OrganisationPosition?
}