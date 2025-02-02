package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.lookups.OrganisationPermission
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "org_position_permissions")
data class OrganisationPositionPermission(
    @Id
    @GeneratedValue
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    val position: OrganisationPosition,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    val permission: OrganisationPermission
)
