package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.BlobStorage
import com.luckylookas.soundboard.persistence.*
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.io.InputStream

@EnableAutoConfiguration
@SpringBootTest(classes = [OutputRepository::class, SceneRepository::class, SceneController::class, SoundFileRepository::class, SoundFileCollectionRepository::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class SceneTest {

    @Autowired
    private lateinit var outputRepository: OutputRepository

    @Autowired
    private lateinit var soundFileRepository: SoundFileRepository

    @MockBean
    private lateinit var blobStorage: BlobStorage

    @MockBean
    private lateinit var mp3Player: Mp3Player

    @Autowired
    private lateinit var controller: SceneController

    @Mock
    private lateinit var woodlandsMockStream: InputStream
    @Mock
    private lateinit var luteConcertMockStream: InputStream

    @BeforeEach
    fun setup() {
        doReturn(woodlandsMockStream).`when`(blobStorage).getMp3Stream(argThat(SoundFileMatcher(SoundFile(name = "woodlands", collection = SoundFileCollection(name = "ambience")))))
        doReturn(luteConcertMockStream).`when`(blobStorage).getMp3Stream(argThat(SoundFileMatcher(SoundFile(name = "luteConcert", collection = SoundFileCollection(name = "music")))))

        outputRepository.save(Output(mixer = "front", state = STATE.STOPPED, label = "ambience"))
        outputRepository.save(Output(mixer = "back", state = STATE.STOPPED, label = "music"))
        soundFileRepository.save(SoundFile(name = "luteConcert", collection = SoundFileCollection(name = "music")))
        soundFileRepository.save(SoundFile(name = "woodlands", collection = SoundFileCollection(name = "ambience")))
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
            SceneMappingDto(file = SoundFileDto("woodlands", "ambience"), output = "ambience", loop = true, volume = 25),
            SceneMappingDto(file = SoundFileDto("luteConcert", "music"), output = "music", loop = false, volume = 40)
        )

        val hotbar = setOf(
            HotbarDto("sting", volume = 100, loop = false),
            HotbarDto("distantThunder", volume = 50, loop = false),
        )

        controller.setScene(SceneDto(name = "tavern", mappings = mappings, hotbar = hotbar), "tavern")

        val ret = controller.getScene("tavern")!!

        assertThat(ret.name).isEqualTo("tavern")
        assertThat(ret.mappings).hasSize(2)
        assertThat(ret.hotbar).hasSize(2)
    }

    @Test
    fun setScene_renameScene_updatesSceneNameAndMappings() {
        controller.setScene(
            SceneDto(
                name = "tavern",
                mappings = setOf(
                    SceneMappingDto(file = SoundFileDto("chatter", "music"), output = "ambience", loop = true, volume = 25)
                )
            ), "tavern"
        )

        assertThat(controller.getScene("newName")).isNull()
        assertThat(controller.getScene("tavern")).isNotNull

        val mappings = setOf(SceneMappingDto(file = SoundFileDto("luteConcert", "music"), output = "ambience", loop = true, volume = 100))

        controller.setScene(SceneDto(name = "newName", mappings = mappings), "tavern")

        assertThat(controller.getScene("tavern")).isNull()
        val ret = controller.getScene("newName")!!

        assertThat(ret.name).isEqualTo("newName")
        assertThat(ret.mappings).allMatch { a: SceneMappingDto -> a.file.name == mappings.first().file.name }
    }

    @Test
    fun playScene_validInput_classPlayOnMp3Player() {
        val mappings = setOf(
            SceneMappingDto(file = SoundFileDto("woodlands", "ambience"), output = "front", loop = true, volume = 25),
            SceneMappingDto(file = SoundFileDto("luteConcert", "music"), output = "back", loop = false, volume = 40)
        )

        controller.setScene(SceneDto(name = "tavern", mappings = mappings), "tavern")
        controller.play("tavern")
        verify(mp3Player).destroy()
        verify(mp3Player).play(
                    "front",
                    woodlandsMockStream,
                    25,
                    true)

        verify(mp3Player).play(
            "back",
            luteConcertMockStream,
            40,
            false)

        verifyNoMoreInteractions(mp3Player)

    }
}