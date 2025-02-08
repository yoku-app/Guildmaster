package com.yoku.guildmaster.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "server")
data class ServiceConnectionConfigurationProperties(
   val origins: List<String>,
   val connections: Map<TargetController, String>
){
    enum class TargetController{
        COLOVIA,
        SECUNDA
    }
}

