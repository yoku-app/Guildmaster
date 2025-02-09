package com.yoku.guildmaster.service.cached

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.exceptions.OrganisationNotFoundException
import com.yoku.guildmaster.repository.OrganisationRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.Throws

@Service
class CachedOrganisationService(private val organisationRepository: OrganisationRepository) {

    @Cacheable("organisation.organisation", key = "#id")
    @Throws(OrganisationNotFoundException::class)
    fun findOrganisationByIdOrThrow(id: UUID): Organisation {
        return this.organisationRepository.findById(id)
            .orElseThrow { OrganisationNotFoundException("Organisation not found") }
    }
}