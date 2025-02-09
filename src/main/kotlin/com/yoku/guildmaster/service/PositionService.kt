package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPermissionDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.organisation.OrganisationPermission
import com.yoku.guildmaster.entity.organisation.Permission
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.MemberNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.exceptions.OrganisationPositionNotFoundException
import com.yoku.guildmaster.repository.OrganisationPositionRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.Throws


@Service
class PositionService(
    private val organisationPositionRepository: OrganisationPositionRepository,
    private val positionMemberService: PositionMemberService,
    private val permissionService: PermissionService
) {

    @Throws(OrganisationPositionNotFoundException::class)
    private fun getPositionOrThrow(positionId: UUID): OrganisationPosition{
        return organisationPositionRepository.findById(positionId).orElseThrow { OrganisationPositionNotFoundException("Position not found") }
    }

    @Throws(OrganisationPositionNotFoundException::class)
    private fun getPositionOrThrow(position: OrgPositionDTO): OrganisationPosition{
        return getPositionOrThrow(position.id)
    }

    fun getOrganisationPositions(organisationId: UUID): List<OrgPositionDTO>{
        return organisationPositionRepository.findOrganisationPositionsByOrganisationId(organisationId)
            .map { it.toDTO() }
    }

    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationDefaultPosition(organisationId: UUID): OrganisationPosition{
        return organisationPositionRepository.findDefaultOrganisationPosition(organisationId)
            ?: throw OrganisationNotFoundException("Organisation not found")
    }

    /**
     *  Create a new position in the organisation
     */
    fun createPosition(position: OrgPositionDTO, requesterId: UUID): OrgPositionDTO{

        // Validate User has permission to create a new position
        val userPosition: OrgPositionDTO = positionMemberService.getUserPositionWithPermissions(position.organisationId, requesterId)
        if(!permissionService.userHasPermission(userPosition, Permission.ROLE_CREATE)){
            throw IllegalArgumentException("User does not have permission to create a new position")
        }

        val orgPosition = OrganisationPosition(
            name = position.name,
            organisationId = position.organisationId,
            rank = position.rank,
        )

        val permissions = position.permissions.map { OrganisationPermission(it.id, it.name) }



        if(position.isDefault){
            setNewPositionAsDefault(orgPosition)
        }

        return organisationPositionRepository.save(orgPosition).toDTO()
    }

    /**
     * Update a position in the organisation
     */
    fun updatePosition(position: OrgPositionDTO, requesterId: UUID): OrgPositionDTO{

        // Validate User has permission to update a position
        val userPosition = positionMemberService.getUserPositionWithPermissions(position.organisationId, requesterId)
        if(!permissionService.userHasPermission(userPosition, Permission.ROLE_UPDATE)){
            throw IllegalArgumentException("User does not have permission to update a position")
        }

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
        val (toAdd, toRemove) = findAlteredPermissions(currentPosition.permissions.map { it.toDto() }, position.permissions)

        // Directly modify JPA-managed permissions collection
        currentPosition.permissions.apply {
            addAll(toAdd.map { OrganisationPermission(it.id, it.name) })
            removeAll(toRemove.map { OrganisationPermission(it.id, it.name) })
        }

        // Saving the entity will automatically update the join table
        organisationPositionRepository.save(currentPosition)
        positionMemberService.evictPositionCache(position.id)
        return currentPosition.toDTO()
    }

    /**
     * Remove a position from the organisation, and move any existing members to a new specified position
     */
    fun removePosition(positionId: UUID, newPositionId: UUID, requesterId: UUID): Unit{
        val newPosition: OrganisationPosition = getPositionOrThrow(newPositionId)

        // Validate User has permission to update a position
        val userPosition = positionMemberService.getUserPositionWithPermissions(newPosition.organisationId, requesterId)
        if(!permissionService.userHasPermission(userPosition, Permission.ROLE_DELETE)){
            throw IllegalArgumentException("User does not have permission to delete a position")
        }

        // Move all members to the new position
        positionMemberService.moveMembersToPosition(positionId, newPosition.id!!)
        // Remove the position
        organisationPositionRepository.deleteById(positionId)
        positionMemberService.evictPositionCache(positionId)
    }

    /**
     * Move a user from one position to another
     */
    @Throws(MemberNotFoundException::class, OrganisationPositionNotFoundException::class)
    fun moveUserToPosition(member: OrgMemberDTO, toPositionId: UUID, requesterId: UUID): OrgMemberDTO{
        if(member.position.id == toPositionId){
            throw IllegalArgumentException("User is already in the target position")
        }

        if(member.user == null){
            throw MemberNotFoundException("User object must be included in request")
        }

        val toPosition: OrganisationPosition = getPositionOrThrow(toPositionId)
        // Validate User has permission to update a position
        val userPosition = positionMemberService.getUserPositionWithPermissions(toPosition.organisationId, requesterId)
        if(!permissionService.userHasPermission(userPosition, Permission.MEMBER_UPDATE_ROLE)){
            throw IllegalArgumentException("User does not have permission to move other users")
        }

        val updatedOrgMember: OrganisationMember =
            positionMemberService.moveMemberToPosition(
                memberId = member.user.id,
                fromPositionId = member.position.id,
                toPosition = toPosition)

        return updatedOrgMember.toDTO(user = member.user, includeOrganisation = false)
    }

    /**
     * Find the permissions that have been added and removed from the position
     *
     * @return Pair of added and removed permissions (Pair<Added, Removed>)
     */
    private fun findAlteredPermissions(prevPermissions: List<OrgPermissionDTO>, currPermissions: List<OrgPermissionDTO>):
            Pair<List<OrgPermissionDTO>, List<OrgPermissionDTO>>{

        val addedPermissions = currPermissions.filter { permission -> !prevPermissions.contains(permission) }
        val removedPermissions = prevPermissions.filter { permission -> !currPermissions.contains(permission) }

        return Pair(addedPermissions, removedPermissions)
    }

    private fun setNewPositionAsDefault(position: OrganisationPosition) {
        organisationPositionRepository.clearDefaultPosition(position.organisationId)
        position.isDefault = true
    }
}