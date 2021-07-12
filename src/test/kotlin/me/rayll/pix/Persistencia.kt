package me.rayll.pix

import me.rayll.TipoDeConta
import me.rayll.pix.registrar.*
import java.util.*

open class Persistencia {

    fun retornaChave(): ChavePix {

        return chave(
            tipoChave = me.rayll.pix.registrar.TipoDeChave.CPF,
            chave = "63657520325",
            clienteId = RegistraChaveEndpointTest.CLIENT_ID
        )
    }

    fun dadosDaContaResponse(): DadosDoClienteEmBanco =
        DadosDoClienteEmBanco(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Pontes", "63657520325")
        )

    private fun chave(
        tipoChave: me.rayll.pix.registrar.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clientId = clienteId,
            tipoDeChave = tipoChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}