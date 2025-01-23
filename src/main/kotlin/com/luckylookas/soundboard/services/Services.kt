package com.luckylookas.soundboard.services

import com.luckylookas.soundboard.periphery.AudioSystem
import com.luckylookas.soundboard.periphery.Mp3Player
import com.luckylookas.soundboard.periphery.Storage
import com.luckylookas.soundboard.persistence.*
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW)
class FileService(val storage: Storage, val fileRepository: FileRepository, val mapper: Mapper) {

    @PostConstruct
    fun init() {
        if (fileRepository.findByNameEqualsIgnoreCase("test") == null) {
            fileRepository.save(File(name = "test", loop = false, volume = 100))
        }
        prune()
    }

    fun prune() {
        fileRepository.deleteAll(fileRepository.findAll().filter { file -> !storage.exists(file.name) })
    }

    fun save(name: String, loop: Boolean, volume: Long, stream: InputStream) =
        (fileRepository.findByNameEqualsIgnoreCase(name) ?: fileRepository.save(
            File(
                name = name,
                loop = loop,
                volume = volume
            )
        )).let { file ->
            file.loop = loop
            file.volume = volume
            storage.save(name, stream).let {
                mapper.toDto(File(id = file.id, name = it, loop = file.loop, volume = file.volume))
            }
        }

    fun find(query: String): List<FileDto> = fileRepository.findAllByNameStartsWithOrderByName(query)
        .map { mapper.toDto(it) }

    fun delete(id: Long) = fileRepository.findById(id).ifPresent {
        storage.delete(it.name)
        fileRepository.delete(it)
    }

    fun get(id: Long): FileDto? = fileRepository.findById(id).orElse(null)?.let { mapper.toDto(it) }

}

@Service
@Transactional
class AdventureService(
    val mapper: Mapper,
    val adventureRepository: AdventureRepository,
    val fileRepository: FileRepository
) {
    fun find(query: String): List<AdventureDto> =
        adventureRepository.findAllByNameStartsWithOrderByName(query).map { mapper.toDto(it) }.toList()

    fun save(name: String) = adventureRepository.save(Adventure(name = name)).let { mapper.toDto(it) }
    fun delete(id: Long) = adventureRepository.deleteById(id)

    fun add(id: Long, sceneDto: SceneDto) = adventureRepository.findById(id).orElse(null)?.let { adventure ->
        if (sceneDto.id != null) {
            adventure.scenes.removeIf {it.id == sceneDto.id}
        }
        adventure.scenes.add(mapper.fromDto(sceneDto, adventure))
        mapper.toDto(adventureRepository.save(adventure))
    }

    fun remove(id: Long, sceneId: Long) = adventureRepository.findById(id).orElse(null)?.let {

        it.scenes.removeIf { s -> s.id == sceneId }
    }

    fun get(id: Long): AdventureDto? = adventureRepository.findById(id).orElse(null)?.let { mapper.toDto(it) }
    fun addFile(id: Long, sceneId: Long, outputId: Long, fileId: Long, playOnStart: Boolean): AdventureDto? =
        adventureRepository.findById(id).orElse(null)?.let { adventure ->
            adventure.scenes.find { it.id == sceneId }?.outputs?.find { it.id == outputId }?.apply {
                fileRepository.findById(fileId).orElse(null)?.let {
                    files.add(it)
                    if (playOnStart) {
                        this.playOnStart = it
                    } else if (it == this.playOnStart) {
                        this.playOnStart = null
                    }
                }
            }
            mapper.toDto(adventure)
        }

    fun assign(id: Long, sceneId: Long, outputDto: OutputDto) = adventureRepository.findById(id).orElse(null)?.let { adventure ->
        adventure.scenes.find { it.id == sceneId }?.apply {
            this.outputs.add(mapper.fromDto(outputDto))

        }
        mapper.toDto(adventureRepository.save(adventure))
    }

    fun remove(id: Long, sceneId: Long, outputId: Long) = adventureRepository.findById(id).orElse(null)?.let { adventure ->
        adventure.scenes.find { it.id == sceneId }?.apply {
            this.outputs.removeIf { it.id == outputId }
        }
        mapper.toDto(adventureRepository.save(adventure))
    }

    fun removeFile(id: Long, sceneId: Long, outputId: Long, fileId: Long) = adventureRepository.findById(id).orElse(null)?.let { adventure ->
        adventure.scenes.find { it.id == sceneId }?.outputs?.find { it.id == outputId }?.apply {
           this.files.removeIf { it.id == fileId }
            if (this.playOnStart?.id == fileId) {
                this.playOnStart = null
            }
        }
        mapper.toDto(adventure)
    }

    fun adjustVolume(id: Long, sceneId: Long, outputId: Long, volume: Long) = adventureRepository.findById(id).orElse(null)?.let { adventure ->
        adventure.scenes.find { it.id == sceneId }?.apply {
            this.outputs.find { it.id == outputId }?.apply {

                this.volume = Math.clamp(volume, 0, 100).toLong()
            }
        }
        mapper.toDto(adventureRepository.save(adventure))
    }
}

