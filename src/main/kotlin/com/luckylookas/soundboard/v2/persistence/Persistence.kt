package com.luckylookas.soundboard.v2.persistence

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

    @ManyToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    @JoinTable(
        name = "adventure_scenes",
        joinColumns = [ JoinColumn(name = "scene_id") ],
        inverseJoinColumns = [ JoinColumn(name = "adventure_id") ]
    )
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

    @ManyToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "scenes")
    var adventures: MutableSet<Adventure> = mutableSetOf(),
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var outputs: MutableSet<Output> = mutableSetOf(),
)

@Entity
class Output (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var scene: Scene? = null,

    @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var collection: Collection? = null
)

@Entity
class File (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var name: String,

    @ManyToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "files")
    var collection: MutableSet<Collection> = mutableSetOf(),
)

@Repository
interface FileRepository : CrudRepository<File, Long> {
    fun findAllByNameStartsWithOrderByName(name: String): List<File>
    fun findByNameEqualsIgnoreCase(name: String): File?
}

@Entity
class Collection (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,

    @ManyToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    @JoinTable(
        name = "file_collection",
        joinColumns = [ JoinColumn(name = "collection_id") ],
        inverseJoinColumns = [ JoinColumn(name = "file_id") ]
    )
    var files: MutableSet<File> = mutableSetOf(),
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var outputs: MutableSet<Output> = mutableSetOf()
)

@Repository
interface CollectionRepository : CrudRepository<Collection, Long> {
    fun findAllByNameStartsWithOrderByName(name: String): List<Collection>
}

