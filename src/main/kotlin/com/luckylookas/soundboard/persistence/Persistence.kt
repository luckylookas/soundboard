package com.luckylookas.soundboard.persistence

import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Entity
class Adventure (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var name: String,

    @OneToMany(mappedBy = "adventure", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var scenes: MutableSet<Scene> = mutableSetOf(),
)

@Repository
interface AdventureRepository : CrudRepository<Adventure, Long> {
    fun findAllByNameStartsWithOrderByName(name: String): List<Adventure>
}

@Entity
class Scene (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var name: String,

    @ManyToOne
    var adventure: Adventure,
    @OneToMany(orphanRemoval = true, cascade = [(CascadeType.ALL)])
    var outputs: MutableSet<Output> = mutableSetOf(),
)

@Entity
class Output (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    @ManyToOne
    var scene: Scene? = null,
    var volume: Long = 100,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "output_files",
        joinColumns = [JoinColumn(name = "output_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "file_id", referencedColumnName = "id")],
    )
    var files: MutableSet<File> = mutableSetOf(),

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var playOnStart: File? = null,

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER, orphanRemoval = false)
    var currentlyControlling: MutableSet<SoundDevice> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        joinColumns = [JoinColumn(name = "output_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "device_id", referencedColumnName = "id")]
    )
    var soundDevices: MutableSet<SoundDevice> = mutableSetOf(),
)

@Repository
interface OutputRepository : CrudRepository<Output, Long>

@Entity
class File (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var name: String,
    var loop: Boolean = false,
    var volume: Long = 100,

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "files")
    var outputs: MutableSet<Output> = mutableSetOf(),

    @OneToMany(mappedBy = "playOnStart", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var playOnStart: MutableSet<Output> = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var currentlyPlaying: MutableSet<SoundDevice> = mutableSetOf(),
)

@Repository
interface FileRepository : CrudRepository<File, Long> {
    fun findAllByNameStartsWithOrderByName(name: String): List<File>
    fun findByNameEqualsIgnoreCase(name: String): File?
}

@Entity
class SoundDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var name: String,
    var volume: Long = 100,
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "soundDevices")
    var outputs: MutableSet<Output> = mutableSetOf(),

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var currentlyPlaying: File? = null,

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var currentlyControlledBy: Output? = null,
)

@Repository
interface SoundDeviceRepository: CrudRepository<SoundDevice, Long>