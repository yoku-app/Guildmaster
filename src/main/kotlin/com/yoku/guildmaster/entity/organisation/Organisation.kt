package com.yoku.guildmaster.entity.organisation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.yoku.guildmaster.entity.dto.OrganisationDTO
import com.yoku.guildmaster.entity.dto.OrganisationPartialDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import com.yoku.guildmaster.entity.shared.lookups.IndustryLookupDTO
import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(
    name = "organisation",
    schema = "organisation",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["org_name"]),
        UniqueConstraint(columnNames = ["org_email"])
    ],
    indexes = [
        Index(name = "idx_org_name", columnList = "org_name")
    ]
)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
data class Organisation(

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "org_industry_id", nullable = false)
    var industryId: UUID?,

    @Column(name = "org_creator_id", nullable = false)
    var creatorId: UUID,

    @Column(name = "org_name", nullable = false, unique = true)
    var name: String,

    @Column(name = "org_desc", nullable = false)
    var description: String,

    @Column(name = "org_email", nullable = false, unique = true)
    var email: String,

    @Column(name = "org_member_count", nullable = false, updatable = false)
    val memberCount: Int = 0,

    @Column(name = "org_avatar_url")
    var avatarURL: String?,

    @Column(name = "org_public_status", nullable = false)
    var publicStatus: Boolean = false,

    @Column(name = "org_type")
    @Enumerated(EnumType.STRING)
    var orgType: OrganisationType = OrganisationType.PERSONAL,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "org_surveys_created", nullable = false,  updatable = false)
    val surveyCreationCount: Int = 0,

    @Column(name = "avg_survey_review", nullable = false,  updatable = false)
    val averageSurveyReviewRating: Double = 0.0,

    @JsonIgnore
    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    val invites: MutableList<OrganisationInvite> = mutableListOf(),

    @JsonIgnore
    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    val members: MutableList<OrganisationMember> = mutableListOf()
) {

    enum class OrganisationType{
        PERSONAL,
        COMPANY,
        EDUCATIONAL
    }

    fun toDTO(creator: UserPartialDTO?, industry: IndustryLookupDTO?): OrganisationDTO {
        return OrganisationDTO(
            id = this.id ?: throw IllegalStateException("ID should not be null"),
            creatorId = this.creatorId,
            industryId = this.industryId,
            name = this.name,
            description = this.description,
            email = this.email,
            memberCount = this.memberCount,
            orgType = this.orgType,
            avatarURL = this.avatarURL,
            publicStatus = this.publicStatus,
            surveyCreationCount = this.surveyCreationCount,
            averageSurveyReviewRating = this.averageSurveyReviewRating,
            industry = industry,
            creator = creator
        )
    }

    fun toDTO(): OrganisationDTO {
        return OrganisationDTO(
            id = this.id ?: throw IllegalStateException("ID should not be null"),
            creatorId = this.creatorId,
            industryId = this.industryId,
            name = this.name,
            description = this.description,
            email = this.email,
            orgType = this.orgType,
            memberCount = this.memberCount,
            avatarURL = this.avatarURL,
            publicStatus = this.publicStatus,
            surveyCreationCount = this.surveyCreationCount,
            averageSurveyReviewRating = this.averageSurveyReviewRating,
            creator = null,
            industry = null
        )
    }

    fun toPartialDTO(): OrganisationPartialDTO {
        return OrganisationPartialDTO(
            id = this.id ?: throw IllegalStateException("ID should not be null"),
            name = this.name,
            orgType = this.orgType,
            description = this.description,
            avatarURL = this.avatarURL,
            publicStatus = this.publicStatus
        )
    }
}
