package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganisationPositionRepository: JpaRepository<OrganisationPosition, UUID> {
    fun findByOrganisationIdAndDefaultIsTrue(organisationId: UUID): OrganisationPosition?
}