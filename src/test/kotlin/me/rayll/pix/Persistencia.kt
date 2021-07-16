package me.rayll.pix

import me.rayll.CarregaChavePixRequest
import me.rayll.TipoDeConta
import me.rayll.pix.clients.*
import me.rayll.pix.registrar.*
import java.time.LocalDateTime
import java.util.*

open class Persistencia {

    companion object CREATED_AT {
        val CLIENT_ID = UUID.randomUUID().toString()
    }

    fun retornaChave(): ChavePix {

        return chave(
            tipoChave = me.rayll.pix.registrar.TipoDeChave.CPF,
            chave = "63657520325",
            clienteId = CLIENT_ID
        )
    }

    fun retornaChavePixRequest() =
        createPixKeyRequest()

    fun retornaChavePixResponse() =
        CreatePixKeyResponse(
            keyType = retornaChavePixRequest().keyType,
            key = retornaChavePixRequest().key,
            bankAccount = retornaChavePixRequest().bankAccount,
            owner = retornaChavePixRequest().owner,
            createdAt = LocalDateTime.now()
        )

    fun retornaDeletePixKeyRequest() = DeletePixKeyRequest(
        key = retornaChave().chave,
        participant = titularResponse().nome
    )

    fun retornaDeletePixKeyResponse() = DeletePixKeyResponse(
        key = retornaChave().chave,
        participant = titularResponse().nome,
        deletedAt = LocalDateTime.now()
    )

    fun dadosDaContaResponse() =
        DadosDoClienteEmBanco(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = titularResponse()
        )

    private fun titularResponse() = TitularResponse("Rafael Pontes", "63657520325")

    fun chave(
        tipoChave: me.rayll.pix.registrar.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipoDeChave = tipoChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada()
        )
    }

    private fun contaAssociada() = ContaAssociada(
        instituicao = "UNIBANCO ITAU",
        nomeDoTitular = "Rafael Pontes",
        cpfDoTitular = "63657520325",
        agencia = "1218",
        numeroDaConta = "291900"
    )


    private fun createPixKeyRequest() =
        CreatePixKeyRequest.of(retornaChave())

}