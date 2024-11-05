package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.BlobStorage
import com.luckylookas.soundboard.persistence.SoundFile
import com.luckylookas.soundboard.persistence.SoundFileCollectionRepository
import com.luckylookas.soundboard.persistence.SoundFileRepository
import jakarta.transaction.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

class SoundFileDto(val name: String, val collection: String)


@RestController
@RequestMapping("/files")
@Transactional
class LibraryController(
    val soundFileCollectionRepository: SoundFileCollectionRepository,
    val soundFileRepository: SoundFileRepository,
    val blobStorage: BlobStorage
) {

    @PostMapping("/rescan")
    fun rescan() {
        soundFileRepository.deleteAll()
        soundFileCollectionRepository.deleteAll()
        blobStorage.scan()
    }

    @GetMapping("/query")
    fun findFile(@RequestParam("query") query: String): Collection<SoundFileDto> =
        soundFileRepository.findAllByNameStartingWithOrderByNameAsc(query.lowercase())
            .map { SoundFileDto(it.name, it.collection.name) }

    @PostMapping("/files/{collection}")
    fun upload(
        @RequestParam(required = false) name: String?,
        @PathVariable("collection") collection: String,
        @RequestBody file: MultipartFile
    ) =
        SoundFileDto(name = name ?: file.name, collection = collection).let { f ->
            soundFileCollectionRepository.findByNameEqualsIgnoreCase(f.collection)?.let { collection ->
                soundFileRepository.save(SoundFile(name = f.name, collection = collection))
            }
        }?.let {
            SoundFileDto(
                name = it.name,
                collection
            )
        }


}