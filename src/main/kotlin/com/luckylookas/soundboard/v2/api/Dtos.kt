package com.luckylookas.soundboard.v2.api

import com.luckylookas.soundboard.v2.persistence.Collection
import com.luckylookas.soundboard.v2.persistence.File
import org.springframework.stereotype.Component

class FileDto (val id: Long?, val name: String)
class CollectionDto (val id: Long?, val name: String, val files: Set<FileDto>)
class AdventureDto (val id: Long?,val name: String, val scenes: Set<SceneDto>)
class SceneDto (val id: Long?,val name: String, val outputs: Set<OutputDto>)
class OutputDto (val id: Long?,val name: String)


@Component
class Mapper {
    fun toDto(entity: File) = FileDto(id = entity.id ,name = entity.name)
    fun toDto(entity: Collection) = CollectionDto(id = entity.id, name = entity.name, files = entity.files.map { toDto(it) }.toSet())
}
