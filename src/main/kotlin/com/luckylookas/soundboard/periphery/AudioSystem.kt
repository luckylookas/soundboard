package com.luckylookas.soundboard.periphery

import org.springframework.stereotype.Component
import java.io.InputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.Mixer

const val AUDIO_OUT = "playback"
const val DEFAULT_OUT_ALIAS = "primary"

@Component
class AudioSystem {

    fun getMixerInfo() = javax.sound.sampled.AudioSystem.getMixerInfo().filter { it.description.lowercase().contains(AUDIO_OUT) && !it.name.lowercase().contains(DEFAULT_OUT_ALIAS) }
    fun getMixer(mixer: Mixer.Info) = javax.sound.sampled.AudioSystem.getMixer(mixer)
    fun getClip(mixer: Mixer.Info) = javax.sound.sampled.AudioSystem.getClip(mixer)
    fun getAudioInputStream(inputStream: InputStream) = javax.sound.sampled.AudioSystem.getAudioInputStream(inputStream)
    fun getAudioInputStream(inputStream: AudioFormat, audioInputStream: AudioInputStream) = javax.sound.sampled.AudioSystem.getAudioInputStream(inputStream, audioInputStream)

}