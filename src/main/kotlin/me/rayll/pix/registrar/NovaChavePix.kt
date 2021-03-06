package me.rayll.pix.registrar

import io.micronaut.core.annotation.Introspected
import me.rayll.TipoDeConta
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class NovaChavePix(
    @field:ValidUUID
    @field:NotEmpty
    val clientId: String,
    @field:NotNull
    val tipoChave: TipoDeChave?,
    @field:Size(max = 77)
    val chave: String,
    @field:NotNull
    val tipoDeConta: TipoDeConta?
) {
    fun paraChavePix(conta: ContaAssociada) = ChavePix(
        clientId = UUID.fromString(this.clientId).toString(),
        tipoDeChave = TipoDeChave.valueOf(this.tipoChave!!.name),
        chave = if (this.tipoChave == TipoDeChave.UNKNOWN_TIPO_CHAVE) UUID.randomUUID().toString() else this.chave!!,
        tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
        conta = conta
    )
}