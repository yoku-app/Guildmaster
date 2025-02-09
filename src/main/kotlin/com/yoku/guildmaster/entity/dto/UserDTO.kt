package com.yoku.guildmaster.entity.dto

import java.time.ZonedDateTime
import java.util.UUID

data class UserPartialDTO(
    val id: UUID,
    val name: String,
    val dob: ZonedDateTime?,
    val email: String,
    val avatarUrl: String?,
)