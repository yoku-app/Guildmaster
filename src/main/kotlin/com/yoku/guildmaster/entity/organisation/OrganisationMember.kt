package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Entity
@Table(
    name = "org_member",
    indexes = [
        Index(name = "idx_member_user_id", columnList = "user_id"),
        Index(name = "idx_member_organisation_id", columnList = "organisation_id")
    ]
)
data class OrganisationMember(
    @EmbeddedId
    val id: OrganisationMemberKey,

    @Column(name = "member_since", nullable = false, updatable = false)
    val memberSince: LocalDateTime = LocalDateTime.now(),

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserProfile
) {
    @Embeddable
    data class OrganisationMemberKey(
        @Column(name = "organisation_id", nullable = false)
        val organisationId: UUID,

        @Column(name = "user_id", nullable = false)
        val userId: UUID
    ) : Serializable

}
