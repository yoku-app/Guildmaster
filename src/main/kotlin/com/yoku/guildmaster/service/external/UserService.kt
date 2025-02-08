package com.yoku.guildmaster.service.external

import com.fasterxml.jackson.core.type.TypeReference
import com.yoku.guildmaster.configuration.properties.ServiceConnectionConfigurationProperties
import com.yoku.guildmaster.entity.dto.UserPartialDTO
import okhttp3.HttpUrl
import okhttp3.Request
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val httpService: HttpService) {

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
                { url -> HttpUrl.Builder()
                    .encodedPath(url)
                    .addPathSegment("user/display/ids")
                    .addQueryParameter("ids", userIds.joinToString(","))
                    .build()}
            )

            val response: Map<UUID, UserPartialDTO?>? =
                httpService.get(request, object: TypeReference<Map<UUID, UserPartialDTO?>>() {})

            return response ?: throw Exception("Failed to fetch user profiles")

        } catch(e: Exception){
            return emptyMap()
        }

    }
}