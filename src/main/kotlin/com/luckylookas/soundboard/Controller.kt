package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import jakarta.annotation.PostConstruct
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.*

class PlayRequest(val file: String)
class OutputStateDto(val name: String, val labels: List<String>, val state: STATE)

@RestController
@RequestMapping("/outputs")
class Controller(val mp3Player: Mp3Player, val outputRepository: OutputRepository) {

    @PostMapping("/reload")
    fun reloadMixers(@RequestParam(name = "cleanup", defaultValue = "false", required = false) cleanup: Boolean ) = mp3Player.reloadOutputs(cleanup)

    @PostMapping("/{name}/label/{label}")
    fun relabel(@PathVariable("name") name: String, @PathVariable("label") label: String) {
        if (outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label) != null) {
            throw IllegalArgumentException("Duplicate label '$label'")
        }

        (outputRepository.findByMixerEqualsIgnoreCase(name) ?: Output(name)).also {
            it.labelsCsv = "${it.labelsCsv},$label"
            outputRepository.save(it)
        }
    }

    @GetMapping("")
    fun getOutputs(): Map<String, OutputStateDto> = outputRepository.findAll().associateBy({ it.mixer },
        {
            OutputStateDto(
                name = it.mixer,
                labels = it.labelsCsv.split(",").filter { s -> s.isNotEmpty() }.toList(),
                state = it.state
            )
        })

    @PostMapping("/{label}/play")
    fun play(
        @PathVariable("label") label: String,
        @RequestParam("volume", defaultValue = "75", required = false) volume: Int,
        @RequestBody file: PlayRequest,
        @RequestParam(value = "loop", required = false, defaultValue = "false") loop: Boolean
    ) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.play(it.mixer, file.file, volume, loop)
        }

    @PostMapping("/{label}/volume/{volume}")
    fun volume(@PathVariable("label") label: String, @PathVariable("volume") volume: Int) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.setVolume(it.mixer, volume.coerceAtMost(100).coerceAtLeast(1))
        }

    @PostMapping("/{label}/identify")
    fun identify(@PathVariable("label") label: String) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.play(it.mixer, "test", 100,false)
        }

    @PostMapping("/{label}/stop")
    fun stop(@PathVariable("label") label: String) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.stop(it.mixer)
        }
}