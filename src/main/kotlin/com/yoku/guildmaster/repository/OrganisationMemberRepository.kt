package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationMember
import com.yoku.guildmaster.entity.user.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OrganisationMemberRepository: JpaRepository<OrganisationMember, OrganisationMember.OrganisationMemberKey>{
    fun findByIdOrganisationId(organisationId: UUID): List<OrganisationMember>
    fun findByUser(user: UserProfile): List<OrganisationMember>
}