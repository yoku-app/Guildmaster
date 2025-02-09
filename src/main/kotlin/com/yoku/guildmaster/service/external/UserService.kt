package com.yoku.guildmaster.service.external

import com.fasterxml.jackson.core.type.TypeReference
import com.yoku.guildmaster.configuration.properties.ServiceConnectionConfigurationProperties
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val httpService: HttpService) {

    val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Communicating with Colovia to fetch a user's profile based on the provided email, handling instances where the profile should not exist
     * @param email The email of the user to fetch the profile for
     *
     * @return UserProfile object if the user exists, null if the user does not exist
     */
    fun fetchUserProfileFromEmail(email: String): UserPartialDTO? {
        try{
            val request: Request.Builder = httpService.generateInternalServiceConnection(
                target = ServiceConnectionConfigurationProperties.TargetController.COLOVIA,
                endpoint = "user/email/$email"
            )
            return httpService.get(request, UserPartialDTO::class.java)
        } catch(e: Exception){
            logger.error("Failed to fetch user profile", e)
            return null
        }
    }

    /**
     *
     */
    fun fetchBatchUsersByIds(userIds: List<UUID>): Map<UUID, UserPartialDTO?> {
        try{
            val request: Request.Builder = httpService.generateInternalServiceConnection(
                target = ServiceConnectionConfigurationProperties.TargetController.COLOVIA,
                urlBuilder =
                { url ->
                    val baseUrl: HttpUrl = url.toHttpUrlOrNull()?.resolve("user/display/ids")
                        ?: throw Exception("Failed to parse base URL")
                    baseUrl.newBuilder()
                    .addQueryParameter("userIds", userIds.joinToString(","))
                    .build()
                }
            )

            val response: Map<UUID, UserPartialDTO?>? =
                httpService.get(request, object: TypeReference<Map<UUID, UserPartialDTO?>>() {})

            return response ?: throw Exception("Failed to fetch user profiles")

        } catch(e: Exception){
            logger.error("Failed to fetch user profiles", e)
            return emptyMap()
        }

    }
}