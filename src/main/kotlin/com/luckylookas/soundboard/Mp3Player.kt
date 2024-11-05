package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.BlobStorage
import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import com.luckylookas.soundboard.persistence.SoundFile
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

@Component
class Mp3Player(val outputRepository: OutputRepository , val storage: BlobStorage) {

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

    fun play(output: String, file: SoundFile, volume: Int, loop: Boolean) {
        storage.getMp3Stream(file)?.let { blob ->
            availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
                (outputRepository.findByMixerEqualsIgnoreCase(output)?.also {
                    it.state = STATE.PLAYING
                    outputRepository.save(it)
                })

                backgroundOpsScope.launch(Dispatchers.IO) {
                    getAudioInputStream(blob).use { stream ->
                        stop(mixer.name)
                        val clip = AudioSystem.getClip(mixer)

                        clip.open(stream)
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
    }

    fun stop(output: String) {
        availableMixers().firstOrNull { encodeMixerName(it.name) == encodeMixerName(output) }?.also { mixer ->
            AudioSystem.getMixer(mixer).sourceLines.forEach { clip ->
                with((clip as Clip)) {
                    loop(0)
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