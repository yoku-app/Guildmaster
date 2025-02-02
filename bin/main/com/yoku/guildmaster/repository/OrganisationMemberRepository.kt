package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationMember
import org.springframework.data.jpa.repository.JpaRepository

interface OrganisationMemberRepository: JpaRepository<OrganisationMember, OrganisationMember.OrganisationMemberKey>{

}