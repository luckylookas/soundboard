package com.luckylookas.soundboard.v2

import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.v2.api.CollectionController
import com.luckylookas.soundboard.v2.api.FilesController
import com.luckylookas.soundboard.v2.api.Mapper
import com.luckylookas.soundboard.v2.persistence.*
import com.luckylookas.soundboard.v2.service.CollectionService
import com.luckylookas.soundboard.v2.service.FileService
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.multipart.MultipartFile

class MockMultiPartFile(private val name: String): MultipartFile {
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
@SpringBootTest(classes = [
    FileRepository::class,
    FileService::class,
    FilesController::class,
    Mapper::class])
class FileControllerTest {

    @MockBean
    lateinit var storage: Storage

    @Autowired
    lateinit var filesController: FilesController

    @Test
    @Transactional
    fun save_any_savesAndCallsStorage() {
        filesController.save(name = MOCKFILE.name, file = MOCKFILE)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("name").containsExactly(MOCKFILE.name)
        verify(storage, times(1)).save(eq(MOCKFILE.name), any())
    }

    @Test
    @Transactional
    fun find_fullName_findsFile() {
        filesController.save(name = MOCKFILE.name, file = MOCKFILE)
        assertThat(filesController.find(query = MOCKFILE.name)).extracting("name").containsExactly(MOCKFILE.name)
    }

    @Test
    @Transactional
    fun find_partial_findsFilesStatingWith() {
        filesController.save(name = MOCKFILE.name, file = MOCKFILE)
        filesController.save(name = MOCKINGFILE.name, file = MOCKINGFILE)
        assertThat(filesController.find(query = "mock")).extracting("name").containsExactly(MOCKFILE.name, MOCKINGFILE.name)
        assertThat(filesController.find(query = "mocking")).extracting("name").containsExactly(MOCKINGFILE.name)
    }

    @Test
    @Transactional
    fun delete_any_deletesFileByNameAndCallsStorage() {
        filesController.save(name = MOCKFILE.name, file = MOCKFILE)
        val mockingFile = filesController.save(name = MOCKINGFILE.name, file = MOCKINGFILE)

        filesController.delete(id = mockingFile.id!!)

        assertThat(filesController.find(query = "mock")).extracting("name").containsExactly(MOCKFILE.name)
        verify(storage, times(2)).save(any(), any())
        verify(storage, times(1)).delete(eq(MOCKINGFILE.name))

    }



}


@EnableAutoConfiguration
@SpringBootTest(classes = [
    FileRepository::class,
    Mapper::class,
    CollectionRepository::class,
    CollectionController::class,
    CollectionService::class,])
class CollectionControllerTest {

    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    lateinit var collectionController: CollectionController

    @Test
    @Transactional
    fun save_any_saves() {
       collectionController.save("abc")
       assertThat(collectionController.find("abc")).extracting("name").containsExactly("abc")
    }

    @Test
    @Transactional
    fun find_fullName_findsFile() {
        collectionController.save("abc")
        assertThat(collectionController.find("abc")).extracting("name").containsExactly("abc")
    }

    @Test
    @Transactional
    fun find_partial_findsNameStartingWith() {
        collectionController.save("abc")
        collectionController.save("abcde")
        assertThat(collectionController.find("abc")).extracting("name").containsExactly("abc", "abcde")
        assertThat(collectionController.find("abcd")).extracting("name").containsExactly("abcde")

    }

    @Test
    @Transactional
    fun delete_any_deletesFileByName() {
        collectionController.save("abc").also {
            collectionController.delete(it.id!!)
        }
        assertThat(collectionController.find("abc")).isEmpty()
    }

    @Test
    @Transactional
    fun assign_any_addsToCollection() {
        collectionController.save("abc").also { collection ->
            fileRepository.save(File(name = MOCKFILE.name)).also { file ->
                assertThat(collectionController.assign(collection.id!!, file.id!!)?.files?.map { it.name }).containsExactly("abc")
                assertThat(collectionController.find("abc")[0].files.map { it.name }).containsExactly("abc")
            }
        }
    }

    @Test
    @Transactional
    fun remove_any_removesFromCollection() {
        collectionController.save("abc").also { collection ->
            fileRepository.save(File(name = MOCKFILE.name)).also { file ->
                assertThat(collectionController.assign(collection.id!!, file.id!!)?.files?.map { it.name }).containsExactly("abc")
                assertThat(collectionController.remove(collection.id!! , file.id!!)?.files?.map { it.name }).isEmpty()
                assertThat(collectionController.find("abc")[0].files.map { it.name }).isEmpty()
            }
        }
    }
}