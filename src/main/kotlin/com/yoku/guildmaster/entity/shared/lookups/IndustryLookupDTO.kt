package com.yoku.guildmaster.entity.shared.lookups

import java.util.UUID

data class IndustryLookupDTO(
    val id: UUID,
    val name: String,
    val description: String
)