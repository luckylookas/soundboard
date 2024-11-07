package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.BlobStorage
import com.luckylookas.soundboard.periphery.Mp3Player
import com.luckylookas.soundboard.periphery.STATE
import com.luckylookas.soundboard.persistence.*
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = [OutputRepository::class, OutputTest.TestConfig::class, OutputController::class, SoundFileRepository::class, SoundFileCollectionRepository::class])
class OutputTest {

    @Configuration
    @Transactional
    class TestConfig(val outputRepository: OutputRepository, val soundFileRepository: SoundFileRepository) {
        @PostConstruct
        fun init() {
            outputRepository.save(Output(mixer = "front", label = "ambience", state = STATE.STOPPED))
            outputRepository.save(Output(mixer = "back", state = STATE.STOPPED))
            soundFileRepository.save(SoundFile(name = "woodlands", collection = SoundFileCollection(name = "ambience")))
        }
    }

    @MockBean
    private lateinit var mp3Player: Mp3Player

    @Autowired
    private lateinit var controller: OutputController

    @MockBean
    private lateinit var blobStorage: BlobStorage

    @Mock
    private lateinit var woodlandsMockStream: InputStream
    @Mock
    private lateinit var luteConcertMockStream: InputStream
    @Mock
    private lateinit var testMockStream: InputStream
    @BeforeEach
    fun setUp() {
        doReturn(woodlandsMockStream).`when`(blobStorage).getMp3Stream(argThat(SoundFileMatcher(SoundFile(name = "woodlands", collection = SoundFileCollection(name = "ambience")))))
        doReturn(luteConcertMockStream).`when`(blobStorage).getMp3Stream(argThat(SoundFileMatcher(SoundFile(name = "luteConcert", collection = SoundFileCollection(name = "music")))))
        doReturn(testMockStream).`when`(blobStorage).getMp3Stream(argThat(SoundFileMatcher(SoundFile(name = "test", collection = SoundFileCollection(name = "test")))))
    }

    @Test
    fun setup_checkSetup_initialStateIsAsExpected() {
        assertThat(controller).isNotNull
        assertThat(mp3Player).isNotNull
        assertThat(controller.mp3Player == mp3Player).isTrue
        assertThat(controller.getOutputs()["front"]?.label).isEqualTo("ambience")

        val outputs = controller.getOutputs()
        assertThat(outputs).containsOnlyKeys("front", "back")
        assertThat(outputs.values).extracting("state").allMatch { it == STATE.STOPPED }
    }

    @Test
    fun relabel_relabel_labelIsAssigned() {
        controller.relabel("back", "music")
        assertThat(controller.getOutputs()["back"]?.label).isEqualTo("music")
    }

    @Test
    fun relabel_relabelFrontWithExistingLabel_rejected() {
        assertThatThrownBy({
            controller.relabel(
                "back",
                "ambience"
            )
        }).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun play_anyArgs_argsPassedToMp3Player() {
        controller.play(label = "ambience", volume = 25, loop = true, file = SoundFileDto("woodlands", "ambience"))
        verify(mp3Player).play(output = "front", woodlandsMockStream, 25, loop = true)
    }

    @Test
    fun stop_anyArgs_argsPassedToMp3Player() {
        controller.stop(label = "ambience")
        verify(mp3Player).stop(output = "front")
    }

    @Test
    fun setVolume_validArgs_argsPassedToMp3Player() {
        controller.volume(label = "ambience", volume = 25)
        verify(mp3Player).setVolume(output = "front", percent = 25)
    }

    @Test
    fun setVolume_outOfBoundsArgs_argsCappedAndPassedToMp3Player() {
        controller.volume(label = "ambience", volume = 2000)
        verify(mp3Player).setVolume(output = "front", percent = 100)
        controller.volume(label = "ambience", volume = 0)
        verify(mp3Player).setVolume(output = "front", percent = 1)
    }

    @Test
    fun identify_existingMixerName_playsTestSound() {
        controller.identify(label = "front")
        verify(mp3Player).play(
            "front",
            testMockStream,
                100,
            false)
    }

    @Test
    fun identify_existingLabel_playsTestSound() {
        controller.identify(label = "ambience")
        verify(mp3Player).play(output = "front", file = testMockStream, 100, loop = false)
    }

}