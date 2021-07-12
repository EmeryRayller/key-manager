package me.rayll.pix.remove

import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.pix.registrar.ValidUUID
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.exceptions.ChavePixExistenteException
import me.rayll.pix.shared.exceptions.ChavePixNaoExistenteException
import javax.validation.Validator
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val validator: Validator) {

    fun remove(
        @NotBlank @ValidUUID("Client id com formato invalido") clientId: String,
        @NotBlank @ValidUUID("Pix id com formato invalido") pixId: String
    ) {

        val erros = validator.validate(arrayOf(clientId, pixId))

        if (erros.isEmpty()) {
            val chave = repository.findByIdAndClientId(pixId, clientId)
                .orElseThrow{ throw ChavePixNaoExistenteException("Chave pix não encontrada ou não pertence ao cliente.") }
            repository.deleteById(pixId)
        }
    }
}

