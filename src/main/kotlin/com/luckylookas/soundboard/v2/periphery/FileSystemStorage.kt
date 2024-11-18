package com.luckylookas.soundboard.v2.periphery

import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.persistence.SoundFile
import com.luckylookas.soundboard.persistence.SoundFileCollection
import com.luckylookas.soundboard.v2.api.FileDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@Component
class FileSystemStorage(@Value("\${staticdir}") val staticdir: String): Storage {

    fun getMp3Stream(fileDto: FileDto): InputStream? {
        return if (Files.exists(Paths.get(staticdir, fileDto.name)))
            FileInputStream(Paths.get(staticdir, fileDto.name).toFile()) else null
    }

    fun scan(): List<SoundFileCollection> =
        File(staticdir).listFiles()
            ?.map { collectionName ->  SoundFileCollection(name = collectionName.name) }
            ?.map { collection ->
                Path.of(staticdir, collection.name).toFile().listFiles()?.let {
                    collection.soundFiles.addAll(
                        it.map { file -> SoundFile(name = file.name, collection = collection) }
                    )
                }
                return@map collection
            }.orEmpty().toList()


    override fun save(name: String, inputStream: InputStream) {
        inputStream.copyTo(Paths.get(staticdir, name).toFile().outputStream())
    }

    override fun delete(name: String) {
        Files.delete(Paths.get(staticdir, name))
    }

}