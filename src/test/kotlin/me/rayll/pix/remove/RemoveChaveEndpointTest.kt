package me.rayll.pix.remove

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import me.rayll.*
import me.rayll.pix.Persistencia
import me.rayll.pix.registrar.BuscarClientItau
import me.rayll.pix.registrar.ChavePix
import me.rayll.pix.registrar.RegistraChaveEndpointTest
import me.rayll.pix.repository.ChavePixRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.*
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

    companion object {
        val CLIENT_ID = UUID.randomUUID().toString()
    }

    private lateinit var chave: ChavePix

    @BeforeEach
    internal fun setUp() {
        `when`(itauClientItau.buscarConta(clienteId = CLIENT_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(persistencia.dadosDaContaResponse()))
        repository.deleteAll() //deletar primeiro todo o banco
        chave = repository.save(persistencia.retornaChave()) // salvar o primeiro registro para testar
    }

    @Test
    fun `deve deletar a chave pix`() {

        //ação
        val response = grpcClientRemove.remove(
            RemoveChavePixRequest.newBuilder()
                .setClientId(chave.clientId)
                .setPixId(chave.id)
                .build()
        )

        //validação
        with(response) {
            assertEquals(chave.clientId, clientId)
            assertEquals(chave.id, pixId)
        }
    }

    @Test
    fun `nao deve deletar a chave com cliente nao encontrado` () {

        //ação
        val response = assertThrows<StatusRuntimeException> { grpcClientRemove.remove(
            RemoveChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(UUID.randomUUID().toString())
                .build()
        ) }



        //validação
        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente.", status.description)
        }
    }

    @Test
    fun `nao deve deletar quando argumento invalido` () {

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClientRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setPixId("12345678910")
                    .build()
            )
        }


        //validação
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
           // assertEquals("Pix id com formato invalido", status.description)
        }
    }

    @MockBean(BuscarClientItau::class)
    fun itauClient(): BuscarClientItau? {
        return mock(BuscarClientItau::class.java)
    }
}