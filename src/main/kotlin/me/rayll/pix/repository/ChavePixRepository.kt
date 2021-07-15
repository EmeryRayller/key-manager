package me.rayll.pix.repository

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import me.rayll.pix.registrar.ChavePix
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {
    fun existsByChave(chave: String): Boolean
    fun findByIdAndClientId(pixId: String, clientId: String): Optional<ChavePix>
    fun findByChave(chave: String): Optional<ChavePix>

}
