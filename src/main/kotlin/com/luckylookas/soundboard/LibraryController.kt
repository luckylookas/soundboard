package com.luckylookas.soundboard

import com.luckylookas.soundboard.periphery.BlobStorage
import com.luckylookas.soundboard.persistence.SoundFile
import com.luckylookas.soundboard.persistence.SoundFileCollection
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
        soundFileCollectionRepository.saveAll(blobStorage.scan())
    }

    @GetMapping("/query")
    fun findFile(@RequestParam("query") query: String): Collection<SoundFileDto> =
        soundFileRepository.findAllByNameStartingWithOrderByNameAsc(query.lowercase().replace(" ", "_"))
            .map { SoundFileDto(it.name, it.collection.name) }

    @GetMapping("/{collection}")
    fun listCollection(@PathVariable collection: String): Collection<SoundFileDto> =
        soundFileCollectionRepository.findByNameEqualsIgnoreCase(
            collection.lowercase().replace(" ", "_")
        )?.soundFiles?.stream()?.map {
            SoundFileDto(it.name, it.collection.name)
        }?.toList().orEmpty()

    @GetMapping("/")
    fun listCollections(): Collection<String> =
        soundFileCollectionRepository.findAll().stream().map { it.name }?.toList().orEmpty()

    @PostMapping("/{collection}")
    fun upload(
        @RequestParam(required = false) name: String?,
        @PathVariable("collection") collection: String,
        @RequestBody file: MultipartFile
    ) =
        SoundFileDto(
            name = (name ?: file.name).lowercase().replace(" ", "_"),
            collection = collection.lowercase().replace(" ", "_")
        ).also { f ->
            ( if (soundFileCollectionRepository.existsByName(f.collection)) soundFileCollectionRepository.findByNameEqualsIgnoreCase(f.collection)
            else soundFileCollectionRepository.save(SoundFileCollection(name = f.collection)))?.let { collection ->
                blobStorage.save(f, file.inputStream)
                collection.soundFiles.add(SoundFile(name = f.name, collection = collection))
            }
        }

    @DeleteMapping("/{collection}/{name}")
    fun delete(
        @PathVariable collection: String,
        @PathVariable name: String,
    ) {
        SoundFileDto(
            name = name.lowercase().replace(" ", "_"),
            collection = collection.lowercase().replace(" ", "_")
        ).let {
            blobStorage.delete(it)
            soundFileRepository.findByNameEqualsIgnoreCaseAndCollectionNameEqualsIgnoreCase(it.name, it.collection)
                ?.let { entity ->
                    soundFileRepository.delete(entity)
                }
        }
    }
}