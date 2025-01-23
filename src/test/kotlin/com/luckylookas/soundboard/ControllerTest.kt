package com.luckylookas.soundboard

import com.luckylookas.soundboard.api.*
import com.luckylookas.soundboard.periphery.AudioSystem
import com.luckylookas.soundboard.periphery.Mp3Player
import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.persistence.*
import com.luckylookas.soundboard.services.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.multipart.MultipartFile
import javax.sound.sampled.Mixer

class MockMultiPartFile(private val name: String) : MultipartFile {
    override fun getInputStream() = name.byteInputStream()

    override fun getName() = name

    override fun getOriginalFilename() = name

    override fun getContentType() = "audio/mpeg"

    override fun isEmpty() = false

    override fun getSize() = name.length.toLong()

    override fun getBytes() = name.byteInputStream().readBytes()
    override fun transferTo(dest: java.io.File) {
        TODO("Not implemented")
    }

}

val MOCKFILE = MockMultiPartFile("mockfile")
val MOCKINGFILE = MockMultiPartFile("mockingfile")

@EnableAutoConfiguration
@SpringBootTest(
    classes = [
        SoundDeviceRepository::class,
        DeviceService::class,
        DeviceController::class,
        Mapper::class,
        FileSystemStorage::class,
        Mp3Player::class,
        AdventureRepository::class,
        AdventureService::class,
        AdventureController::class,
        FileRepository::class,
        FileService::class,
        FilesController::class,
        GameController::class,
        GameService::class,
       ]
)

class IntegrationTest {

    @MockBean
    lateinit var audioSystem: AudioSystem

    @Autowired
    lateinit var deviceController: DeviceController
    @Autowired
    lateinit var filesController: FilesController
    @Autowired
    lateinit var adventureController: AdventureController
    @Autowired
    lateinit var gameController: GameController

    @BeforeEach
    fun setup() {
        val speaker = mock<Mixer.Info>().apply {
            doReturn("speaker").`when`(this).name
        }

        val box = mock<Mixer.Info>().apply {
            doReturn("box").`when`(this).name
        }

        doReturn(listOf(speaker, box)).`when`(audioSystem).getMixerInfo()
    }

    @Disabled
    @Test
    fun setUpAdventureOnGameDay() {

    }
}