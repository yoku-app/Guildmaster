package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.exceptions.UnauthorizedException
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

    @GetMapping("/user/{userId}")
    fun getUserOrganisations(@PathVariable userId: UUID): ResponseEntity<List<OrgMemberDTO>>{
        val organisations: List<OrgMemberDTO> = this.memberService.fetchUserOrganisations(userId)
        return ResponseEntity.ok(organisations)
    }

    @DeleteMapping("/user/{userId}/organisation/{organisationId}")
    fun removeMemberFromOrganisation(@PathVariable userId: UUID,
                                     @PathVariable organisationId: UUID,
                                     @RequestHeader("X-User-Id") originUserId: UUID?): ResponseEntity<Unit>{
        if(originUserId == null) throw UnauthorizedException("User Id was not provided in request headers")

        this.memberService.removeMemberFromOrganisation(organisationId, userId, originUserId)
        return ResponseEntity.ok().build()
    }

}