package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.user.UserProfile
import jakarta.persistence.*
import java.io.Serializable
import java.time.ZonedDateTime
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
    val memberSince: ZonedDateTime = ZonedDateTime.now(),

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserProfile,

    @MapsId("organisationId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisation_id", nullable = false)
    val organisation: Organisation
) {
    @Embeddable
    data class OrganisationMemberKey(
        @Column(name = "organisation_id", nullable = false)
        val organisationId: UUID,

        @Column(name = "user_id", nullable = false)
        val userId: UUID
    ) : Serializable

    fun toDTO(): OrgMemberDTO {
        return OrgMemberDTO(
            id = this.id.userId,
            displayName = this.user.displayName,
            email = this.user.email,
            avatarUrl = this.user.avatarUrl,
            memberSince = this.memberSince,
            organisation = this.organisation.toPartialDTO()
        )
    }

    fun hasPermission(): Boolean {
        return true
    }

}
