package com.yoku.guildmaster.controller

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.organisation.OrganisationPosition
import com.yoku.guildmaster.service.PositionMemberService
import com.yoku.guildmaster.service.PositionService
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
    fun getOrganisationPositions(@PathVariable organisationId: UUID): List<OrganisationPosition>{
        return positionService.getOrganisationPositions(organisationId)
    }

    @GetMapping("/member/{positionId}")
    fun getOrganisationPositionMembers(@PathVariable positionId: UUID): List<OrgMemberDTO>{
        return positionMemberService.getOrganisationPositionMembers(positionId)
    }

    @PostMapping("/userId/{userId}")
    fun createNewPosition(position: OrgPositionDTO, @PathVariable userId: UUID ): OrganisationPosition{
        return positionService.createPosition(position, userId)
    }

    @PutMapping("/userId/{userId}")
    fun updatePosition(position: OrgPositionDTO, @PathVariable userId: UUID ): OrganisationPosition{
        return positionService.updatePosition(position, userId)
    }

    @DeleteMapping("position/{positionId}/newPosition/{newPositionId}/userId/{userId}")
    fun deletePosition(@PathVariable positionId: UUID, @PathVariable newPositionId: UUID ,@PathVariable userId: UUID ): Unit{
        return positionService.removePosition(positionId, newPositionId, userId)
    }

    @PutMapping("/member/{memberId}/fromPosition/{fromPositionId}/toPosition/{toPositionId}/userId/{userId}")
    fun moveMemberToPosition(@PathVariable memberId: UUID, @PathVariable fromPositionId: UUID,
                             @PathVariable toPositionId: UUID, @PathVariable userId: UUID): OrgMemberDTO{
        return positionService.moveUserToPosition(memberId, fromPositionId, toPositionId, userId).toDTO()
    }

}