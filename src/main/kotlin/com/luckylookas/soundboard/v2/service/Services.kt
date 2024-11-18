package com.luckylookas.soundboard.v2.service

import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.v2.api.CollectionDto
import com.luckylookas.soundboard.v2.api.FileDto
import com.luckylookas.soundboard.v2.api.Mapper
import com.luckylookas.soundboard.v2.persistence.Collection
import com.luckylookas.soundboard.v2.persistence.CollectionRepository
import com.luckylookas.soundboard.v2.persistence.File
import com.luckylookas.soundboard.v2.persistence.FileRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@Transactional
class FileService(val storage: Storage, val fileRepository: FileRepository, val mapper: Mapper) {

    fun save(name: String, stream: InputStream) =
        ( fileRepository.findByNameEqualsIgnoreCase(name) ?: fileRepository.save(File(name = name))).let {
            storage.save(name, stream)
            mapper.toDto(it)
        }

    fun find(query: String): List<FileDto> = fileRepository.findAllByNameStartsWithOrderByName(query)
        .map { mapper.toDto(it) }

    fun delete(id: Long) = fileRepository.findById(id).ifPresent {
        storage.delete(it.name)
        fileRepository.delete(it)
    }

}

@Service
@Transactional
class CollectionService(
    val collectionRepository: CollectionRepository,
    val fileRepository: FileRepository,
    val mapper: Mapper
) {
    fun save(name: String) = collectionRepository.save(Collection(name = name)).let { mapper.toDto(it) }
    fun find(query: String) = collectionRepository.findAllByNameStartsWithOrderByName(query).map { mapper.toDto(it) }
    fun delete(id: Long) = collectionRepository.deleteById(id)

    fun assign(id: Long, fileId: Long): CollectionDto? = collectionRepository.findById(id).map { collection ->
        fileRepository.findById(fileId).orElse(null)
            ?.let { collection.files.add(it) }
            ?.let { mapper.toDto(collection) }
    }.orElse(null)

    fun remove(id: Long, fileId: Long): CollectionDto? = collectionRepository.findById(id).map { collection ->
        fileRepository.findById(fileId).orElse(null)
            ?.let { collection.files.remove(it) }
            ?.let { mapper.toDto(collection) }
    }.orElse(null)
}


