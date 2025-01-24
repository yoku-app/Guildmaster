package com.yoku.guildmaster.configuration

import com.yoku.guildmaster.configuration.properties.OriginProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.cors.CorsConfiguration as DefaultCorsConfiguration

@Configuration
class CorsConfiguration(private val originProperties: OriginProperties) {

    @Bean
    fun corsFilter(): CorsFilter {
        val corsConfig = DefaultCorsConfiguration()
        corsConfig.allowedOrigins = originProperties.origins
        corsConfig.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        corsConfig.allowedMethods = listOf("*")
        corsConfig.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsFilter(source)
    }

}