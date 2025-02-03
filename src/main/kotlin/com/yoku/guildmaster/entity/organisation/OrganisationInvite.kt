package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgInviteDTO
import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "org_user_invite",
    indexes = [
        Index(name = "idx_invite_user_id", columnList = "user_id"),
        Index(name = "idx_invite_code", columnList = "invite_code")
    ]
)
data class OrganisationInvite(

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    val id: UUID? = null,

    @Version
    @Column(name = "version")
    var version: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisation_id")
    val organisation: Organisation,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: UserProfile?,
    @Column(name = "email")
    val email: String,
    @Column(name = "invite_code", length = 12, nullable = false)
    val token: String,
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var inviteStatus: InviteStatus = InviteStatus.PENDING,
    @Column(name = "expires_at")
    val expiresAt: ZonedDateTime = ZonedDateTime.now().plusDays(7),
    @Column(name = "created_at", updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
    ){

    enum class InviteStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }

    fun toOrganisationMember(user: UserProfile): OrganisationMember{
        val organisationMemberKey = OrganisationMember.OrganisationMemberKey(
            organisationId = this.organisation.id ?: throw IllegalStateException("ID should not be null"),
            userId = user.userId
        )

        return OrganisationMember(
            id = organisationMemberKey,
            user = user,
            organisation = this.organisation
        )
    }

    fun toDTO(): OrgInviteDTO {
        return OrgInviteDTO(
            id = this.id ?: throw IllegalStateException("ID should not be null"),
            organisation = this.organisation.toPartialDTO(),
            user = this.user?.toPartialDTO(),
            email = this.email,
            token = this.token,
            inviteStatus = this.inviteStatus,
            createdAt = this.createdAt,
            expiresAt = this.expiresAt
        )
    }

    fun isInvitationValid(): Boolean{
        return this.expiresAt.isAfter(ZonedDateTime.now()) && this.inviteStatus == InviteStatus.PENDING
    }
}