package me.rayll.pix.remove

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import me.rayll.KeyManagerRemoveServiceGrpc
import me.rayll.KeyManagerServiceGrpc
import me.rayll.RemoveChavePixRequest
import me.rayll.pix.Persistencia
import me.rayll.pix.clients.BuscarClientItau
import me.rayll.pix.clients.ClientBCB
import me.rayll.pix.registrar.ChavePix
import me.rayll.pix.registrar.RegistraChaveEndpointTest
import me.rayll.pix.repository.ChavePixRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClientRegistra: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub,
    val grpcClientRemove: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub
) {

    val persistencia = Persistencia()

    @Inject
    lateinit var itauClientItau: BuscarClientItau

    @Inject
    lateinit var bcbClient: ClientBCB

    companion object {
        val CLIENT_ID = UUID.randomUUID().toString()
    }

    private lateinit var chave: ChavePix

    @BeforeEach
    internal fun setUp() {
        `when`(itauClientItau.buscarConta(clienteId = Persistencia.CLIENT_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(persistencia.dadosDaContaResponse()))

        `when`(bcbClient.delete(persistencia.retornaChave().chave, persistencia.retornaDeletePixKeyRequest()))
            .thenReturn(HttpResponse.ok(persistencia.retornaDeletePixKeyResponse()))

        repository.deleteAll() //deletar primeiro todo o banco

        chave = repository.save(persistencia.retornaChave()) // salvar o primeiro registro para testar
    }

    @Test
    fun `deve deletar a chave pix`() {

        //a????o
        val response = grpcClientRemove.remove(
            RemoveChavePixRequest.newBuilder()
                .setClientId(chave.clientId)
                .setPixId(chave.id)
                .build()
        )

        //valida????o
        with(response) {
            assertEquals(chave.clientId, clientId)
            assertEquals(chave.id, pixId)
        }
    }

    @Test
    fun `nao deve deletar a chave com cliente nao encontrado` () {

        //a????o
        val response = assertThrows<StatusRuntimeException> { grpcClientRemove.remove(
            RemoveChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(UUID.randomUUID().toString())
                .build()
        ) }



        //valida????o
        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertThat(status.description).contains("Chave pix n??o encontrada ou n??o pertence ao cliente.")
        }
    }

    @Test
    fun `nao deve deletar quando argumento invalido` () {

        //a????o
        val response = assertThrows<StatusRuntimeException> {
            grpcClientRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setPixId("12345678910")
                    .build()
            )
        }


        //valida????o
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertThat(status.description).contains("Pix id com formato invalido")
        }
    }

    @MockBean(BuscarClientItau::class)
    fun itauClient(): BuscarClientItau? {
        return mock(BuscarClientItau::class.java)
    }

    @MockBean(ClientBCB::class)
    fun clienteBCB(): ClientBCB? {
        return mock(ClientBCB::class.java)
    }
}