package com.yoku.guildmaster.service.external

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.yoku.guildmaster.configuration.properties.ServiceConnectionConfigurationProperties
import com.yoku.guildmaster.configuration.properties.ServiceConnectionConfigurationProperties.TargetController
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class HttpService(
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val serviceProperties: ServiceConnectionConfigurationProperties
) {

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    private fun getServiceURL(target: TargetController): String{
        return when(target){
            TargetController.COLOVIA -> serviceProperties.connections[TargetController.COLOVIA]
            TargetController.SECUNDA -> serviceProperties.connections[TargetController.SECUNDA]
        } ?: throw IllegalArgumentException("Target controller not found")
    }

    fun generateInternalServiceConnection(target: TargetController, endpoint: String): Request.Builder{
        return Request.Builder()
            .url("${getServiceURL(target)}$endpoint")
    }

    fun generateInternalServiceConnection(target: TargetController, urlBuilder: (String) -> HttpUrl): Request.Builder{
        val url: HttpUrl = urlBuilder(getServiceURL(target))

        return Request.Builder()
            .url(url)
    }

    fun <T> get (builder: Request.Builder, responseType: Class<T>): T?{
        val request: Request = builder.get().build()
        return executeRequest(request, responseType)
    }

    fun <T> get (builder: Request.Builder, responseType: TypeReference<T>): T?{
        val request: Request = builder.get().build()
        return executeRequest(request, responseType)
    }

    fun <T> get(url: String, responseType: Class<T>): T? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return executeRequest(request, responseType)
    }

    fun <T> get(url: String, responseType: TypeReference<T>): T? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return executeRequest(request, responseType)
    }

    fun <T, V> post (builder: Request.Builder, body: V, responseType: Class<T>): T?{
        val requestBody = objectMapper.writeValueAsString(body).toRequestBody(mediaTypeJson)
        val request = builder.post(requestBody).build()
        return executeRequest(request, responseType)
    }

    fun <T, V> post(url: String, body: V, responseType: Class<T>): T? {
        val requestBody = objectMapper.writeValueAsString(body).toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return executeRequest(request, responseType)
    }

    fun <T, V> put(url: String, body: V, responseType: Class<T>): T? {
        val requestBody = objectMapper.writeValueAsString(body).toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        return executeRequest(request, responseType)
    }

    private fun <T> executeRequest(request: Request, responseType: Class<T>): T? {
        return executeAndParseResponse<TypeReference<T>, T>(
            request = request,
            parser = { response -> objectMapper.readValue(response, responseType) }
        )
    }

    private fun <T> executeRequest(request: Request, responseType: TypeReference<T>): T? {
        return executeAndParseResponse<TypeReference<T>, T>(
            request = request,
            parser = { response -> objectMapper.readValue(response, responseType) }
        )
    }

    private fun <T,V> executeAndParseResponse(request: Request, parser: (String) -> V? ): V? {
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            response.body?.string()?.let {
                return parser(it)
            }
        }
        return null
    }
}
