package me.rayll.pix.carregar

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import me.rayll.pix.clients.ClientBCB
import me.rayll.pix.registrar.ValidUUID
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.exceptions.ChavePixNaoExistenteException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: ClientBCB): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: ClientBCB): ChavePixInfo {

             val retorno = repository.findById(pixId)
                .filter { it.clientId.equals(clienteId)}
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixNaoExistenteException("Chave pix não encontrada") }

            return retorno
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String): Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: ClientBCB): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    val response = bcbClient.findByKey(chave)

                    when(response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixNaoExistenteException("Chave Pix não encontrada.")
                    }
                }
        }
    }

    @Introspected
     class Invalida(): Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: ClientBCB): ChavePixInfo {

            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }

    }
}
