package com.luckylookas.soundboard.periphery


import com.luckylookas.soundboard.SoundFileDto
import com.luckylookas.soundboard.persistence.SoundFile
import com.luckylookas.soundboard.persistence.SoundFileCollection
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class BlobStorage(@Value("\${staticdir}") val staticdir: String) {

    fun getMp3Stream(soundFile: SoundFile): InputStream? {
        return if (Files.exists(Paths.get(staticdir, soundFile.collection.name, soundFile.name)))
            FileInputStream(Paths.get(staticdir, soundFile.collection.name, soundFile.name).toFile()) else null
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

    fun save( handle: SoundFileDto, inputStream: InputStream) {
        Paths.get(staticdir, handle.collection).toFile().mkdirs()
        inputStream.use {
            it.copyTo(Paths.get(staticdir, handle.collection, handle.name).toFile().outputStream())
        }
    }

    fun delete(handle: SoundFileDto) {
         Paths.get(staticdir, handle.collection, handle.name).toFile().let {
             if (it.exists()) {
                 it.delete()
             }
         }
    }

    fun deleteCollection(collection: String) {
        Paths.get(staticdir, collection).toFile().deleteRecursively()
    }
}