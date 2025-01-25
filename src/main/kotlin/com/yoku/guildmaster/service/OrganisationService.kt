package com.yoku.guildmaster.service

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.exceptions.InvalidArgumentException
import com.yoku.guildmaster.exceptions.InvalidOrganisationPermissionException
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import com.yoku.guildmaster.util.UUIDUtil
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.Throws

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val uuidUtil: UUIDUtil
) {

    @Throws(InvalidArgumentException::class, OrganisationNotFoundException::class)
    fun getOrganisationByID(id: String): Organisation {
        val uuid: UUID = this.uuidUtil.parseUUID(id)
        return this.findOrganisationByIdOrThrow(uuid)
    }

    @Throws(OrganisationNotFoundException::class)
    fun getOrganisationByName(name: String): Organisation{
        return this.findOrganisationByNameOrThrow(name)
    }

    @Throws(OrganisationNotFoundException::class, InvalidOrganisationPermissionException::class)
    fun updateOrganisation(updaterUserId: UUID, organisation: Organisation): Organisation{
        // todo: Validate Users Organisation permissions for ORG Crud
        if(false){
            throw InvalidOrganisationPermissionException("User does not have permission to update Organisation")
        }

        // Validate Organisations existence
        findOrganisationByIdOrThrow(organisation.id)
        return organisationRepository.save(organisation)
    }

    fun saveOrganisation (organisation: Organisation): Organisation{
        return this.organisationRepository.save(organisation)
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

    fun deleteOrganisation(id: String){
        //todo: Validate Users Organisation permissions for ORG Crud
        if(false){
            throw InvalidOrganisationPermissionException("User does not have permission to update Organisation")
        }

        val uuid: UUID = this.uuidUtil.parseUUID(id)
        this.organisationRepository.deleteById(uuid)
    }
}