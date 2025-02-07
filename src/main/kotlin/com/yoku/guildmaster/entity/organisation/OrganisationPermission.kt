package com.yoku.guildmaster.entity.organisation

import jakarta.persistence.*

@Entity
@Table(
    name = "lkp_org_permissions",
    schema = "organisation",
    uniqueConstraints = [UniqueConstraint(columnNames = ["permission_name"])],
    indexes = [Index(name = "idx_org_permission_name", columnList = "permission_name")]
)
data class OrganisationPermission(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: Int,  // Using static ID instead of UUID

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_name", nullable = false, unique = true)
    val name: Permission,  // Enum now maps by ID

    @Column(name = "permission_description")
    val description: String? = null,

    @Column(name = "requires_hierarchy", nullable = false)
    val requiresHierarchy: Boolean = false
)

enum class Permission(val id: Int) {
    ORGANISATION_EDIT(1),
    ORGANISATION_DELETE(2),
    ORGANISATION_VIEW_BILLING(3),
    ORGANISATION_MANAGE_BILLING(4),
    MEMBER_INVITE(5),
    MEMBER_REMOVE(6),
    MEMBER_UPDATE_ROLE(7),
    ROLE_CREATE(8),
    ROLE_DELETE(9),
    ROLE_UPDATE(10),
    ROLE_ASSIGN_PERMISSION(11),
    SURVEY_CREATE(12),
    SURVEY_EDIT(13),
    SURVEY_DELETE(14),
    SURVEY_VIEW_RESULTS(15),
    AUDIT_VIEW(16),
    AUDIT_DOWNLOAD(17);

    companion object {
        fun fromId(id: Int): Permission? = entries.find { it.id == id }
    }
}
