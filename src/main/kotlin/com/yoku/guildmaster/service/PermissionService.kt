package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.lookups.Permission
import com.yoku.guildmaster.entity.organisation.OrganisationPositionPermission
import com.yoku.guildmaster.repository.OrganisationPositionPermissionRepository
import com.yoku.guildmaster.repository.lookups.OrganisationPermissionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionService(private val positionPermissionRepository: OrganisationPositionPermissionRepository) {

    fun getOrganisationPermissionById(permissionId: UUID){}
    fun getManyOrganisationPermissionsById(permissionIds: List<UUID>){}

    fun getOrganisationPermissionByName(permissionName: Permission){}

    fun organisationHasPermission(organisationId: UUID, permission: Permission): Boolean{
        val positionPermissionKey = OrganisationPositionPermission.OrganisationPositionPermissionKey(
            positionId = organisationId,
            permissionId = permission.
        )
        positionPermissionRepository.findBy()
    }
}