package com.luckylookas.soundboard.api

import com.luckylookas.soundboard.services.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(path = ["/files"])
class FilesController(val service: FileService) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) = service.get(id)
    @PutMapping("/{name}")
    fun save(@RequestBody file: MultipartFile, @PathVariable name: String,
             @RequestParam(required = false, defaultValue = "false") loop: Boolean,
             @RequestParam(required = false, defaultValue = "100") volume: Long
    ) = file.let {
        it.inputStream.use { ins ->
            service.save(name, loop, volume, ins)
        }
    }
    @GetMapping("/find")
    fun find(@RequestParam(required = true) query: String) = service.find(query)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    //rename file
}

@RestController
@RequestMapping(path = ["/adventures"])
class AdventureController(val service: AdventureService) {

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) = service.get(id)
    @GetMapping("/find")
    fun find(@RequestParam(required = true) query: String) = service.find(query)
    @PostMapping("/{name}")
    fun save(@PathVariable name: String) = service.save(name)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PutMapping("/{id}")
    fun assign(@PathVariable id: Long, @RequestBody sceneDto: SceneDto) = service.add(id, sceneDto)
    @DeleteMapping("/{id}/{sceneId}")
    fun remove(@PathVariable id: Long, @PathVariable sceneId: Long) = service.remove(id, sceneId)
    @PutMapping("/{id}/{outputId}/{fileId}")
    fun addFile(@PathVariable id: Long, @PathVariable outputId: Long, @PathVariable fileId: Long,
                @RequestParam(required = false, defaultValue = "false") playOnStart: Boolean) = service.addFile(id, outputId, fileId, playOnStart)

    //remove File
    //remove output
    //rename output
    //rename scene

}

@RestController
@RequestMapping(path = ["/game"])
class GameController(val service: GameService) {

    @PostMapping("/{outputId}")
    fun play(@PathVariable outputId: Long,
             @RequestParam(required = false) fileId: Long? = null) = service.play(outputId, fileId)

    @DeleteMapping("/{outputId}")
    fun stop(@PathVariable outputId: Long) = service.stop(outputId)
}

@RestController
@RequestMapping(path = ["/devices"])
class DeviceController(val service: DeviceService) {

    @PostMapping("/rescan")
    fun rescan() = service.rescan()

    @GetMapping("/")
    fun list() = service.list()

    @PostMapping("/{deviceId}/volume")
    fun volume(@PathVariable deviceId: Long, @RequestParam(required = true) volume: Long) = service.volume(deviceId, volume)

    @PostMapping("/{deviceId}/identify")
    fun identify(@PathVariable deviceId: Long) = service.identify(deviceId)

    @PostMapping("/{deviceId}/stop")
    fun stop(@PathVariable deviceId: Long) = service.stop(deviceId)

    @PutMapping("/{deviceId}/{outputId}")
    fun assign(@PathVariable deviceId: Long, @PathVariable outputId: Long) = service.assign(deviceId, outputId)

    @DeleteMapping("/{deviceId}/{outputId}")
    fun remove(@PathVariable deviceId: Long, @PathVariable outputId: Long) = service.remove(deviceId, outputId)
}