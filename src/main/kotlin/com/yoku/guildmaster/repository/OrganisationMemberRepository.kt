package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.user.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*

interface OrganisationMemberRepository: JpaRepository<OrganisationMember, OrganisationMember.OrganisationMemberKey>{
    fun findByIdOrganisationId(organisationId: UUID): List<OrganisationMember>
    fun findByIdUserId(userId: UUID): List<OrganisationMember>
    fun findByUser(user: UserProfile): List<OrganisationMember>
    fun findByPositionId(positionId: UUID): List<OrganisationMember>

    @Modifying
    @Query("UPDATE OrganisationMember om SET om.position = (SELECT op FROM OrganisationPosition op WHERE op.id = :newPositionId) WHERE om.position.id = :positionId")
    fun updateByPositionId(positionId: UUID, newPositionId: UUID): Int

}