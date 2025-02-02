package com.yoku.guildmaster.entity.lookups

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "lkp_industry")
class Industry (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID,
    @Column(name = "industry_name", nullable = false)
    var name: String,
    @Column(name = "industry_desc", nullable = false)
    var description: String,
    @Column(name ="created_at", nullable = false)
    var createdAt: ZonedDateTime = ZonedDateTime.now(),
    @Column(name ="updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now(),
)