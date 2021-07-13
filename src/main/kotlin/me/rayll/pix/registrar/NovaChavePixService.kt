package me.rayll.pix.registrar

import io.micronaut.http.HttpResponse
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.pix.clients.BuscarClientItau
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.exceptions.ChavePixExistenteException
import me.rayll.pix.shared.exceptions.DadosDoClienteNaoEncontratoException
import javax.transaction.Transactional


@Singleton
@Validated
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: BuscarClientItau
) {

    @Transactional
    fun registra(novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix ${novaChave.chave} existente")
        }

        val clientResponse: HttpResponse<DadosDoClienteEmBanco> = itauClient.buscarConta(novaChave.clientId, novaChave.tipoDeConta!!.name)
        val contaAssociada = clientResponse.body() ?:
                            throw DadosDoClienteNaoEncontratoException("Cliente não encontrado no Itau.")

        val chave = novaChave.paraChavePix(contaAssociada.paraContaAssociada())
        repository.save(chave)
        return chave
    }
}
