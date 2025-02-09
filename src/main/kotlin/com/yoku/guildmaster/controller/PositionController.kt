package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.exceptions.UnauthorizedException
import com.yoku.guildmaster.service.PositionMemberService
import com.yoku.guildmaster.service.PositionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/position")
class PositionController(private val positionService: PositionService, private val positionMemberService: PositionMemberService) {

    @GetMapping("/organisation/{organisationId}")
    fun getOrganisationPositions(@PathVariable organisationId: UUID): ResponseEntity<List<OrgPositionDTO>>{
        val positions: List<OrgPositionDTO> = positionService.getOrganisationPositions(organisationId)
        return ResponseEntity.ok(positions)
    }

    @GetMapping("/member/{positionId}")
    fun getOrganisationPositionMembers(@PathVariable positionId: UUID): ResponseEntity<List<OrgMemberDTO>>{
        val members: List<OrgMemberDTO> = positionMemberService.getOrganisationPositionMembersWithUserProfile(positionId)
        return ResponseEntity.ok(members)
    }

    @GetMapping("/user/{userId}/organisation/{organisationId}")
    fun getUserPositionWithPermissions(@PathVariable userId: UUID, @PathVariable organisationId: UUID): ResponseEntity<OrgPositionDTO>{
        val position: OrgPositionDTO = positionMemberService.getUserPositionWithPermissions(organisationId, userId)
        return ResponseEntity.ok(position)
    }

    @PostMapping("/")
    fun createNewPosition(@RequestBody position: OrgPositionDTO, @RequestHeader("X-User-Id") originUserId: UUID ): ResponseEntity<OrgPositionDTO>{
        val newPosition: OrgPositionDTO = positionService.createPosition(position, originUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(newPosition)
    }

    @PutMapping("/")
    fun updatePosition(@RequestBody position: OrgPositionDTO, @RequestHeader("X-User-Id") originUserId: UUID ): ResponseEntity<OrgPositionDTO>{
        val updatedPosition: OrgPositionDTO = positionService.updatePosition(position, originUserId)
        return ResponseEntity.ok(updatedPosition)
    }

    @DeleteMapping("{positionId}/newPosition/{newPositionId}")
    fun deletePosition(@PathVariable positionId: UUID,
                       @PathVariable newPositionId: UUID,
                       @RequestHeader("X-User-Id") originUserId: UUID  ): ResponseEntity<Unit>{
        positionService.removePosition(positionId, newPositionId, originUserId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PutMapping("/member/toPosition/{toPositionId}")
    fun moveMemberToPosition(@RequestBody member: OrgMemberDTO,
                             @PathVariable toPositionId: UUID,
                             @RequestHeader("X-User-Id") userId: UUID?): OrgMemberDTO{
        if(userId == null) throw UnauthorizedException("User Id was not provider in request headers")

        return positionService.moveUserToPosition(member, toPositionId, userId)
    }

}