package com.luckylookas.soundboard.persistence

import com.luckylookas.soundboard.STATE
import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Entity
class Scene(
    @Column
    @Id
    val name: String,

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
    val sceneMappings: MutableSet<SceneMapping> = HashSet()

)

@Entity
class SceneMapping(

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

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val scene: Scene,

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    val output: Output,
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

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.LAZY)
    val sceneMappings: MutableSet<SceneMapping> = HashSet()

)

@Repository
interface OutputRepository: CrudRepository<Output, String> {
    fun findByLabelEqualsIgnoreCaseOrMixerEqualsIgnoreCase(label: String, mixer: String): Output?
    fun findByMixerEqualsIgnoreCase(mixer: String): Output?
}

@Repository
interface SceneRepository: CrudRepository<Scene, String> {
  fun findByNameEqualsIgnoreCase(name: String): Scene?
}