package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.dto.OrgPositionPartialDTO
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "org_position",
    schema = "organisation",
    uniqueConstraints = [UniqueConstraint(columnNames = ["organisation_id", "name"])],
    indexes = [
        Index(name = "idx_org_position_organisation_id", columnList = "organisation_id"),
    ]
)
data class OrganisationPosition(
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "organisation_id", nullable = false)
    val organisationId: UUID,

    @Column(nullable = false)
    var name: String,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @Column(nullable = false)
    var rank: Int,
) {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "org_position_permissions",
        joinColumns = [JoinColumn(name = "position_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    var permissions: MutableList<OrganisationPermission> = mutableListOf()

    fun toDTO(): OrgPositionDTO = OrgPositionDTO(
        id = this.id ?: throw IllegalStateException("ID should not be null"),
        name = this.name,
        permissions = this.permissions,
        organisationId = this.organisationId,
        rank = this.rank,
        isDefault = this.isDefault
    )

    fun toPartialDTO(): OrgPositionPartialDTO = OrgPositionPartialDTO(
        id = this.id ?: throw IllegalStateException("ID should not be null"),
        name = this.name
    )
}
