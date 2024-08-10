package com.luckylookas.soundboard.persistence

import com.luckylookas.soundboard.STATE
import jakarta.persistence.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Entity
class Output(

    @Column(nullable = false)
    @Id
    val mixer: String,

    @Column(nullable = false)
    var labelsCsv: String = "",

    @Column(nullable = false)
    var state: STATE = STATE.UNAVAILABLE

)

@Repository
interface OutputRepository: CrudRepository<Output, String> {
    fun findByLabelsCsvContainingIgnoreCaseOrMixerEqualsIgnoreCase(label: String, mixer: String): Output?
    fun findByMixerEqualsIgnoreCase(mixer: String): Output?
}
