package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.BlobStorage
import com.luckylookas.soundboard.persistence.*
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream

@Transactional
@EnableAutoConfiguration
@SpringBootTest(classes = [LibraryTest.TestConfig::class, SoundFileRepository::class, SoundFileCollectionRepository::class, BlobStorage::class, LibraryController::class])
class LibraryTest {

    @Configuration
    @Transactional
    class TestConfig {
        @PostConstruct
        fun init() {

        }
    }

    @Autowired
    private lateinit var controller: LibraryController

    private val testFileDto = SoundFileDto("mocky mocksen.mp3", "testy")

    @BeforeEach
    fun setUp() {
        controller.delete(testFileDto.collection, testFileDto.name, true)
    }

    @AfterEach
    fun tearDown() {
        controller.delete(testFileDto.collection, testFileDto.name, true)

    }

    @Test
    fun scan_list() {
        controller.rescan()
        assertThat(controller.listCollections()).containsExactlyInAnyOrder("music", "test")
        assertThat(controller.listCollection("music")).extracting("name").containsExactlyInAnyOrder("sunny_place.mp3", "sundown.mp3")
        assertThat(controller.findFile("sun")).extracting("name").containsExactlyInAnyOrder("sunny_place.mp3", "sundown.mp3")
    }

    @Test
    fun upload() {
        val mpf = mock<MultipartFile>()
        `when`(mpf.originalFilename).thenReturn(testFileDto.name)
        `when`(mpf.inputStream).thenReturn(ByteArrayInputStream("abc".toByteArray()))
        controller.rescan()
        controller.upload(testFileDto.name, testFileDto.collection, mpf)
        assertThat(controller.listCollections()).containsExactlyInAnyOrder("music", "test", "testy")
        assertThat(controller.listCollection("testy")).extracting("name").containsExactlyInAnyOrder("mocky_mocksen.mp3")
    }

}