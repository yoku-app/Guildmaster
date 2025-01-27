package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.organisation.OrgMemberDTO
import com.yoku.guildmaster.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/member")
class MemberController(private val memberService: MemberService) {

    @GetMapping("/organisation/{id}")
    fun getOrganisationMembers(@PathVariable id: UUID): ResponseEntity<List<OrgMemberDTO>>{
        val members: List<OrgMemberDTO> = this.memberService.fetchOrganisationMembers(id)
        return ResponseEntity.ok(members)
    }

    @DeleteMapping("{userId}/organisation/{organisationId}")
    fun removeMemberFromOrganisation(@PathVariable userId: UUID, @PathVariable organisationId: UUID, @RequestHeader("X-User-ID") requesterUserId: UUID): ResponseEntity<Unit>{
        this.memberService.removeMemberFromOrganisation(organisationId, userId, requesterUserId)
        return ResponseEntity.ok().build()
    }

}