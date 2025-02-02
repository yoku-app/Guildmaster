package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.dto.OrgPositionDTO
import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "org_position",
    uniqueConstraints = [UniqueConstraint(columnNames = ["organisation_id", "name"])],
    indexes = [
        Index(name = "idx_org_position_organisation_id", columnList = "organisation_id"),
    ]
)
data class OrganisationPosition(
    @Id @GeneratedValue @Column(
        columnDefinition = "UUID DEFAULT uuid_generate_v4()",
        updatable = false,
        nullable = false
    ) val id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(
        name = "organisation_id",
        nullable = false
    ) val organisation: Organisation,

    @Column(nullable = false) val name: String,

    @Column(name = "is_default", nullable = false) val isDefault: Boolean = false,

    @Column(nullable = false) val rank: Int,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
    name = "org_position_permissions",
    joinColumns = [JoinColumn(name = "position_id")],
    inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: List<OrganisationPermission> = mutableListOf()

){
    fun toDTO(): OrgPositionDTO {
        return OrgPositionDTO(
            id = this.id ?: UUID.randomUUID(),
            name = this.name,
        )
    }
}
