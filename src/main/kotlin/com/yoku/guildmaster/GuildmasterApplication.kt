package com.yoku.guildmaster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
class GuildmasterApplication
fun main(args: Array<String>) {
    runApplication<GuildmasterApplication>(*args)
}
