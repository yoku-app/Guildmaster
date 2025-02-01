package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgInviteDTO
import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.service.InvitationService
import jakarta.persistence.Enumerated
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

    @PostMapping("/organisation/{organisationId}/email/{email}/creator/{creatorId}")
    fun inviteToOrganisation(@PathVariable organisationId: UUID, @PathVariable email: String,
                             @PathVariable creatorId: UUID, @RequestParam(name = "userId", required = false) userId: UUID?): ResponseEntity<OrgInviteDTO> {
        val invite: OrganisationInvite = invitationService.createInvitation(organisationId, email, creatorId ,userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(invite.toDTO())
    }

    @PostMapping("/accept/{inviteToken}/email/{email}")
    fun acceptInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<OrgMemberDTO> {
        val organisationMember: OrganisationMember = invitationService.handleInvitationAccept(inviteToken, email)
        return ResponseEntity.status(HttpStatus.CREATED).body(organisationMember.toDTO())
    }

    @PostMapping("/reject/{inviteToken}/user/{email}")
    fun rejectInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.handleInvitationReject(inviteToken, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/organisation/{organisationId}")
    fun getAllOrganisationInvites(@PathVariable organisationId: UUID,
                                  @RequestParam(name = "inviteStatus", required = false) inviteStatus: OrganisationInvite.InviteStatus?
    ): ResponseEntity<List<OrgInviteDTO>> {
        val invites: List<OrganisationInvite> = invitationService.getOrganisationInvites(organisationId, inviteStatus)
        return ResponseEntity.ok(invites.map { it.toDTO() })
    }

    @GetMapping("/user/{userId}")
    fun getAllUserInvites(@PathVariable userId: UUID): ResponseEntity<List<OrgInviteDTO>> {
        val invites: List<OrganisationInvite> = invitationService.getUserInvites(userId)
        return ResponseEntity.ok(invites.map { it.toDTO() })
    }

    @DeleteMapping("/organisation/{organisationId}/email/{email}")
    fun revokeUserInvite(@PathVariable organisationId: UUID, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.revokeInvitation(organisationId, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


}