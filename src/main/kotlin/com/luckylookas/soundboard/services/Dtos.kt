package com.luckylookas.soundboard.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.luckylookas.soundboard.persistence.*
import org.springframework.stereotype.Component

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FileDto(val id: Long? = null, val name: String, val volume: Long, val loop: Boolean)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AdventureDto(val id: Long? = null, val name: String, val scenes: List<SceneDto> = listOf())
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class SceneDto(val id: Long? = null, val name: String, val outputs: List<OutputDto> = listOf())
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OutputDto(val id: Long? = null, val name: String, val files: List<FileDto> = listOf(), val playOnStart: FileDto? = null, val volume: Long = 100, val devices: List<SoundDeviceDto> = listOf())
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class SoundDeviceDto(val id: Long? = null, val name: String, val currentlyPlaying: FileDto? = null, val volume: Long = 100, val outputs: List<OutputDto> = listOf())

@Component
class Mapper {
    fun toDto(entity: File) = FileDto(id = entity.id, name = entity.name, volume = entity.volume, loop = entity.loop)
    fun toDto(entity: Adventure) =
        AdventureDto(id = entity.id, name = entity.name, scenes = entity.scenes.map { toDto(it) }.sortedBy { it.name }.toList())

    fun toDto(entity: SoundDevice) = SoundDeviceDto(name = entity.name,
        id = entity.id,
        currentlyPlaying = entity.currentlyPlaying?.let { toDto(it) },
        volume = entity.volume
    )
    fun toDto(entity: Scene) =
        SceneDto(id = entity.id, name = entity.name, outputs = entity.outputs.map { toDto(it) }.sortedBy { it.name }.toList())

    fun toDto(entity: Output) = OutputDto(id = entity.id, name = entity.name, files = entity.files.map { toDto(it) }.sortedBy { it.name }.toList(),
        playOnStart = entity.playOnStart?.let { toDto(it) },
        volume = entity.volume,
        devices = entity.soundDevices.map { toDto(it) }.sortedBy { it.name }.toList())

    fun fromDto(dto: OutputDto) = Output(id = dto.id, name = dto.name, volume = dto.volume)
    fun fromDto(dto: SceneDto, adventure: Adventure) = Scene(
        id = dto.id,
        name = dto.name,
        adventure = adventure,
        outputs = dto.outputs.map { fromDto(it) }.toMutableSet()
    )
}
