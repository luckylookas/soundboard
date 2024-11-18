package com.luckylookas.soundboard.v2.api

import com.luckylookas.soundboard.v2.service.CollectionService
import com.luckylookas.soundboard.v2.service.FileService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping(path = ["/files"])
class FilesController(val fileService: FileService) {

    @PutMapping("/save/{name}")
    fun save(@RequestBody file: MultipartFile, @PathVariable name: String) = file.let {
        it.inputStream.use { ins ->
            fileService.save(name, ins)
        }
    }

    @GetMapping("/find")
    fun find(@RequestParam(required = true) query: String) = fileService.find(query)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = fileService.delete(id)
}

@RestController
@RequestMapping(path = ["/collections"])
class CollectionController(val fileService: FileService, val collectionService: CollectionService) {

    @PutMapping("/save/{name}")
    fun save(@PathVariable name: String) = collectionService.save(name)

    @GetMapping("/find")
    fun find(@RequestParam(required = true) query: String) = collectionService.find(query)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = collectionService.delete(id)

    @PutMapping("/{id}/{fileId}")
    fun assign(@PathVariable id: Long, @PathVariable fileId: Long) = collectionService.assign(id, fileId)

    @DeleteMapping("/{id}/{fileId}")
    fun remove(@PathVariable id: Long, @PathVariable fileId: Long) = collectionService.remove(id, fileId)
}