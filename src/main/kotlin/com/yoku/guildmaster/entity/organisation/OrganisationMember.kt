package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import jakarta.persistence.*
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "org_member",
    schema = "organisation",
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

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @MapsId("organisationId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organisation_id", nullable = false)
    val organisation: Organisation,

    @MapsId("positionId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id", nullable = false)
    var position: OrganisationPosition? = null
) {
    @Embeddable
    data class OrganisationMemberKey(
        @Column(name = "organisation_id", nullable = false)
        val organisationId: UUID,

        @Column(name = "user_id", nullable = false)
        val userId: UUID
    ) : Serializable

    fun toDTO(user: UserPartialDTO): OrgMemberDTO {
        return OrgMemberDTO(
            user = user,
            memberSince = this.memberSince,
            organisation = this.organisation.toPartialDTO(),
            position = this.position?.toPartialDTO() ?: throw IllegalStateException("Position should not be null")
        )
    }

}
