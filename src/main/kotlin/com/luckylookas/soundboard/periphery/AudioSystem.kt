package com.luckylookas.soundboard.periphery

import org.springframework.stereotype.Component
import java.io.InputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.Mixer

@Component
class AudioSystem {

    fun getMixerInfo() = javax.sound.sampled.AudioSystem.getMixerInfo()
    fun getMixer(mixer: Mixer.Info) = javax.sound.sampled.AudioSystem.getMixer(mixer)
    fun getClip(mixer: Mixer.Info) = javax.sound.sampled.AudioSystem.getClip(mixer)
    fun getAudioInputStream(inputStream: InputStream) = javax.sound.sampled.AudioSystem.getAudioInputStream(inputStream)
    fun getAudioInputStream(inputStream: AudioFormat, audioInputStream: AudioInputStream) = javax.sound.sampled.AudioSystem.getAudioInputStream(inputStream, audioInputStream)

}