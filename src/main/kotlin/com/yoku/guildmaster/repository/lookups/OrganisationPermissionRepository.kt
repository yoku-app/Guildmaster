package com.yoku.guildmaster.repository.lookups

import com.yoku.guildmaster.entity.organisation.OrganisationPermission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganisationPermissionRepository: JpaRepository<OrganisationPermission, UUID>