package com.luckylookas.soundboard.periphery

import java.io.InputStream

interface Storage {
    fun exists(name: String): Boolean
    fun get(name: String): InputStream?
    fun save(name: String, inputStream: InputStream): String
    fun delete(name: String)
}

