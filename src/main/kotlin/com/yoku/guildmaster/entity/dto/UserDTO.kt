package com.yoku.guildmaster.entity.dto

import java.util.UUID

data class UserProfilePartialDTO(
    val userId: UUID,
    val displayName: String,
    val email: String,
    val avatarUrl: String?
)