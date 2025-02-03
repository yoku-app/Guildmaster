package com.yoku.guildmaster.validators;

import com.yoku.guildmaster.entity.lookups.Permission
import com.yoku.guildmaster.repository.lookups.OrganisationPermissionRepository
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component;

/**
 * Validator component on application startup to do the following
 *  - Validate all permissions within the Permission Lookup table match up to the Enum Set used throughout this service
 */

@Component
class PermissionValidator(private val permissionRepository: OrganisationPermissionRepository) {

    private val logger = LoggerFactory.getLogger(PermissionValidator::class.java)

    @PostConstruct
    @Transactional
    fun validatePermissions() {
        val dbPermissions = permissionRepository.findAll().map { it.name }.toSet()
        val enumPermissions = Permission.entries.toSet()

        val missingInDb = enumPermissions - dbPermissions
        val extraInDb = dbPermissions - enumPermissions

        if (missingInDb.isNotEmpty()) {
            logger.error("🚨 Missing permissions in DB: $missingInDb")
        }

        if (extraInDb.isNotEmpty()) {
            logger.warn("⚠️ Extra permissions found in DB that are not in the Enum: $extraInDb")
        } else {
            logger.info("✅ All permissions are properly synced between DB and Enum.")
        }
    }
}
