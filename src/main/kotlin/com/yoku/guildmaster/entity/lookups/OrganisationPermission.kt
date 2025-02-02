package com.yoku.guildmaster.entity.lookups;

import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "lkp_org_permissions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["permission_name"])],
    indexes = [Index(name = "idx_org_permission_name", columnList = "permission_name")]
)
data class OrganisationPermission(
    @Id @GeneratedValue @Column(
        columnDefinition = "UUID DEFAULT uuid_generate_v4()",
        updatable = false,
        nullable = false
    ) val id: UUID = UUID.randomUUID(),

    @Column(name = "permission_name", nullable = false, unique = true) val name: String,

    @Column(name = "permission_description") val description: String? = null,

    @Column(name = "requires_hierarchy", nullable = false) val requiresHierarchy: Boolean = false
)

enum class Permission{
    ORGANISATION_EDIT,
    ORGANISATION_DELETE,
    ORGANISATION_VIEW_BILLING,
    ORGANISATION_MANAGE_BILLING,
    MEMBER_INVITE,
    MEMBER_REMOVE,
    MEMBER_UPDATE_ROLE,
    ROLE_CREATE,
    ROLE_DELETE,
    ROLE_UPDATE,
    ROLE_ASSIGN_PERMISSION,
    SURVEY_CREATE,
    SURVEY_DELETE,
    SURVEY_EDIT,
    SURVEY_VIEW_RESULTS,
    AUDIT_VIEW,
    AUDIT_DOWNLOAD
}
