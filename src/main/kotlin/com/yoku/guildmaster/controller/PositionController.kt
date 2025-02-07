package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.service.PositionMemberService
import com.yoku.guildmaster.service.PositionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
        val members: List<OrgMemberDTO> = positionMemberService.getOrganisationPositionMembers(positionId)
        return ResponseEntity.ok(members)
    }

    @GetMapping("/user/{userId}/organisation/{organisationId}")
    fun getUserPositionWithPermissions(@PathVariable userId: UUID, @PathVariable organisationId: UUID): OrgPositionDTO{
        return positionMemberService.getUserPositionWithPermissions(organisationId, userId).toDTO()
    }

    @PostMapping("/userId/{userId}")
    fun createNewPosition(position: OrgPositionDTO, @PathVariable userId: UUID ): ResponseEntity<OrgPositionDTO>{
        val newPosition: OrgPositionDTO = positionService.createPosition(position, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(newPosition)
    }

    @PutMapping("/userId/{userId}")
    fun updatePosition(position: OrgPositionDTO, @PathVariable userId: UUID ): ResponseEntity<OrgPositionDTO>{
        val updatedPosition: OrgPositionDTO = positionService.updatePosition(position, userId)
        return ResponseEntity.ok(updatedPosition)
    }

    @DeleteMapping("position/{positionId}/newPosition/{newPositionId}/userId/{userId}")
    fun deletePosition(@PathVariable positionId: UUID, @PathVariable newPositionId: UUID ,@PathVariable userId: UUID ): ResponseEntity<Unit>{
        positionService.removePosition(positionId, newPositionId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PutMapping("/member/{memberId}/fromPosition/{fromPositionId}/toPosition/{toPositionId}/userId/{userId}")
    fun moveMemberToPosition(@PathVariable memberId: UUID, @PathVariable fromPositionId: UUID,
                             @PathVariable toPositionId: UUID, @PathVariable userId: UUID): OrgMemberDTO{
        return positionService.moveUserToPosition(memberId, fromPositionId, toPositionId, userId).toDTO()
    }

}