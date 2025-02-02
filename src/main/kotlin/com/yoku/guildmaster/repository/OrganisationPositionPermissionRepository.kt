package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationPositionPermission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganisationPositionPermissionRepository: JpaRepository<OrganisationPositionPermission, UUID> {
}