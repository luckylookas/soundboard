package com.luckylookas.soundboard.persistence

import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.services.FileDto

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths


@Component
class FileSystemStorage(@Value("\${staticdir}") val staticdir: String): Storage {
    override fun exists(name: String): Boolean {
       return Files.exists(Paths.get(staticdir, "${name.substringBefore(".")}.mp3"))
    }

    override fun get(name: String): InputStream? {
        return if (Files.exists(Paths.get(staticdir, "${name.substringBefore(".")}.mp3")))
            FileInputStream(Paths.get(staticdir, "${name.substringBefore(".")}.mp3").toFile()) else null
    }

    override fun save(name: String, inputStream: InputStream): String {
        inputStream.use { ins ->
            Paths.get(staticdir, "${name.substringBefore(".")}.mp3").toFile().outputStream().use { out ->
                ins.copyTo(out)
            }
        }
        return name.substringBefore(".")
    }

    override fun delete(name: String) {
        Files.delete(Paths.get(staticdir, name.substringBefore(".")))
    }

}