package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.entity.organisation.Permission
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import com.yoku.guildmaster.service.cached.CachedOrganisationService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val cachedOrganisationService: CachedOrganisationService,
    private val positionMemberService: PositionMemberService,
    private val permissionService: PermissionService
) {
    @Throws(InvalidArgumentException::class, OrganisationNotFoundException::class)
    fun getOrganisationByID(id: UUID): OrganisationDTO {
        // Fetch Organisation Creator/Industry
        return cachedOrganisationService.findOrganisationByIdOrThrow(id).toDTO()
    }

    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationByName(name: String): OrganisationDTO{
        return this.findOrganisationByNameOrThrow(name).toDTO()
    }

    @CachePut("organisation.organisation", key = "#organisation.id")
    @Throws(OrganisationNotFoundException::class, InvalidOrganisationPermissionException::class)
    fun updateOrganisation(updaterUserId: UUID, organisation: OrganisationDTO): OrganisationDTO{
        // Validate user's permissions
        val userPosition: OrgPositionDTO =
            positionMemberService.getUserPositionWithPermissions(organisation.id, updaterUserId)

        if(!permissionService.userHasPermission(userPosition, Permission.ORGANISATION_EDIT)){
            throw InvalidOrganisationPermissionException("User does not have permission to update Organisation")
        }

        // Validate Organisations existence
        val entity: Organisation = cachedOrganisationService.findOrganisationByIdOrThrow(organisation.id)

        // Update Organisation
        entity.apply {
            this.name = organisation.name
            this.description = organisation.description
            this.avatarURL = organisation.avatarURL
            this.publicStatus = organisation.publicStatus
            this.email = organisation.email
            this.creatorId = organisation.creator?.id ?: this.creatorId
            this.industryId = organisation.industry?.id ?: this.industryId
        }

        return this.organisationRepository.save(entity).toDTO()
    }

    fun saveOrganisation (organisation: OrganisationDTO): OrganisationDTO{
        if(organisation.creator == null){
            throw InvalidArgumentException("Organisation must have a creator")
        }

        val entity = Organisation(
            name = organisation.name,
            description = organisation.description,
            avatarURL = organisation.avatarURL,
            publicStatus = organisation.publicStatus,
            email = organisation.email,
            creatorId = organisation.creator.id,
            industryId = organisation.industry?.id
        )

        return this.organisationRepository.save(entity).toDTO()
    }

    @Throws(OrganisationNotFoundException::class)
    fun findOrganisationByNameOrThrow(name: String): Organisation {
        return this.organisationRepository.findByName(name)
            .orElseThrow { OrganisationNotFoundException("Organisation not found") }
    }

    @CacheEvict("organisation.organisation", key = "#id")
    fun deleteOrganisation(id: UUID, userId: UUID){
        val userPosition: OrgPositionDTO = positionMemberService.getUserPositionWithPermissions(id, userId)
        if(!permissionService.userHasPermission(userPosition, Permission.ORGANISATION_DELETE)){
            throw InvalidOrganisationPermissionException("User does not have permission to delete Organisation")
        }
        this.organisationRepository.deleteById(id)
    }

}