package com.luckylookas.soundboard.periphery

import java.io.InputStream

interface Storage {
    fun save(name: String, inputStream: InputStream)
    fun delete(name: String)
}

