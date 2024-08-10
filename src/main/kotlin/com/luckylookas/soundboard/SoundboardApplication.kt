package com.luckylookas.soundboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SoundboardApplication

fun main(args: Array<String>) {
    runApplication<SoundboardApplication>(*args)
}
