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
        FileRepository::class,
        FileService::class,
        FilesController::class,
        Mapper::class]
)
class FileControllerTest {

    @MockBean
    lateinit var storage: Storage

    @Autowired
    lateinit var filesController: FilesController

    @Autowired
    lateinit var fileRepository: FileRepository

    @BeforeEach
    fun setup() {
        fileRepository.deleteAll()
    }

    @Test
    fun save_any_savesAndCallsStorage() {
        filesController.save(name = MOCKFILE.name, loop = true, file = MOCKFILE, volume = 50)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("name").containsExactly(MOCKFILE.name)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("loop").containsExactly(true)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("defaultVolume").containsExactly(50)
        verify(storage, times(1)).save(eq(MOCKFILE.name), any())
    }

    @Test
    fun find_fullName_findsFile() {
        filesController.save(name = MOCKFILE.name, loop = false, file = MOCKFILE, volume = 10)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("name").containsExactly(MOCKFILE.name)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("loop").containsExactly(false)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("defaultVolume").containsExactly(10)


    }

    @Test
    fun find_partial_findsFilesStatingWith() {
        filesController.save(name = MOCKFILE.name, loop = false, file = MOCKFILE, volume = 50)
        filesController.save(name = MOCKINGFILE.name, loop = false, file = MOCKINGFILE, volume = 50)
        assertThat(filesController.find(query = "mock")).extracting("name")
            .containsExactly(MOCKFILE.name, MOCKINGFILE.name)
        assertThat(filesController.find(query = "mocking")).extracting("name").containsExactly(MOCKINGFILE.name)
    }

    @Test
    fun delete_any_deletesFileByNameAndCallsStorage() {
        filesController.save(name = MOCKFILE.name, loop = false, file = MOCKFILE, volume = 50)
        val mockingFile = filesController.save(name = MOCKINGFILE.name, loop = false, file = MOCKINGFILE, volume = 50)

        filesController.delete(id = mockingFile.id!!)

        assertThat(filesController.find(query = "mock")).extracting("name").containsExactly(MOCKFILE.name)
        verify(storage, times(2)).save(any(), any())
        verify(storage, times(1)).delete(eq(MOCKINGFILE.name))

    }


}

@EnableAutoConfiguration
@SpringBootTest(
    classes = [
        Mapper::class,
        AdventureRepository::class,
        AdventureService::class,
        AdventureController::class,
        FileRepository::class
    ]
)
class AdventureControllerTest {

    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var adventureRepository: AdventureRepository

    @Autowired
    lateinit var adventureController: AdventureController

    @BeforeEach
    fun setup() {
        adventureRepository.deleteAll()
        fileRepository.deleteAll()
    }

    @Test
    fun save_any_saves() {
        adventureController.save("abc")
        assertThat(adventureController.find("abc")).extracting("name").containsExactly("abc")
    }

    @Test
    fun find_fullName_findsFile() {
        adventureController.save("abc")
        assertThat(adventureController.find("abc")).extracting("name").containsExactly("abc")
    }

    @Test
    fun find_partial_findsNameStartingWith() {
        adventureController.save("abc")
        adventureController.save("abcde")
        assertThat(adventureController.find("abc")).extracting("name").containsExactly("abc", "abcde")
        assertThat(adventureController.find("abcd")).extracting("name").containsExactly("abcde")

    }

    @Test
    fun delete_any_deletesFileByName() {
        adventureController.save("abc").also {
            adventureController.delete(it.id!!)
        }
        assertThat(adventureController.find("abc")).isEmpty()
    }

    @Test
    fun assign_any_addsToAdventure() {
        adventureController.save("abc").also {
            adventureController.assign(
                it.id!!, SceneDto(
                    name = "library",
                    outputs = setOf(
                        OutputDto(name = "windows"),
                        OutputDto(name = "ambience")
                    )
                )
            )
        }

        assertThat(adventureController.find("abc")).hasSize(1)
        assertThat(adventureController.find("abc")[0].scenes).allMatch { scene ->
            scene.id != null && scene.name == "library" && scene.outputs.map { o -> o.name }
                .containsAll(setOf("windows", "ambience"))
        }
    }


    @Test
    fun remove_any_removesScene() {
        adventureController.save("abc").also {
            adventureController.assign(
                it.id!!, SceneDto(
                    name = "library",
                    outputs = setOf(
                        OutputDto(name = "windows", id = null),
                        OutputDto(id = null, name = "ambience")
                    ),
                    id = null
                )
            )
        }

        adventureController.find("abc")
            .forEach { a -> a.scenes.forEach { s -> adventureController.remove(a.id!!, s.id!!) } }

        assertThat(adventureController.find("abc")).allMatch { a -> a.scenes.isEmpty() }
    }

