package com.yoku.guildmaster.repository

import com.yoku.guildmaster.entity.organisation.OrganisationPositionPermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface OrganisationPositionPermissionRepository: JpaRepository<OrganisationPositionPermission, OrganisationPositionPermission.OrganisationPositionPermissionKey> {
    fun getOrganisationPositionPermissionsByIdPositionId(positionId: UUID): List<OrganisationPositionPermission>

    @Query
        ("""
            SELECT perm FROM OrganisationPositionPermission perm
            LEFT JOIN OrganisationMember mem ON mem.position.id = perm.position.id
            WHERE mem.user.userId = :userId
        """)
    fun getPermissionByUserIdAndPermissionId(userId: UUID): List<OrganisationPositionPermission>
}