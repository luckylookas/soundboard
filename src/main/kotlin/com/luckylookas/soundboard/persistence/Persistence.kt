package com.luckylookas.soundboard.persistence

import com.luckylookas.soundboard.STATE
import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Entity
class Scene(

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
    val sceneMappings: MutableSet<SceneMapping> = HashSet(),

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
    val hotbar: MutableSet<HotBarEntry> = HashSet()

)

@Entity
class SoundFileCollection(

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.LAZY)
    val soundFiles: MutableSet<SoundFile> = HashSet()
)

@Entity
class SoundFile(
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val collection: SoundFileCollection,

    @OneToMany(fetch = FetchType.LAZY)
    val sceneMappings:  MutableSet<SceneMapping> = HashSet()
)

@Entity
class HotBarEntry(
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(nullable = false)
    val file: String,
    @Column(nullable = false)
    val loop: Boolean,
    @Column(nullable = false)
    val volume: Int,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    val scene: Scene?,
)

@Entity
class SceneMapping(

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(nullable = false)
    val loop: Boolean,
    @Column(nullable = false)
    val volume: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    val scene: Scene,

    @ManyToOne(fetch = FetchType.EAGER)
    val output: Output,

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    val file: SoundFile,
)

@Entity
class Output(

    @Column(nullable = false)
    @Id
    val mixer: String,

    @Column(nullable = false)
    var label: String? = "",

    @Column(nullable = false)
    var state: STATE = STATE.UNAVAILABLE,

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
    val sceneMappings: MutableSet<SceneMapping> = HashSet()

)

@Repository
interface OutputRepository: CrudRepository<Output, String> {
    fun findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label: String, mixer: String): Output?
    fun findByMixerEqualsIgnoreCase(mixer: String): Output?
    override fun findAll(): List<Output>
}

@Repository
interface SceneRepository: CrudRepository<Scene, Int> {
  fun findByNameEqualsIgnoreCase(name: String): Scene?
}

@Repository
interface SoundFileRepository: CrudRepository<SoundFile, Int> {
    fun findByNameEqualsIgnoreCaseAndCollectionNameEqualsIgnoreCase(name: String, collection: String): SoundFile?
    fun findAllByNameStartingWithOrderByNameAsc(name: String): List<SoundFile>

    companion object {
        fun getTestFile() = SoundFile(collection = SoundFileCollection(name = "test"), name = "test")
    }
}

@Repository
interface SoundFileCollectionRepository: CrudRepository<SoundFileCollection, Int> {
    fun findByNameEqualsIgnoreCase(name: String): SoundFileCollection?
    fun existsByName(name: String): Boolean
    override fun findAll(): List<SoundFileCollection>
}
