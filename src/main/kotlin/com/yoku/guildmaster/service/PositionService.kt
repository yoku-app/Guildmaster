package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.repository.OrganisationPositionRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PositionService(private val permissionService: PermissionService, private val organisationPositionRepository: OrganisationPositionRepository) {

    @Cacheable("organisation.position.user", key = "#organisationId + '-' + #userId")
    fun getUserPositionWithPermissions(organisationId: UUID, userId: UUID): OrganisationPosition {
        return organisationPositionRepository.findUserPositionWithPermissions(organisationId, userId)?: throw Exception("Position not found")
    }

    fun createPosition(position: OrgPositionDTO, isDefault: Boolean = false){
        val orgPosition = OrganisationPosition(
            name = position.name,
            organisationId = position.organisationId,
            rank = position.rank,
            isDefault = isDefault
        )

        if(isDefault){
            setNewPositionAsDefault(orgPosition)
        }
    }

    fun updatePosition(){}

    fun removePosition(){}
    fun updatePositionHierarchyRank(){}

    /**
     * Set the new position as the default position for the organisation
     * and will remove the default flag from the previous default position
     */
    private fun setNewPositionAsDefault(position: OrganisationPosition){}

    private fun addPermissionsToPosition(position: OrganisationPosition, permissions: List<OrganisationPosition>){}
    private fun removePermissionsFromPosition(position: OrganisationPosition, permissions: List<OrganisationPosition>){}

    fun getOrganisationDefaultPosition(){}



}