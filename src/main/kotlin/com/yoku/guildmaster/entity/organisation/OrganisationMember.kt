package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgMemberDTO
import com.yoku.guildmaster.entity.dto.OrganisationPartialDTO
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import jakarta.persistence.*
import org.hibernate.Hibernate
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

    @MapsId("organisationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    val organisation: Organisation,

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

    fun toDTO(user: UserPartialDTO?, includeOrganisation: Boolean = false): OrgMemberDTO {
        // Force Lazy rendering of Org if requested
        if(includeOrganisation){
            Hibernate.initialize(this.organisation)
        }

        return OrgMemberDTO(
            user = user,
            memberSince = this.memberSince,
            organisation = if (includeOrganisation) this.organisation.toPartialDTO() else null,
            position = this.position?.toPartialDTO() ?: throw IllegalStateException("Position should not be null")
        )
    }

    fun toDTO(user: UserPartialDTO?, organisation: OrganisationPartialDTO): OrgMemberDTO {
        return OrgMemberDTO(
            user = user,
            memberSince = this.memberSince,
            organisation = organisation,
            position = this.position?.toPartialDTO() ?: throw IllegalStateException("Position should not be null")
        )
    }

}