@Service
@Transactional
class GameService(val outputRepository: OutputRepository, val fileRepository: FileRepository, val mp3Player: Mp3Player, val storage: Storage, val deviceService: DeviceService) {

    fun play(outputId: Long, fileId: Long?) {
        outputRepository.findById(outputId).ifPresent { output ->
            output.soundDevices.forEach { device ->
                (fileId ?: output.playOnStart?.id)?.also { resolvedFileId ->
                    fileRepository.findById(resolvedFileId).orElse(null)?.also {
                        device.currentlyPlaying = it
                        device.currentlyControlledBy = output
                        mp3Player.play(it.name, device.name, storage.get(it.name)!!, (100.0 * (device.volume.toDouble()/100) * (output.volume.toDouble()/100) * (it.volume.toDouble()/100)).toLong(), it.loop) { deviceService.stop(device.id!!) }
                    }
                }
            }
        }
    }

    fun stop(outputId: Long) {
        outputRepository.findById(outputId).ifPresent { output ->
            output.soundDevices.forEach { device ->
                deviceService.stop(device.id!!)
            }
        }
    }
}

@Service
@Transactional(value = Transactional.TxType.REQUIRED)
class DeviceService(
    val mp3Player: Mp3Player,
    val mapper: Mapper,
    val audioSystem: AudioSystem,
    val fileRepository: FileRepository,
    val storage: Storage,
    val soundDeviceRepository: SoundDeviceRepository,
    val outputRepository: OutputRepository
) {
    @PostConstruct
    fun init() {
        rescan()
    }

    fun rescan() {
        val mixers = audioSystem.getMixerInfo()
        val devices = soundDeviceRepository.findAll()

        outputRepository.findAll().forEach { output ->
            output.soundDevices.clear()
        }

        // remove devices where the filter does no longer exist
        soundDeviceRepository.deleteAll(devices.filter { device -> !mixers.map { it.name }.contains(device.name) })
        // create devices for new mixers
        soundDeviceRepository.saveAll(mixers.filter { mixer -> !devices.map { it.name }.contains(mixer.name) }
            .map { SoundDevice(name = it.name) })

    }

    fun assign(deviceId: Long, outputId: Long) {
        soundDeviceRepository.findById(deviceId).ifPresent { device ->
            outputRepository.findById(outputId).ifPresent { output ->
                device.outputs.add(output)
                output.soundDevices.add(device)
            }
        }
    }

    fun remove(deviceId: Long, outputId: Long) {
        outputRepository.findById(outputId).ifPresent { output ->
            soundDeviceRepository.findById(deviceId).ifPresent { device ->
                output.soundDevices.remove(device)
                device.outputs.remove(output)
            }
        }
    }

    fun list(): Set<SoundDeviceDto> = soundDeviceRepository.findAll().map { mapper.toDto(it) }.toSet()

    fun identify(deviceId: Long) {
        soundDeviceRepository.findById(deviceId).ifPresent { device ->
            fileRepository.findByNameEqualsIgnoreCase("test")?.also {
                device.currentlyPlaying = it
                device.currentlyControlledBy = null
                mp3Player.play(it.name, device.name, storage.get(it.name)!!, device.volume * (it.volume/100), it.loop) { this.stop(deviceId) }
            }
        }
    }

    fun stop(deviceId: Long) {
        soundDeviceRepository.findById(deviceId).ifPresent { device ->
            device.currentlyPlaying = null
            device.currentlyControlledBy = null
            mp3Player.stop(device.name)
            soundDeviceRepository.save(device)
        }
    }

    fun volume(deviceId: Long, volume: Long) {
        soundDeviceRepository.findById(deviceId).ifPresent { device ->
            device.volume = volume
            mp3Player.lineVolume(device.name, volume * ((device.currentlyControlledBy?.volume?:100)/100) * ((device.currentlyPlaying?.volume ?: 100)/100))
        }
    }


}