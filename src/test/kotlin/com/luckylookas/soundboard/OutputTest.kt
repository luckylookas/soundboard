package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration

@EnableAutoConfiguration
@SpringBootTest(classes = [OutputRepository::class, OutputTest.TestConfig::class, OutputController::class])
class OutputTest {

    @Configuration
    class TestConfig(val outputRepository: OutputRepository) {
        @PostConstruct
        fun init() {
            outputRepository.save(Output(mixer = "front", label = "ambience", state = STATE.STOPPED))
            outputRepository.save(Output(mixer = "back", state = STATE.STOPPED))

        }
    }

    @MockBean
    private lateinit var mp3Player: Mp3Player

    @Autowired
    private lateinit var controller: OutputController

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
        controller.play(label = "ambience", volume = 25, loop = true, file = PlayRequest("woodlands"))
        verify(mp3Player).play(output = "front", file = "woodlands", 25, loop = true)
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
        verify(mp3Player).play(output = "front", file = "test", 100, loop = false)
    }

    @Test
    fun identify_existingLabel_playsTestSound() {
        controller.identify(label = "ambience")
        verify(mp3Player).play(output = "front", file = "test", 100, loop = false)
    }

    @Test
    fun findFiles_query_passedArgsToMp3Player() {
        controller.findFile(query = "ambience")
        verify(mp3Player).findFiles("ambience")
    }
}