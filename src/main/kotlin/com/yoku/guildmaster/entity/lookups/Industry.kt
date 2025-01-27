package com.yoku.guildmaster.entity.lookups

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

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
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name ="updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)