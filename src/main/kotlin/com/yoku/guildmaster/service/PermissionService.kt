package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import com.yoku.guildmaster.entity.lookups.Permission
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.entity.organisation.OrganisationPositionPermission
import com.yoku.guildmaster.repository.OrganisationPositionPermissionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PermissionService(private val organisationPositionPermissionRepository: OrganisationPositionPermissionRepository) {

    fun getPositionPermissions(positionId: UUID): List<OrganisationPositionPermission>{
        return organisationPositionPermissionRepository.getOrganisationPositionPermissionsByPositionId(positionId)
    }

    fun addPermissionsToPosition(position: OrganisationPosition, permissions: List<OrganisationPermission>): List<OrganisationPositionPermission>{
        if(position.id == null) throw IllegalArgumentException("Position ID cannot be null")
        if(permissions.isEmpty()) return listOf()

        return organisationPositionPermissionRepository.saveAll(permissions.map
        { permission ->
            val key = OrganisationPositionPermission.OrganisationPositionPermissionKey(position.id, permission.id)
            OrganisationPositionPermission(key, position, permission) })
    }

    /**
     * Check if the user has the required permission
     * This should take in a fully User Position object that has had all permissions fully loaded
     * (see. PositionService.getUserPositionWithPermissions)
     * It is best if we used this as it is cached, whilst a lazy loaded entity would require an additional
     * query to find all permissions
     */
    fun userHasPermission(userPosition: OrganisationPosition, permission: Permission): Boolean{
        return userPosition.permissions.any { it.id == permission.id }
    }

    /**
     * Hierarchical based permission check
     */
    fun userHasPermission(userPosition: OrganisationPosition, permission: Permission, targetPosition: OrganisationPosition): Boolean{
        return userPosition.permissions.any { it.id == permission.id } && userPosition.rank > targetPosition.rank
    }
}