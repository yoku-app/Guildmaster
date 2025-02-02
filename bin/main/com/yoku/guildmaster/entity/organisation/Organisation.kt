package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.lookups.Industry
import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(
    name = "organisation",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["org_name"]),
        UniqueConstraint(columnNames = ["org_email"])
    ],
    indexes = [
        Index(name = "idx_org_name", columnList = "org_name")
    ]
)
data class Organisation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID,

    @ManyToOne(fetch = )
    @JoinColumn(name = "org_industry_id", nullable = false)
    var industry: Industry,

    @ManyToOne(fetch = FetchType.E)
    @JoinColumn(name = "org_creator_id", nullable = false)
    var creator: UserProfile,

    @Column(name = "org_name", nullable = false, unique = true)
    var name: String,

    @Column(name = "org_desc", nullable = false)
    var description: String,

    @Column(name = "org_email", nullable = false, unique = true)
    var email: String,

    @Column(name = "org_member_count", nullable = false)
    var memberCount: Int = 0,

    @Column(name = "org_avatar_url")
    var avatarURL: String?,

    @Column(name = "org_public_status", nullable = false)
    var publicStatus: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "survey_creation_count", nullable = false)
    var surveyCreationCount: Int = 0,

    @Column(name = "avg_survey_review", nullable = false)
    var averageSurveyReviewRating: Double = 0.0,

    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    val invites: MutableList<OrganisationInvite> = mutableListOf(),

    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    val members: MutableList<OrganisationMember> = mutableListOf()
) {

    fun addMember(member: OrganisationMember) {
        members.add(member)
    }

    fun addInvite(invite: OrganisationInvite) {
        invites.add(invite)
    }
}
