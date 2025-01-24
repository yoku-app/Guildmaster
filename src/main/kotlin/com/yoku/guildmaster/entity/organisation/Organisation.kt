package com.yoku.guildmaster.entity.organisation

import com.yoku.guildmaster.entity.lookups.Industry
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "organisations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["org_name"]),
        UniqueConstraint(columnNames = ["org_email"])
    ],
    indexes = [
        Index(name = "idx_org_name", columnList = "org_name"),
    ]
)
class Organisation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "org_industry_id")
    var industry: Industry?,
    @Column(name = "org_creator_id", nullable = false)
    var creatorId: UUID,
    @Column(name = "org_name", nullable = false, unique = true)
    var name: String,
    @Column(name = "org_desc", nullable = false)
    var description: String,
    @Column(name = "org_email", nullable = false, unique = true)
    var email: String,
    @Column(name = "org_member_count", nullable = false)
    var memberCount: Int,
    @Column(name = "org_avtar_url",)
    var avatarURL: String?,
    @Column(name = "org_public_status", nullable = false)
    var publicStatus: Boolean,
)
