package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.service.InvitationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/invite")
class InvitationController(private val invitationService: InvitationService) {

    @PostMapping("/organisation/{organisationId}/email/{email}")
    fun inviteToOrganisation(@PathVariable organisationId: UUID, @PathVariable email: String, @RequestParam(name = "user", required = false) userId: UUID?): ResponseEntity<OrganisationInvite> {
        val invite: OrganisationInvite = invitationService.createInvitation(organisationId, email, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(invite)
    }

    @PostMapping("/accept/{inviteToken}/email/{email}")
    fun acceptInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<OrganisationMember> {
        val organisationMember: OrganisationMember = invitationService.handleInvitationAccept(inviteToken, email)
        return ResponseEntity.status(HttpStatus.CREATED).body(organisationMember)
    }

    @PostMapping("/reject/{inviteToken}/user/{email}")
    fun rejectInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.handleInvitationReject(inviteToken, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/organisation/{organisationId}")
    fun getAllOrganisationInvites(@PathVariable organisationId: UUID): ResponseEntity<List<OrganisationInvite>> {
        val invites: List<OrganisationInvite> = invitationService.getOrganisationInvites(organisationId)
        return ResponseEntity.ok(invites)
    }

    @GetMapping("/user/{userId}")
    fun getAllUserInvites(@PathVariable userId: UUID): ResponseEntity<List<OrganisationInvite>> {
        val invites: List<OrganisationInvite> = invitationService.getUserInvites(userId)
        return ResponseEntity.ok(invites)
    }

    @DeleteMapping("/organisation/{organisationId}/email/{email}")
    fun revokeUserInvite(@PathVariable organisationId: UUID, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.revokeInvitation(organisationId, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


}