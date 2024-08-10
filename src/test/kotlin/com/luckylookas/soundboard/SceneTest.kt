package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import com.luckylookas.soundboard.persistence.SceneRepository
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration

@EnableAutoConfiguration
@SpringBootTest(classes = [OutputRepository::class, SceneRepository::class, SceneTest.TestConfig::class, SceneController::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SceneTest {

    @Configuration
    class TestConfig(val outputRepository: OutputRepository) {
        @PostConstruct
        fun init() {
            outputRepository.save(Output(mixer = "front", state = STATE.STOPPED, label = "ambience"))
            outputRepository.save(Output(mixer = "back", state = STATE.STOPPED, label = "music"))
        }
    }

    @MockBean
    private lateinit var mp3Player: Mp3Player

    @Autowired
    private lateinit var controller: SceneController

    @Test
    fun setup_checkSetup_initialStateIsAsExpected() {
        assertThat(controller).isNotNull
        assertThat(mp3Player).isNotNull
        assertThat(controller.mp3Player == mp3Player).isTrue
    }

    @Test
    fun setScene_validInput_createsScene() {
        controller.setScene(
            SceneDto(
                name = "tavern", mappings =
                setOf(
                    SceneMappingDto(file = "chatter", outputLabel = "ambience", loop = true, volume = 25),
                    SceneMappingDto(file = "luteConcert", outputLabel = "music", loop = false, volume = 40)
                )
            )
        )

        val ret = controller.getScene("tavern")

        assertThat(ret).isNotNull
        assertThat(ret.name).isEqualTo("tavern")
        assertThat(ret.mappings.map { it.file }.toSet()).containsExactlyInAnyOrder("chatter", "luteConcert")
        assertThat(ret.mappings.map { it.outputLabel }.toSet()).containsExactlyInAnyOrder("ambience", "music")
        assertThat(ret.mappings.map { it.loop }.toSet()).containsExactlyInAnyOrder(true, false)
        assertThat(ret.mappings.map { it.volume }.toSet()).containsExactlyInAnyOrder(25, 40)
    }
}