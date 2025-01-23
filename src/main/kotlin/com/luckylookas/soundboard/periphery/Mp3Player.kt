package com.luckylookas.soundboard.periphery

import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.*

fun encodeMixerName(name: String): String = name.replace(" ", "").lowercase()

@Component
class Mp3Player(val audioSystem: AudioSystem, val async: Async) {

    fun clipVolume(clip: Clip, percent: Long) {
        (clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl).apply {
            this.value = this.minimum + ((this.maximum - this.minimum) * (percent/100F)).toInt()
        }
    }

    fun lineVolume(output: String, percent: Long) {
        audioSystem.getMixerInfo().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            audioSystem.getMixer(mixer).sourceLines.map { (it.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl) }
                .forEach { it.value = it.minimum + ((it.maximum - it.minimum) * (percent / 100F)) }
        }
    }

    fun play(filename: String, output: String, file: InputStream, volume: Long, loop: Boolean, onStop: () -> Unit) {
        file.let { blob ->
            audioSystem.getMixerInfo().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
                async.dispatch {
                    getAudioInputStream(blob).use { stream ->
                        stop(mixer.name)
                        val clip = audioSystem.getClip(mixer)
                        clip.open(stream)
                        clipVolume(clip = clip, percent = volume)
                        clip.loop(if (loop) Clip.LOOP_CONTINUOUSLY else 0)
                        clip.addLineListener { event ->
                            if (event.type == LineEvent.Type.CLOSE) {
                                stream.close()
                                onStop.invoke()
                            }
                        }
                        clip.start()
                    }
                }
            }
        }
    }

    fun stop(output: String) {
        audioSystem.getMixerInfo().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            audioSystem.getMixer(mixer).sourceLines.filterIsInstance<Clip>().forEach { clip -> cancelClip(clip) }
        }
    }

    private fun cancelDataLine(line: DataLine?) = line?.apply {
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

}

