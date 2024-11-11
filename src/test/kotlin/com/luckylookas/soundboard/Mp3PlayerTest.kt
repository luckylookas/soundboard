package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.*
import com.luckylookas.soundboard.persistence.*
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import java.io.InputStream
import javax.sound.sampled.Clip
import javax.sound.sampled.Line
import javax.sound.sampled.Mixer

@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = [Mp3PlayerTest.TestConfig::class, OutputRepository::class])
class Mp3PlayerTest {

    @Configuration
    class TestConfig {
        @PostConstruct
        fun init() {

        }
    }

    private lateinit var mp3Player: Mp3Player
    @Autowired
    private lateinit var outputRepository: OutputRepository

    @MockBean
    private lateinit var audioSystem: AudioSystem

    @MockBean
    private lateinit var async: Async

    class MockMixerInfo(name: String, description: String): Mixer.Info(name, "any", description, name)

    @BeforeEach
    fun setUp() {
        doReturn(arrayOf(MockMixerInfo("speaker", AUDIO_OUT),
            MockMixerInfo("mic", "input"),
            MockMixerInfo("$DEFAULT_OUT_ALIAS (speaker)", AUDIO_OUT)
        )).whenever(audioSystem).getMixerInfo()

        val mockMixer = mock<Mixer>()
        doReturn(mockMixer).whenever(audioSystem).getMixer(any())
        doReturn(arrayOf(mock<Clip>())).whenever(mockMixer).sourceLines
        mp3Player = Mp3Player(outputRepository, audioSystem, async)
        mp3Player.initDb()
        assertThat(outputRepository.findAll()).hasSize(1)
    }

    @AfterEach
    fun tearDown() {
        mp3Player.destroy()
    }

    @Test
    fun loadPlayStop_filtersInputsAndDefaultAliases() {
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.STOPPED)
        mp3Player.play("speaker", mock<InputStream>(), 10, false)
        verify(async, times(1)).dispatch(any())
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.PLAYING)
        mp3Player.stop("speaker")
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.STOPPED)

    }

    @Test
    fun loadPlayDestory_filtersInputsAndDefaultAliases() {
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.STOPPED)
        mp3Player.play("speaker", mock<InputStream>(), 10, false)
        verify(async, times(1)).dispatch(any())
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.PLAYING)
        mp3Player.destroy()
        assertThat(outputRepository.findByMixerEqualsIgnoreCase("speaker")?.state).isEqualTo(STATE.STOPPED)
    }
}