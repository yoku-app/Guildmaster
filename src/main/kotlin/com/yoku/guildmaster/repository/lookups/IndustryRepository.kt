package com.yoku.guildmaster.repository.lookups

import com.yoku.guildmaster.entity.lookups.Industry
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IndustryRepository: JpaRepository<UUID, Industry> {
}