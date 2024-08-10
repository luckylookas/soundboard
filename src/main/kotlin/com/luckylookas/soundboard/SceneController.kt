package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.OutputRepository
import com.luckylookas.soundboard.persistence.Scene
import com.luckylookas.soundboard.persistence.SceneMapping
import com.luckylookas.soundboard.persistence.SceneRepository
import org.springframework.web.bind.annotation.*
import java.util.*


class SceneMappingDto(val file: String, val outputLabel: String, val loop: Boolean, val volume: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        return encodeMixerName(outputLabel) == encodeMixerName((other as SceneMappingDto).outputLabel)

    }

    override fun hashCode(): Int {
        return Objects.hash(encodeMixerName(outputLabel))
    }
}

class SceneDto(val name: String, val mappings: Set<SceneMappingDto>)


@RestController
@RequestMapping("/scene")
class SceneController(
    val mp3Player: Mp3Player,
    val sceneRepository: SceneRepository,
    val outputRepository: OutputRepository
) {

    @PutMapping("")
    fun setScene(@RequestBody dto: SceneDto) {
        outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(dto.name, dto.name).also { output ->
            (sceneRepository.findByNameEqualsIgnoreCase(dto.name) ?: Scene(name = dto.name)).also {
                it.sceneMappings.clear()
                it.sceneMappings.addAll(dto.mappings.map { m ->
                    SceneMapping(file = m.file, volume = m.volume, loop = m.loop, output = output!!, scene = it)
                })
                sceneRepository.save(it)
            }
        }
    }

    @GetMapping("/{name}")
    fun getScene(@PathVariable("name") name: String): SceneDto {
        sceneRepository.findByNameEqualsIgnoreCase(name).also {
            return SceneDto(name = it!!.name, mappings = it.sceneMappings.map { m ->
                SceneMappingDto(
                    outputLabel = m.output.label ?: m.output.mixer,
                    file = m.file,
                    loop = m.loop,
                    volume = m.volume
                )
            }.toSet())
        }
    }
}