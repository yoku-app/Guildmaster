package com.yoku.guildmaster

import org.springframework.boot.fromApplication


fun main(args: Array<String>) {
    fromApplication<GuildmasterApplication>().run(*args)
}
