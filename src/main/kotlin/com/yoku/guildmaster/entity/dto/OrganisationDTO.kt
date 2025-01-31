package com.yoku.guildmaster.entity.dto

import com.yoku.guildmaster.entity.lookups.Industry
import com.yoku.guildmaster.entity.organisation.OrganisationInvite
import com.yoku.guildmaster.entity.user.UserProfile
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
    val industry: Industry?, // Industry of the organisation
    val creator: UserProfile?, // Creator of the organisation
)

data class OrgMemberDTO(
    val id: UUID,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
)

data class OrgInviteDTO(
    val id: UUID,
    val organisationId: UUID,
    val email: String,
    val token: String,
    val status: OrganisationInvite.InviteStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime
)