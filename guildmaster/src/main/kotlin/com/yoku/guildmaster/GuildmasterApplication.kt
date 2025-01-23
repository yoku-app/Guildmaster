package com.yoku.guildmaster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GuildmasterApplication

fun main(args: Array<String>) {
    runApplication<GuildmasterApplication>(*args)
}
