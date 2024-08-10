package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.*

class PlayRequest(val file: String)
class OutputStateDto(val name: String, val labels: List<String>, val state: STATE)

@RestController
@RequestMapping("/outputs")
class Controller(val mp3Player: Mp3Player, val outputRepository: OutputRepository) {

    @PostConstruct
    fun initDb() {
        reloadMixers(false)
    }

    @PostMapping("/reload")
    fun reloadMixers(@RequestParam(name = "cleanup", defaultValue = "false", required = false) cleanup: Boolean )  {
        val availableMixers =  mp3Player.availableMixers().map { encodeMixerName(it.name) }

        if (cleanup) {
            outputRepository.findAll().filter { !availableMixers.contains(it.mixer) }.forEach{ outputRepository.delete(it) }
        }

        availableMixers.forEach {
            outputRepository.findByMixerEqualsIgnoreCase(encodeMixerName(it))?: outputRepository.save(Output(mixer = it, state = STATE.STOPPED))
        }
    }

    @PostMapping("/{name}/label/{label}")
    fun relabel(@PathVariable("name") name: String, @PathVariable("label") label: String) {
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
            mp3Player.play(it.mixer, file.file, loop)
        }

    @PostMapping("/{label}/volume/{volume}")
    fun volume(@PathVariable("label") label: String, @PathVariable("volume") volume: Int) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.setVolume(it.mixer, volume)
        }

    @PostMapping("/{label}/identify")
    fun play(@PathVariable("label") label: String) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.play(it.mixer, "test", false)
        }

    @PostMapping("/{label}/stop")
    fun stop(@PathVariable("label") label: String) =
        outputRepository.findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label, label)?.also {
            mp3Player.stop(it.mixer)
        }
}