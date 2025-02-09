package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgInviteDTO
import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.exceptions.UnauthorizedException
import com.yoku.guildmaster.service.InvitationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/invite")
class InvitationController(private val invitationService: InvitationService) {

    @PostMapping("/organisation/{organisationId}/email/{email}")
    fun inviteToOrganisation(@PathVariable organisationId: UUID, @PathVariable email: String,
                             @RequestHeader("X-User-Id") creatorId: UUID?, @RequestBody(required = false) user: UserPartialDTO?): ResponseEntity<OrgInviteDTO> {
        if(creatorId == null) throw UnauthorizedException("User Id was not provided in request headers")

        val invite: OrgInviteDTO = invitationService.createInvitation(organisationId, email, creatorId, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(invite)
    }

    @PostMapping("/accept/{inviteToken}/email/{email}")
    fun acceptInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<OrgMemberDTO> {
        val organisationMember: OrgMemberDTO = invitationService.handleInvitationAccept(inviteToken, email)
        return ResponseEntity.status(HttpStatus.CREATED).body(organisationMember)
    }

    @PostMapping("/reject/{inviteToken}/email/{email}")
    fun rejectInvite(@PathVariable inviteToken: String, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.handleInvitationReject(inviteToken, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/organisation/{organisationId}")
    fun getAllOrganisationInvites(@PathVariable organisationId: UUID,
                                  @RequestParam(name = "inviteStatus", required = false) inviteStatus: OrganisationInvite.InviteStatus?
    ): ResponseEntity<List<OrgInviteDTO>> {
        val invites: List<OrgInviteDTO> = invitationService.getOrganisationInvites(organisationId, inviteStatus)
        return ResponseEntity.ok(invites)
    }

    @GetMapping("/user/{userId}")
    fun getAllUserInvites(@PathVariable userId: UUID,
                          @RequestParam(name = "inviteStatus", required = false) inviteStatus: OrganisationInvite.InviteStatus?
    ): ResponseEntity<List<OrgInviteDTO>> {
        val invites: List<OrgInviteDTO> = invitationService.getUserInvites(userId, inviteStatus)
        return ResponseEntity.ok(invites)
    }

    @DeleteMapping("/organisation/{organisationId}/email/{email}")
    fun revokeUserInvite(@PathVariable organisationId: UUID, @PathVariable email: String): ResponseEntity<Unit> {
        invitationService.revokeInvitation(organisationId, email)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}