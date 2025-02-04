package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import com.yoku.guildmaster.entity.lookups.Permission
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(
    name = "org_position_permissions",
    indexes = [
        Index(name = "idx_org_position_permission_position_id", columnList = "position_id"),
    ]
)
data class OrganisationPositionPermission(

    @EmbeddedId val id: OrganisationPositionPermissionKey,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    val position: OrganisationPosition,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    val permission: OrganisationPermission

) {
    @Embeddable
    data class OrganisationPositionPermissionKey(
        @Column(name = "position_id", nullable = false) val positionId: UUID? = null,
        @Column(name = "permission_id", nullable = false) val permissionId: Int
    ) : Serializable
}
