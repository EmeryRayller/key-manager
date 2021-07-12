package me.rayll.pix.registrar

import me.rayll.RegistraChavePixRequest
import me.rayll.TipoDeConta
import javax.validation.Constraint
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RegistraChavePixRequest.paraNovaChavePix(validator: Validator): NovaChavePix {

    val novaChavePix = NovaChavePix(
        clientId = this.clientId,
        tipoChave = me.rayll.pix.registrar.TipoDeChave.valueOf(this.tipoDeChave.name),
//     when(this.tipoDeChave){
//
//        TipoDeChave.UNKNOWN_TIPO_CHAVE -> null
//        else -> TipoDeChave.valueOf(this.tipoDeChave.name)
//
//    },
        chave = this.chave,
        tipoDeConta = when (this.tipoDeConta) {
            TipoDeConta.UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(this.tipoDeConta.name)
        }
    )

    val erros = validator.validate(novaChavePix)

    if (! erros.isEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return novaChavePix
}