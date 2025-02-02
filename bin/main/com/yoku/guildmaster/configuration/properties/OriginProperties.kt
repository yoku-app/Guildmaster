package com.yoku.guildmaster.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
data class OriginProperties(val origins: List<String>)
