package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.service.OrganisationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/organisation")
class OrganisationController(private val organisationService: OrganisationService){

    @GetMapping("/id/{id}")
    fun getOrganisationById(@PathVariable id: UUID): ResponseEntity<OrganisationDTO>{
        val organisation: OrganisationDTO = this.organisationService.getOrganisationByID(id).toDTO()
        return ResponseEntity.ok(organisation)
    }

    @GetMapping("/name/{name}")
    fun getOrganisationByName(@PathVariable name: String): ResponseEntity<OrganisationDTO>{
        val organisation: OrganisationDTO = this.organisationService.getOrganisationByName(name).toDTO()
        return ResponseEntity.ok(organisation)
    }

    @PutMapping("/{userId}")
    fun updateOrganisation(@RequestBody organisation: OrganisationDTO, @PathVariable userId: UUID): ResponseEntity<OrganisationDTO>{
        val updatedOrganisation: OrganisationDTO = this.organisationService.updateOrganisation(
            updaterUserId = userId,
            organisation = organisation)

        return ResponseEntity.ok(updatedOrganisation)
    }

    @PostMapping("/industry/{industryId}/user/{userId}")
    fun createOrganisation(@RequestBody organisation: OrganisationDTO): ResponseEntity<OrganisationDTO>{
        val createdOrganisation: OrganisationDTO = this.organisationService.saveOrganisation(organisation)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganisation)
    }

    @DeleteMapping("/{id}")
    fun deleteOrganisation(@PathVariable id: UUID): ResponseEntity<Unit>{
        this.organisationService.deleteOrganisation(id)
        return ResponseEntity.ok().build()
    }
}