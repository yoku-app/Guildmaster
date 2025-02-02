package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.Organisation
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface OrganisationInviteRepository: JpaRepository<OrganisationInvite, UUID> {
    fun findByToken(inviteToken: String): Optional<OrganisationInvite>
    @Query("SELECT i FROM OrganisationInvite i WHERE i.organisation.id = :organisationId")
    fun findByOrganisationId (organisationId: UUID): List<OrganisationInvite>
    @Query("SELECT i FROM OrganisationInvite i WHERE i.user.userId = :userId")
    fun findByUserId(userId: UUID): List<OrganisationInvite>
    fun findByOrganisationAndEmailAndInviteStatus(organisation: Organisation, email: String, inviteStatus: OrganisationInvite.InviteStatus): Optional<OrganisationInvite>
    @Query("SELECT i FROM OrganisationInvite i WHERE i.organisation.id = :organisationId AND i.inviteStatus = :inviteStatus")
    fun findByOrganisationIdAndStatus(organisationId: UUID, inviteStatus: OrganisationInvite.InviteStatus): List<OrganisationInvite>
    @Query("SELECT i FROM OrganisationInvite i WHERE i.user.userId = :userId AND i.inviteStatus = :inviteStatus")
    fun findByUserIdAndStatus(userId: UUID, inviteStatus: OrganisationInvite.InviteStatus): List<OrganisationInvite>
}