    @Test
    fun addFile_any_addsFile() {
        val adventureId = adventureController.save("abc").also {
            adventureController.assign(
                it.id!!, SceneDto(
                    name = "library",
                    outputs = setOf(
                        OutputDto(name = "windows", id = null),
                        OutputDto(id = null, name = "ambience")
                    ),
                    id = null
                )
            )
            adventureController.assign(
                it.id!!, SceneDto(
                    name = "car",
                    outputs = setOf(
                        OutputDto(name = "radio", id = null),
                    ),
                    id = null
                )
            )
        }.id

        val file = fileRepository.save(File(name = MOCKFILE.name))

        adventureController.get(adventureId!!)!!.also { adventure ->
            adventureController.addFile(
                adventureId,
                adventure.scenes.find { it.name == "library" }?.outputs?.find { it.name == "windows" }!!.id!!,
                file.id!!,
                playOnStart = false
            )
            adventureController.addFile(
                adventureId,
                adventure.scenes.find { it.name == "car" }?.outputs?.find { it.name == "radio" }!!.id!!,
                file.id!!,
                playOnStart = true
            )
        }

        adventureController.get(adventureId)?.also { adventure ->
            assertThat(adventure.scenes.find { it.name == "library" }?.outputs?.find { it.name == "ambience" }!!.files).isEmpty()
            assertThat(adventure.scenes.find { it.name == "library" }?.outputs?.find { it.name == "windows" }!!.files).allMatch { it.name == MOCKFILE.name }
            assertThat(adventure.scenes.find { it.name == "car" }?.outputs?.find { it.name == "radio" }!!.files).allMatch { it.name == MOCKFILE.name }
            assertThat(adventure.scenes.find { it.name == "car" }?.outputs?.find { it.name == "radio" }!!.playOnStart?.name == MOCKFILE.name)
            assertThat(adventure.scenes.find { it.name == "library" }?.outputs?.find { it.name == "windows" }!!.playOnStart).isNull()


        }
    }
}

@EnableAutoConfiguration
@SpringBootTest(
    classes = [
        FileRepository::class,
        FileService::class,
        OutputRepository::class,
        SoundDeviceRepository::class,
        DeviceService::class,
        DeviceController::class,
        Mapper::class]
)
class DeviceControllerTest {

    @MockBean
    lateinit var audioSystem: AudioSystem
    @MockBean
    lateinit var mp3Player: Mp3Player

    @Autowired
    lateinit var deviceController: DeviceController
    @Autowired lateinit var soundDeviceRepository: SoundDeviceRepository

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

    @Test
    fun rescan_deletesNonExisting() {
        soundDeviceRepository.save(SoundDevice(name = "nonExisting"))
        deviceController.rescan()
        assertThat(soundDeviceRepository.findAll()).hasSize(2)
    }
}

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
        //upload some files
        //f1 is pretty loud, so we turn it's volume adjustment down to 75%
        val f1 = filesController.save(file = MOCKFILE, name = MOCKFILE.name, loop = true, volume = 75)
        val f2 = filesController.save(file = MOCKINGFILE, name = MOCKINGFILE.name, loop = false, volume = 100)
        deviceController.rescan()


        val adventureId = adventureController.save("adventure").also { adventure ->
            //create an adventure with 2 scenes
            // a tavern with ambience and volume tuned down and a door where someone will eg. knock with volume up at maximum
            // a forest with ambience at low volume
            adventureController.assign(adventure.id!!, SceneDto(name = "tavern", outputs = setOf(OutputDto(name = "ambience", volume = 75), OutputDto(name = "door", volume = 100))))
            adventureController.assign(adventure.id!!, SceneDto(name = "forest", outputs = setOf(OutputDto(name = "ambience", volume = 50))))
            val tavern_ambience = adventure?.scenes?.find { scene -> scene.name == "tavern"}?.outputs?.find { it.name == "ambience" }!!
            val tavern_door = adventure.scenes.find { scene -> scene.name == "tavern"}?.outputs?.find { it.name == "door" }!!
            val forest_ambience = adventure.scenes.find { scene -> scene.name == "forest"}?.outputs?.find { it.name == "ambience" }!!

            //add some files to the scenes outputs
            // in the tavern there is just generic ambience that will start when the tavern scene is active & an initially silent door
            // in the forest the ambience has 2 files (eg. night and day), none of those starts on scene load
            adventureController.addFile(adventure.id!!, tavern_ambience.id!!, f1.id!!, playOnStart = true)
            adventureController.addFile(adventure.id!!, tavern_door.id!!, f2.id!!, playOnStart = false)
            adventureController.addFile(adventure.id!!, forest_ambience.id!!, f1.id!!, playOnStart = false)
            adventureController.addFile(adventure.id!!, forest_ambience.id!!, f2.id!!, playOnStart = false)

            // get available audio devices
            val devices = deviceController.list()

            //identify devices by playing a test sound
            //assert audio system calls

            // assign outputs to devices
            // the speaker will play ambience on both scenes
            // the box will be used for the tavern door, and also play ambience on the forest
            deviceController.assign(devices.find { it.name == "speaker" }?.id!!, tavern_ambience.id!!)
            deviceController.assign(devices.find { it.name == "speaker" }?.id!!, forest_ambience.id!!)
            deviceController.assign(devices.find { it.name == "box" }?.id!!, forest_ambience.id!!)
            deviceController.assign(devices.find { it.name == "box" }?.id!!, tavern_door.id!!)
        }.id!!

        //trigger playing on an output
        adventureController.save("adventure").also { adventure ->
            // play the 'playOnStart' file on the tavern ambience output, this is f1 on a loop
            gameController.play(adventure.scenes.find { scene -> scene.name == "tavern"}?.outputs?.find { it.name == "ambience" }!!.id!!)
            // play f2 (arbitrary) on the tavern's door output once, this is f2
            gameController.play(adventure.scenes.find { scene -> scene.name == "tavern"}?.outputs?.find { it.name == "door" }!!.id!!, f2.id)

            //assert playing states of all devices

            //turn down volume for all devices to 10 percent of maximum
            deviceController.list().forEach {
                deviceController.volume(it.id!!, 10)
            }

            //stop tavern ambience
            gameController.stop(adventure.scenes.find { scene -> scene.name == "tavern"}?.outputs?.find { it.name == "ambience" }!!.id!!)
            //assert playing states  of all devices


        }
    }
}