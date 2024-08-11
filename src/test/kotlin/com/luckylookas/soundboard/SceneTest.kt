package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.Output
import com.luckylookas.soundboard.persistence.OutputRepository
import com.luckylookas.soundboard.persistence.SceneRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@EnableAutoConfiguration
@SpringBootTest(classes = [OutputRepository::class, SceneRepository::class, SceneController::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class SceneTest {

    @Autowired
    private lateinit var outputRepository: OutputRepository

    @MockBean
    private lateinit var mp3Player: Mp3Player

    @Autowired
    private lateinit var controller: SceneController

    @BeforeEach
    fun setup() {
        outputRepository.save(Output(mixer = "front", state = STATE.STOPPED, label = "ambience"))
        outputRepository.save(Output(mixer = "back", state = STATE.STOPPED, label = "music"))
    }

    @Test
    fun setup_checkSetup_initialStateIsAsExpected() {
        assertThat(controller).isNotNull
        assertThat(mp3Player).isNotNull
        assertThat(controller.mp3Player == mp3Player).isTrue
    }

    @Test
    fun setScene_validInput_createsScene() {

        val mappings = setOf(
            SceneMappingDto(file = "chatter", output = "ambience", loop = true, volume = 25),
            SceneMappingDto(file = "luteConcert", output = "music", loop = false, volume = 40)
        )

        controller.setScene(SceneDto(name = "tavern", mappings = mappings), "tavern")

        val ret = controller.getScene("tavern")!!

        assertThat(ret.name).isEqualTo("tavern")
        assertThat(ret.mappings).containsExactlyInAnyOrderElementsOf(mappings)
    }

    @Test
    fun setScene_renameScene_updatesSceneNameAndMappings() {

        controller.setScene(
            SceneDto(
                name = "tavern",
                mappings = setOf(
                    SceneMappingDto(file = "chatter", output = "ambience", loop = true, volume = 25)
                )
            ), "tavern"
        )

        assertThat(controller.getScene("newName")).isNull()
        assertThat(controller.getScene("tavern")).isNotNull

        val mappings = setOf(SceneMappingDto(file = "music", output = "ambience", loop = true, volume = 100))

        controller.setScene(SceneDto(name = "newName", mappings = mappings), "tavern")

        assertThat(controller.getScene("tavern")).isNull()
        val ret = controller.getScene("newName")!!

        assertThat(ret.name).isEqualTo("newName")
        assertThat(ret.mappings).containsExactlyInAnyOrderElementsOf(mappings)
    }
}