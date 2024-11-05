package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import com.luckylookas.soundboard.persistence.SoundFileRepository
import jakarta.persistence.Query
import jakarta.transaction.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

class OutputStateDto(val name: String, val label: String, val state: STATE)

@RestController
@RequestMapping("/outputs")
@Transactional
class OutputController(val mp3Player: Mp3Player, val outputRepository: OutputRepository, val soundFileRepository: SoundFileRepository) {

    @PostMapping("/reload")
    fun reloadMixers(@RequestParam(name = "cleanup", defaultValue = "false", required = false) cleanup: Boolean ) = mp3Player.reloadOutputs(cleanup)

    @PostMapping("/{name}/label/{label}")
    fun relabel(@PathVariable("name") name: String, @PathVariable("label") label: String) {
        if (outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label, label) != null) {
            throw IllegalArgumentException("Duplicate label '$label'")
        }

        (outputRepository.findByMixerEqualsIgnoreCase(name) ?: Output(name)).also {
            it.label = label
            outputRepository.save(it)
        }
    }

    @GetMapping("")
    fun getOutputs(): Map<String, OutputStateDto> = outputRepository.findAll().associateBy({ it.mixer },
        { OutputStateDto(name = it.mixer, label = it.label ?: "", state = it.state) })

    @PostMapping("/{label}/play")
    fun play(
        @PathVariable("label") label: String,
        @RequestParam("volume", defaultValue = "75", required = false) volume: Int,
        @RequestBody file: SoundFileDto,
        @RequestParam(value = "loop", required = false, defaultValue = "false") loop: Boolean
    ) =
        outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.let { output ->
            soundFileRepository.findByNameEqualsIgnoreCaseAndCollectionNameEqualsIgnoreCase(file.name, file.collection).let {
                mp3Player.play(output.mixer, it!!, volume, loop)
            }
        }

    @PostMapping("/{label}/volume/{volume}")
    fun volume(@PathVariable("label") label: String, @PathVariable("volume") volume: Int) =
        outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.setVolume(it.mixer, volume.coerceAtMost(100).coerceAtLeast(1))
        }

    @PostMapping("/{label}/identify")
    fun identify(@PathVariable("label") label: String) =
        outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.play(it.mixer, SoundFileRepository.getTestFile(), 100,false)
        }

    @PostMapping("/{label}/stop")
    fun stop(@PathVariable("label") label: String) =
        outputRepository.findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.stop(it.mixer)
        }

    @PutMapping("/stop")
    fun stopAll() =
        outputRepository.findAll().forEach {
            mp3Player.stop(it.mixer)
        }

}