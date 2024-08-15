package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
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
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.sound.sampled.*
import kotlin.io.path.name

enum class STATE {
    PLAYING,
    STOPPED,
    UNAVAILABLE
}

fun encodeMixerName(name: String): String = name.replace(" ", "").lowercase()

@Component
class Mp3Player(val outputRepository: OutputRepository , @Value("\${staticdir}") val staticdir: String) {

    val backgroundOpsScope = CoroutineScope(SupervisorJob())

    @PostConstruct
    fun initDb() {
      reloadOutputs(false)
    }

    fun reloadOutputs(cleanup: Boolean) {
        val availableMixers =  availableMixers().map { encodeMixerName(it.name) }

        if (cleanup) {
            outputRepository.findAll().filter { !availableMixers.contains(it.mixer) }.forEach{ outputRepository.delete(it) }
        }

        availableMixers.forEach {
            outputRepository.findByMixerEqualsIgnoreCase(encodeMixerName(it))?: outputRepository.save(Output(mixer = it, state = STATE.STOPPED))
        }
    }

    @PreDestroy
    fun destroy() {
        availableMixers().forEach { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.forEach {
                with((it as DataLine)) {
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

    fun play(output: String, file: String, volume: Int, loop: Boolean) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            (outputRepository.findByMixerEqualsIgnoreCase(output)?.also {
                it.state = STATE.PLAYING
                outputRepository.save(it)
           })

            backgroundOpsScope.launch(Dispatchers.IO) {
                getAudioInputStream(FileInputStream("$staticdir$file.mp3")).use {
                    stop(mixer.name)
                    val clip = AudioSystem.getClip(mixer)

                    clip.open(it)
                    clip.loop(if (loop) Clip.LOOP_CONTINUOUSLY else 0)
                    setVolume(output, volume)
                    clip.addLineListener { event ->
                        if (event.type == LineEvent.Type.CLOSE) {
                            outputRepository.findByMixerEqualsIgnoreCase(output)?.also {
                                it.state = STATE.STOPPED
                                outputRepository.save(it)
                            }
                        }
                    }
                    clip.start()
                }
            }
        }
    }

    fun stop(output: String) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.forEach {
                with((it as DataLine)) {
                    stop()
                    drain()
                    close()
                    outputRepository.findByMixerEqualsIgnoreCase(output)?.also {
                        it.state = STATE.STOPPED
                        outputRepository.save(it)
                    }
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

    fun findFiles(query: String): List<String> =
        Files.find(Path.of(staticdir), 0, { path, _ -> path.fileName.name.lowercase().startsWith(query.lowercase()) && path.fileName.name.endsWith(".m3") }).map { it.fileName.name }.toList()


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