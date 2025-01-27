package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "org_user_invite",
    indexes = [
        Index(name = "idx_invite_user_id", columnList = "user_id"),
        Index(name = "idx_invite_invite_token", columnList = "invite_token")
    ]
)
data class OrganisationInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    val organisation: Organisation,
    @Column(name = "user_id")
    val userId: UUID?,
    @Column(name = "email")
    val email: String,
    @Column(name = "invite_code", length = 12, nullable = false)
    val token: String,
    @Column(name = "status")
    var inviteStatus: InviteStatus = InviteStatus.PENDING,
    @Column(name = "expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
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

    fun isInvitationValid(): Boolean{
        return this.expiresAt.isAfter(LocalDateTime.now()) && this.inviteStatus == InviteStatus.PENDING
    }
}