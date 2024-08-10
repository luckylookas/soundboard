package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.OutputRepository
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.*
import kotlin.streams.toList

enum class STATE {
    PLAYING,
    STOPPED,
    UNAVAILABLE
}

fun encodeMixerName(name: String): String = name.replace(" ", "").lowercase()

@Component
class Mp3Player(val outputsRepository: OutputRepository , @Value("\${staticdir}") val staticdir: String) {

    val backgroundOpsScope = CoroutineScope(SupervisorJob())

    @PreDestroy
    fun destroy() {
        availableMixers().forEach { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.forEach {
                with((it as SourceDataLine)) {
                    stop()
                    drain()
                    close()
                }
            }
        }
    }

    fun setVolume(output: String, percent: Int) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.map { (it.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl) }
                .forEach { it.value = it.minimum + ((it.maximum - it.minimum) * (percent / 100F)) }
        }
    }

    fun availableMixers(): List<Mixer.Info> = Arrays.stream(AudioSystem.getMixerInfo())
        .filter { it.description.lowercase(Locale.getDefault()).contains("playback") }
        .filter { !it.name.lowercase().contains("primary") }
        .toList()

    fun play(output: String, file: String, loop: Boolean) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
           (outputsRepository.findByMixerEqualsIgnoreCase(output)?.also {
                state = STATE.PLAYING  })

            backgroundOpsScope.launch(Dispatchers.IO) {
                getAudioInputStream(FileInputStream("$staticdir$file.mp3")).use {
                    val clip = AudioSystem.getClip(mixer)
                    clip.open(it)
                    clip.loop(if (loop) Clip.LOOP_CONTINUOUSLY else 0)
                    clip.addLineListener { event ->
                        if (event.type == LineEvent.Type.CLOSE) {
                            outputsRepository.findByMixerEqualsIgnoreCase(output)?.state = STATE.STOPPED
                        }
                    }
                    clip.start()
                }
            }
        }
    }

    fun stop(name: String) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(name) }?.also { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.forEach {
                with((it as DataLine)) {
                    stop()
                    drain()
                    close()
                    outputsRepository.findByMixerEqualsIgnoreCase(name)?.state = STATE.STOPPED
                }
            }
        }
    }

    fun getAudioInputStream(stream: InputStream): AudioInputStream {
        val audioInputStream: AudioInputStream =
            AudioSystem.getAudioInputStream(if (stream.markSupported()) stream else BufferedInputStream(stream))

        val format = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            audioInputStream.format.sampleRate,
            16,
            audioInputStream.format.channels,
            audioInputStream.format.channels * 2,
            audioInputStream.format.sampleRate,
            false
        )


        return AudioSystem.getAudioInputStream(format, audioInputStream)
    }

}




//    @Deprecated("line can't loop")
//    suspend fun play_line(output: String, file: String, loop: Boolean) {
//
//        mixers[output.lowercase()]?.also { mixer ->
//            backgroundOpsScope.launch(Dispatchers.IO) {
//                getAudioInputStream(FileInputStream("C:\\untis\\dev\\soundboard\\src\\main\\resources\\$file.mp3")).use {
//                    val sourceDataLine = AudioSystem.getMixer(mixer)
//                        .getLine(DataLine.Info(SourceDataLine::class.java, it.format)) as SourceDataLine
//                    try {
//                        with(sourceDataLine) {
//                            stop()
//                            drain()
//                            close()
//                            open(format)
//                            start()
//                        }
//                        var bytesRead: Int
//                        val buffer = ByteArray(65536)
//                        while (it.read(buffer).also { r -> bytesRead = r } != -1) {
//                            sourceDataLine.write(buffer, 0, bytesRead)
//                        }
//                    } finally {
//                        with(sourceDataLine) {
//                            stop()
//                            drain()
//                            close()
//                        }
//                    }
//                }
//            }
//        }
//    }