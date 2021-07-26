package me.rayll.pix.registrar

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.pix.clients.BuscarClientItau
import me.rayll.pix.clients.ClientBCB
import me.rayll.pix.clients.CreatePixKeyResponse
import me.rayll.pix.clients.CreatePixKeyRequest
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.exceptions.ChavePixExistenteException
import me.rayll.pix.shared.exceptions.DadosDoClienteNaoEncontratoException
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.transaction.Transactional


@Singleton
@Validated
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: BuscarClientItau,
    @Inject val clientBCB: ClientBCB) {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    //@Transactional
    fun registra(novaChave: NovaChavePix): ChavePix {

        //1 - verifica a existência da chave no banco
        if (repository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix ${novaChave.chave} existente")
        }

        //2 - busca os dados no serviço do itaú
        val clientResponse: HttpResponse<DadosDoClienteEmBanco> =
            itauClient.buscarConta(novaChave.clientId, novaChave.tipoDeConta!!.name)
        val contaAssociada =
            clientResponse.body() ?: throw DadosDoClienteNaoEncontratoException("Cliente não encontrado no Itau.")

        //3 - converte para a entidade e salva no banco
        var chave = novaChave.paraChavePix(contaAssociada.paraContaAssociada())

        //4 - converte a chave para um tipo para fazer a request na api do bacen
        val bacenRequest = CreatePixKeyRequest.of(chave)
        LOGGER.info("Registrando a chave pix no serviço externo do bacen: \\n")

        //5 - salvando a request no bacen e na nossa base
        try {
            //5.1 - salvar a chave no bacen e retorna exceção caso status diferente de 201
            val bacenResponse: HttpResponse<CreatePixKeyResponse> = clientBCB.create(bacenRequest)
                .also {
                    if (it.status != HttpStatus.CREATED) {
                        throw IllegalStateException("Erro ao registrar chave pix no Banco Central")
                    }
                }

            //5.2 - troca a chave da response do bacen na nossa entidade para salvar no bando
            //pois a chave aleatória vira do bacen
            chave.chave = bacenResponse.body()!!.key
            repository.save(chave)

        } catch (ex: Exception) {
            throw ex
        }
            return chave

    }
}
