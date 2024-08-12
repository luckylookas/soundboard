package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.*
import jakarta.transaction.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.HashSet

class SceneMappingDto(val file: String, val output: String, val loop: Boolean, val volume: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        return Objects.hash(output, file, loop, volume)
    }
}

class HotbarDto(val file: String, val loop: Boolean, val volume: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        return Objects.hash(file, loop, volume)
    }
}

class SceneDto(val name: String, val mappings: Set<SceneMappingDto>, val hotbar: Set<HotbarDto> = HashSet())

@RestController
@RequestMapping("/scene")
@Transactional
class SceneController(
    val mp3Player: Mp3Player,
    val sceneRepository: SceneRepository,
    val outputRepository: OutputRepository
) {
    @PutMapping("/{name}")
    fun setScene(@RequestBody dto: SceneDto, @PathVariable("name") name: String) {
        if (dto.mappings.map { it.output }.distinct().size != dto.mappings.size) {
            throw IllegalArgumentException("cannot map multiple files to one output (attempted to map to: ${dto.mappings.map { it.output }})")
        }

        (sceneRepository.findByNameEqualsIgnoreCase(name) ?: Scene(name = name)).also {
            it.name = dto.name
            it.sceneMappings.clear()
            it.sceneMappings.addAll(
                dto.mappings.mapNotNull { m ->
                    outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(m.output, m.output)
                        ?.let { output ->
                            SceneMapping(file = m.file, volume = m.volume, loop = m.loop, output = output, scene = it)
                        }
                })
            it.hotbar.clear()
            it.hotbar.addAll(dto.hotbar.map { m ->
                HotBarEntry(file = m.file, volume = m.volume, loop = m.loop, scene = it)
            })
            sceneRepository.save(it)
        }
    }

    @GetMapping("/{name}")
    fun getScene(@PathVariable("name") name: String): SceneDto? =
        sceneRepository.findByNameEqualsIgnoreCase(name)?.let {
            SceneDto(
                name = it.name,
                mappings = it.sceneMappings.map { m ->
                    SceneMappingDto(
                        output = m.output.label ?: m.output.mixer,
                        file = m.file,
                        loop = m.loop,
                        volume = m.volume
                    )
                }.toSet(),
                hotbar = it.hotbar.map { m ->
                    HotbarDto(file = m.file, volume = m.volume, loop = m.loop)
                }.toSet()
            )
        }

    @PutMapping("/{name}/play")
    fun play(@PathVariable("name") name: String) =
        sceneRepository.findByNameEqualsIgnoreCase(name)?.also {
            it.sceneMappings.forEach { mapping ->
                mp3Player.play(mapping.output.mixer, mapping.file, mapping.volume, mapping.loop)
            }
        }

}