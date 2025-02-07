package com.yoku.guildmaster.entity.dto

import com.yoku.guildmaster.entity.organisation.OrganisationPermission
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.shared.lookups.IndustryLookupDTO
import java.time.ZonedDateTime
import java.util.*

data class OrganisationDTO(
    val id: UUID, // Unique identifier for the organisation
    val name: String, // Name of the organisation
    val description: String, // Description of the organisation
    val email: String, // Email of the organisation
    val memberCount: Int, // Number of members in the organisation
    val avatarURL: String?, // URL for the organisation's avatar (nullable)
    val publicStatus: Boolean, // Public status of the organisation
    val surveyCreationCount: Int, // Number of surveys created by the organisation
    val averageSurveyReviewRating: Double, // Average rating of surveys created by the organisation
    val industry: IndustryLookupDTO?, // Industry of the organisation
    val creator: UserPartialDTO?, // Creator of the organisation
)

data class OrganisationPartialDTO(
    val id: UUID,
    val name: String,
    val description: String,
    val avatarURL: String?,
    val publicStatus: Boolean,
)

data class OrgMemberDTO(
    val user: UserPartialDTO,
    val memberSince: ZonedDateTime,
    val organisation: OrganisationPartialDTO,
    val position: OrgPositionPartialDTO?
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

data class OrgPositionDTO(
    val id: UUID,
    val organisationId: UUID,
    val name: String,
    val rank: Int,
    val permissions: List<OrganisationPermission>,
    val isDefault: Boolean
)

data class OrgPositionPartialDTO(
    val id: UUID,
    val name: String
)