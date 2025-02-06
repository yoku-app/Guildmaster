package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import com.yoku.guildmaster.entity.lookups.Permission
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.entity.organisation.OrganisationPositionPermission
import com.yoku.guildmaster.repository.OrganisationPositionPermissionRepository
import org.springframework.stereotype.Service


@Service
class PermissionService {

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