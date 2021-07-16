package me.rayll.pix.listar

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import me.rayll.KeymanagerListaServiceGrpc
import me.rayll.ListaChavesPixRequest
import me.rayll.TipoDeChave
import me.rayll.pix.Persistencia
import me.rayll.pix.repository.ChavePixRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository: ChavePixRepository,
    val grpcLista: KeymanagerListaServiceGrpc.KeymanagerListaServiceBlockingStub
) {

    val persistencia = Persistencia()

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        persistencia
        repository.save(persistencia.chave(
            tipoChave = me.rayll.pix.registrar.TipoDeChave.EMAIL,
            chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID.toString()))
        repository.save(persistencia.chave(
            tipoChave = me.rayll.pix.registrar.TipoDeChave.ALEATORIA,
            chave = "randomkey-2", clienteId = UUID.randomUUID().toString()))
        repository.save(persistencia.chave(
            tipoChave = me.rayll.pix.registrar.TipoDeChave.ALEATORIA,
            chave = "randomkey-3",
            clienteId = CLIENTE_ID.toString()))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {

        // cenário
        val clienteId = CLIENTE_ID.toString()

        // ação
        val response = grpcLista.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        // validação
        with (response.chavesList) {
            assertThat(this).hasSize(2)
            assertThat(this.map { Pair(it.tipo, it.chave) }.toList())
                .containsAnyElementsOf(
                    mutableListOf(Pair(TipoDeChave.valueOf(me.rayll.pix.registrar.TipoDeChave.ALEATORIA.name), "randomkey-3"),
                    Pair(TipoDeChave.valueOf(me.rayll.pix.registrar.TipoDeChave.EMAIL.name), "rafael.ponte@zup.com.br"))
                )
        }
    }

    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcLista.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteSemChaves)
            .build())

        // validação
        assertEquals(0, response.chavesCount)

    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val erros = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcLista.lista(
                ListaChavesPixRequest.newBuilder()
                    .setClienteId(clienteIdInvalido)
                    .build()
            )
        }

        // validação
        with(erros) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertThat(status.description).containsIgnoringCase("Cliente ID não pode ser nulo ou vazio")
        }
    }
}