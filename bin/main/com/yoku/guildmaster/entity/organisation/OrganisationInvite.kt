package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(
    name = "org_user_invite",
    indexes = [
        Index(name = "idx_invite_user_id", columnList = "user_id"),
        Index(name = "idx_invite_invite_token", columnList = "invite_token")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "unique_invite", columnNames = ["email", "organisation_id", "invite_code"])
    ]
)
data class OrganisationInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    val organisation: Organisation,
    @ManyT
    val userId: UUID?,
    @Column(name = "email")
    val email: String,
    @Column(name = "invite_code", length = 12, nullable = false)
    val token: String,
    @Column(name = "status")
    var inviteStatus: InviteStatus = InviteStatus.PENDING,
    @Column(name = "expires_at")
    val expiresAt: ZonedDateTime = ZonedDateTime.now().plusDays(7),
    @Column(name = "created_at", updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    ){

    enum class InviteStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }

    fun toOrganisationMember(user: UserProfile): OrganisationMember{
        val organisationMemberKey = OrganisationMember.OrganisationMemberKey(
            organisationId = this.organisation.id,
            userId = user.userId
        )

        return OrganisationMember(
            id = organisationMemberKey,
            user = user,
        )
    }

    fun setInviteStatus(status: InviteStatus): Unit{
        this.inviteStatus = status
    }

    fun isInvitationValid(): Boolean{
        return this.expiresAt.isAfter(ZonedDateTime.now()) && this.inviteStatus == InviteStatus.PENDING
    }

}