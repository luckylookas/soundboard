package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.Async
import com.luckylookas.soundboard.periphery.AudioSystem
import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.*
import javax.sound.sampled.*

enum class STATE {
    PLAYING,
    STOPPED,
    UNAVAILABLE
}

fun encodeMixerName(name: String): String = name.replace(" ", "").lowercase()

const val AUDIO_OUT = "playback"

const val DEFAULT_OUT_ALIAS = "primary"

@Component
class Mp3Player(val outputRepository: OutputRepository, val audioSystem: AudioSystem, val async: Async) {

    @PostConstruct
    fun initDb() {
        reloadOutputs(false)
    }

    fun reloadOutputs(cleanup: Boolean) {
        val availableMixers = availableMixers().map { encodeMixerName(it.name) }

        if (cleanup) {
            outputRepository.findAll().filter { !availableMixers.contains(it.mixer) }
                .forEach { outputRepository.delete(it) }
        }

        availableMixers.forEach {
            outputRepository.findByMixerEqualsIgnoreCase(encodeMixerName(it))
                ?: outputRepository.save(Output(mixer = it, state = STATE.STOPPED))
        }
    }

    @PreDestroy
    fun destroy() {
        availableMixers().forEach { mixer ->
            audioSystem.getMixer(mixer).sourceLines.forEach {
                cancelDataLine(it as DataLine)
                outputRepository.findByMixerEqualsIgnoreCase(encodeMixerName(mixer.name))?.state = STATE.STOPPED
            }
        }
    }

    fun setVolume(output: String, percent: Int) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            audioSystem.getMixer(mixer).sourceLines.map { (it.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl) }
                .forEach { it.value = it.minimum + ((it.maximum - it.minimum) * (percent / 100F)) }
        }
    }

    fun play(output: String, file: InputStream, volume: Int, loop: Boolean) {
        file.let { blob ->
            availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
                outputRepository.findByMixerEqualsIgnoreCase(output)?.state = STATE.PLAYING
                async.dispatch {
                    getAudioInputStream(blob).use { stream ->
                        stop(mixer.name)
                        val clip = audioSystem.getClip(mixer)
                        clip.open(stream)
                        clip.loop(if (loop) Clip.LOOP_CONTINUOUSLY else 0)
                        setVolume(output, volume)
                        clip.addLineListener { event ->
                            if (event.type == LineEvent.Type.CLOSE) {
                                stream.close()
                                outputRepository.findByMixerEqualsIgnoreCase(output)?.state = STATE.STOPPED
                            }
                        }
                        clip.start()
                    }
                }
            }
        }
    }

    fun stop(output: String) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            audioSystem.getMixer(mixer).sourceLines.forEach { clip ->
                cancelClip((clip as Clip))
                outputRepository.findByMixerEqualsIgnoreCase(output)?.state = STATE.STOPPED
            }
        }
    }

    private fun cancelDataLine(line: DataLine) = line.apply {
        stop()
        drain()
        close()
    }

    private fun cancelClip(clip: Clip) = clip.apply {
        loop(0)
        cancelDataLine(this)
    }

    private fun getAudioInputStream(stream: InputStream): AudioInputStream =
        audioSystem.getAudioInputStream(if (stream.markSupported()) stream else BufferedInputStream(stream))
            .let {
                audioSystem.getAudioInputStream(
                    AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        it.format.sampleRate,
                        16,
                        it.format.channels,
                        it.format.channels * 2,
                        it.format.sampleRate,
                        false
                    ), it
                )
            }

    private fun availableMixers(): List<Mixer.Info> = Arrays.stream(audioSystem.getMixerInfo())
        .filter { it.description.lowercase(Locale.getDefault()).contains(AUDIO_OUT) }
        .filter { !it.name.lowercase().contains(DEFAULT_OUT_ALIAS) }
        .toList()
}

