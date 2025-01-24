package com.yoku.guildmaster.entity.organisation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Organisation(
    @Id
    var id: String,
    @Column(name = "org_name", nullable = false)
    var name: String,
    @Column(name = "org_desc", nullable = false)
    var description: String,
    @Column(name = "org_member_count", nullable = false)
    var memberCount: Int,
    @Column(name = "org_avtar_url",)
    var avtarUrl: String,
    @Column(name = "org_public_status", nullable = false)
    var publicStatus: Boolean,
)
