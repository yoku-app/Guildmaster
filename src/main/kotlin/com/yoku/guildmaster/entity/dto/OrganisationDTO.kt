package com.yoku.guildmaster.entity.dto

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.yoku.guildmaster.entity.organisation.OrganisationPermission
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.organisation.Permission
import com.yoku.guildmaster.entity.shared.lookups.IndustryLookupDTO
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.ZonedDateTime
import java.util.*

data class OrganisationDTO(
    val id: UUID,
    val industryId: UUID?,
    val creatorId: UUID,
    val name: String,
    val description: String,
    val email: String,
    val memberCount: Int,
    val avatarURL: String?,
    val publicStatus: Boolean,
    val surveyCreationCount: Int,
    val averageSurveyReviewRating: Double,
    val industry: IndustryLookupDTO?,
    val creator: UserPartialDTO?,
)

data class OrganisationPartialDTO(
    val id: UUID,
    val name: String,
    val description: String,
    val avatarURL: String?,
    val publicStatus: Boolean,
)

data class OrgMemberDTO(
    val user: UserPartialDTO?,
    val memberSince: ZonedDateTime,
    val organisation: OrganisationPartialDTO?,
    val position: OrgPositionPartialDTO
)

data class OrgInviteDTO(
    val id: UUID,
    val organisation: OrganisationPartialDTO,
    val user: UserPartialDTO?,
    val email: String,
    val token: String,
    val inviteStatus: OrganisationInvite.InviteStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime
)


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
data class CachedOrgPosition(
    val id: UUID,
    val organisationId: UUID,
    val name: String,
    val rank: Int,
    val permissions: List<OrgPermissionDTO>,
    val isDefault: Boolean
){
    fun toDTO(): OrgPositionDTO = OrgPositionDTO(
        id = this.id,
        name = this.name,
        permissions = this.permissions,
        organisationId = this.organisationId,
        rank = this.rank,
        isDefault = this.isDefault
    )
}

data class OrgPositionDTO(
    val id: UUID,
    val organisationId: UUID,
    val name: String,
    val rank: Int,
    val permissions: List<OrgPermissionDTO>,
    val isDefault: Boolean
)

data class OrgPermissionDTO(
    val id: Int,
    @Enumerated(EnumType.STRING)
    val name: Permission,
    val description: String?,
    val requiresHierarchy: Boolean
)

data class OrgPositionPartialDTO(
    val id: UUID,
    val name: String
)