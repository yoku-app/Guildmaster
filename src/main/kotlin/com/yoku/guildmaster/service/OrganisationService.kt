package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
) {

    @Throws(InvalidArgumentException::class, OrganisationNotFoundException::class)
    fun getOrganisationByID(id: UUID): Organisation {
        return this.findOrganisationByIdOrThrow(id)
    }

    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationByName(name: String): Organisation{
        return this.findOrganisationByNameOrThrow(name)
    }

    @Throws(OrganisationNotFoundException::class, InvalidOrganisationPermissionException::class)
    fun updateOrganisation(updaterUserId: UUID, organisation: OrganisationDTO): OrganisationDTO{
        // todo: Validate Users Organisation permissions for ORG Crud
        if(false){
            throw InvalidOrganisationPermissionException("User does not have permission to update Organisation")
        }

        // Validate Organisations existence
        val entity: Organisation = findOrganisationByIdOrThrow(organisation.id)

        // Update Organisation
        entity.apply {
            this.name = organisation.name
            this.description = organisation.description
            this.avatarURL = organisation.avatarURL
            this.publicStatus = organisation.publicStatus
            this.email = organisation.email
            this.creator = organisation.creator?: this.creator
            this.industry = organisation.industry?: this.industry
        }

        return this.organisationRepository.save(entity).toDTO()
    }

    fun saveOrganisation (organisation: OrganisationDTO): OrganisationDTO{
        if(organisation.creator == null){
            throw InvalidArgumentException("Organisation must have a creator")
        }

        if(organisation.industry == null){
            throw InvalidArgumentException("Organisation must have an industry")
        }

        val entity = Organisation(
            id = UUID.randomUUID(),
            name = organisation.name,
            description = organisation.description,
            avatarURL = organisation.avatarURL,
            publicStatus = organisation.publicStatus,
            email = organisation.email,
            creator = organisation.creator,
            industry = organisation.industry
        )

        return this.organisationRepository.save(entity).toDTO()
    }


    @Throws(OrganisationNotFoundException::class)
    private fun findOrganisationByIdOrThrow(id: UUID): Organisation {
        return this.organisationRepository.findById(id)
            .orElseThrow { OrganisationNotFoundException("Organisation not found") }
    }

    @Throws(OrganisationNotFoundException::class)
    fun findOrganisationByNameOrThrow(name: String): Organisation {
        return this.organisationRepository.findByName(name)
            .orElseThrow { OrganisationNotFoundException("Organisation not found") }
    }

    fun deleteOrganisation(id: UUID){
        //todo: Validate Users Organisation permissions for ORG Crud
        if(false){
            throw InvalidOrganisationPermissionException("User does not have permission to update Organisation")
        }
        this.organisationRepository.deleteById(id)
    }

}