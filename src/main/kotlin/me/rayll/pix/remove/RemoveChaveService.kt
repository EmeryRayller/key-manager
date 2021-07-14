package me.rayll.pix.remove

import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.pix.clients.ClientBCB
import me.rayll.pix.clients.DeletePixKeyRequest
import me.rayll.pix.registrar.ValidUUID
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.exceptions.AcessoNegadoException
import me.rayll.pix.shared.exceptions.ChavePixNaoExistenteException
import javax.transaction.Transactional
import javax.validation.Validator
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val validator: Validator,
    @Inject val clienteBCB: ClientBCB) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID("Client id com formato invalido") clientId: String,
        @NotBlank @ValidUUID("Pix id com formato invalido") pixId: String
    ) {

        val erros = validator.validate(arrayOf(clientId, pixId))

        if (erros.isEmpty()) {

            val chave = repository.findByIdAndClientId(pixId, clientId)
                .orElseThrow { throw ChavePixNaoExistenteException("Chave pix não encontrada ou não pertence ao cliente.") }


            try {

                val deletePixKeyRequest = DeletePixKeyRequest(chave.chave, chave.conta.nomeDoTitular)
                clienteBCB.delete(chave.chave, deletePixKeyRequest).also {

                    if (it.status == HttpStatus.FORBIDDEN) throw AcessoNegadoException(it.status.reason)
                    if (it.status == HttpStatus.NOT_FOUND) throw ChavePixNaoExistenteException(it.status.reason)

                }

                repository.deleteById(pixId)

            } catch (ex: Exception) {
                throw ex
            }
        }
    }
}

