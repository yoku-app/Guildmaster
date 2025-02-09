package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.service.OrganisationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/organisation")
class OrganisationController(private val organisationService: OrganisationService){

    @GetMapping("/id/{id}")
    fun getOrganisationById(@PathVariable id: UUID): ResponseEntity<OrganisationDTO>{
        val organisation: OrganisationDTO = this.organisationService.getOrganisationByID(id)
        return ResponseEntity.ok(organisation)
    }

    @GetMapping("/name/{name}")
    fun getOrganisationByName(@PathVariable name: String): ResponseEntity<OrganisationDTO>{
        val organisation: OrganisationDTO = this.organisationService.getOrganisationByName(name)
        return ResponseEntity.ok(organisation)
    }

    @PutMapping("/")
    fun updateOrganisation(@RequestBody organisation: OrganisationDTO, @RequestHeader("X-User-Id") originUserId: UUID?): ResponseEntity<OrganisationDTO>{
        if(originUserId == null) throw IllegalArgumentException("User Id was not provided in request headers")

        val updatedOrganisation: OrganisationDTO = this.organisationService.updateOrganisation(
            updaterUserId = originUserId,
            organisation = organisation)

        return ResponseEntity.ok(updatedOrganisation)
    }

    @PostMapping("/")
    fun createOrganisation(@RequestBody organisation: OrganisationDTO): ResponseEntity<OrganisationDTO>{
        val createdOrganisation: OrganisationDTO = this.organisationService.saveOrganisation(organisation)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganisation)
    }

    @DeleteMapping("/{organisationId}")
    fun deleteOrganisation(@PathVariable organisationId: UUID, @RequestHeader("X-User-Id") originUserId: UUID?): ResponseEntity<Unit>{
        if(originUserId == null) throw IllegalArgumentException("User Id was not provided in request headers")

        this.organisationService.deleteOrganisation(organisationId, originUserId)
        return ResponseEntity.ok().build()
    }
}