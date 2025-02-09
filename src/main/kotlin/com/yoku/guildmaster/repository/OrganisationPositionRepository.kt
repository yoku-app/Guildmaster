package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface OrganisationPositionRepository: JpaRepository<OrganisationPosition, UUID> {
    @Query("""
        SELECT op FROM OrganisationPosition op
        WHERE op.organisationId = :organisationId
        AND op.isDefault = true
    """)
    fun findDefaultOrganisationPosition(organisationId: UUID): OrganisationPosition?

    @Query("update OrganisationPosition op set op.isDefault = false where op.organisationId = :organisationId and op.isDefault = true")
    fun clearDefaultPosition(organisationId: UUID)

    @EntityGraph(attributePaths = ["permissions"])
    @Query("""
        SELECT op FROM OrganisationMember om
        JOIN om.position op
        LEFT JOIN FETCH op.permissions
        WHERE om.id.organisationId = :organisationId AND om.id.userId = :userId
    """)
    fun findUserPositionWithPermissions(
         organisationId: UUID,
         userId: UUID
    ): OrganisationPosition?

    fun findOrganisationPositionsByOrganisationId(organisationId: UUID): List<OrganisationPosition>
}