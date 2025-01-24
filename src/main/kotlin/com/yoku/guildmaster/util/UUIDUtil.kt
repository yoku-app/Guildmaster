package com.yoku.guildmaster.util

import com.yoku.guildmaster.exceptions.InvalidArgumentException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.Throws

@Component
class UUIDUtil {

    private val logger: Logger = LoggerFactory.getLogger(UUIDUtil::class.java)

    @Throws(InvalidArgumentException::class)
    fun parseUUID(uuid: String): UUID {
        return try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid UUID format: $uuid", e)
            throw InvalidArgumentException("Invalid UUID format")
        }
    }
}