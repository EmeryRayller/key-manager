package me.rayll.pix.carregar

import me.rayll.TipoDeConta
import me.rayll.pix.registrar.ChavePix
import me.rayll.pix.registrar.ContaAssociada
import me.rayll.pix.registrar.TipoDeChave
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipoChave: TipoDeChave,
    val chave: String,
    val tipoDeConta: TipoDeConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clientId,
                tipoChave = chave.tipoDeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}