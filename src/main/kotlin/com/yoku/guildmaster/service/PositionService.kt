package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationPositionNotFoundException
import com.yoku.guildmaster.repository.OrganisationPositionRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.Throws


@Service
class PositionService(private val organisationPositionRepository: OrganisationPositionRepository, private val positionMemberService: PositionMemberService) {

    @Throws(OrganisationPositionNotFoundException::class)
    private fun getPositionOrThrow(positionId: UUID): OrganisationPosition{
        return organisationPositionRepository.findById(positionId).orElseThrow { OrganisationPositionNotFoundException("Position not found") }
    }

    @Throws(OrganisationPositionNotFoundException::class)
    private fun getPositionOrThrow(position: OrgPositionDTO): OrganisationPosition{
        return getPositionOrThrow(position.id)
    }

    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationDefaultPosition(organisationId: UUID): OrganisationPosition{
        return organisationPositionRepository.findOrganisationPositionByOrganisationIdAndDefaultIsTrue(organisationId)
            ?: throw OrganisationNotFoundException("Organisation not found")
    }

    fun createPosition(position: OrgPositionDTO): OrganisationPosition{
        val orgPosition = OrganisationPosition(
            name = position.name,
            organisationId = position.organisationId,
            rank = position.rank,
        )

        orgPosition.apply {
            this.permissions = position.permissions.toMutableList()
        }

        if(position.isDefault){
            setNewPositionAsDefault(orgPosition)
        }

        return organisationPositionRepository.save(orgPosition)
    }

    fun updatePosition(position: OrgPositionDTO): OrganisationPosition{
        val currentPosition: OrganisationPosition = getPositionOrThrow(position)

        if(currentPosition.isDefault && !position.isDefault){
            throw IllegalArgumentException("Cannot remove default flag from default position")
        }

        if(position.isDefault){
            setNewPositionAsDefault(currentPosition)
        }

        currentPosition.apply {
            this.name = position.name
            this.rank = position.rank
        }

        // Find permission changes
        val (toAdd, toRemove) = findAlteredPermissions(currentPosition.permissions, position.permissions)

        // Directly modify JPA-managed permissions collection
        currentPosition.permissions.apply {
            addAll(toAdd)
            removeAll(toRemove)
        }

        // Saving the entity will automatically update the join table
        organisationPositionRepository.save(currentPosition)

        positionMemberService.evictUserPositionCache(position.id)

        return currentPosition
    }

    /**
     * Remove a position from the organisation, and move any existing members to a new specified position
     */
    fun removePosition(positionId: UUID, newPositionId: UUID): Unit{
        val newPosition = getPositionOrThrow(newPositionId)

        // Move all members to the new position
        positionMemberService.moveMembersToPosition(positionId, newPosition.id!!)
        // Remove the position
        organisationPositionRepository.deleteById(positionId)
        positionMemberService.evictUserPositionCache(positionId)
    }


    /**
     * Find the permissions that have been added and removed from the position
     *
     * @return Pair of added and removed permissions (Pair<Added, Removed>)
     */
    private fun findAlteredPermissions(prevPermissions: List<OrganisationPermission>, currPermissions: List<OrganisationPermission>):
            Pair<List<OrganisationPermission>, List<OrganisationPermission>>{

        val addedPermissions = currPermissions.filter { permission -> !prevPermissions.contains(permission) }
        val removedPermissions = prevPermissions.filter { permission -> !currPermissions.contains(permission) }

        return Pair(addedPermissions, removedPermissions)
    }

    private fun setNewPositionAsDefault(position: OrganisationPosition) {
        organisationPositionRepository.clearDefaultPosition(position.organisationId)
        position.isDefault = true
    }
}