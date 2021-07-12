package me.rayll.pix.registrar

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import me.rayll.KeyManagerServiceGrpc
import me.rayll.RegistraChavePixRequest
import me.rayll.TipoDeChave
import me.rayll.TipoDeConta
import me.rayll.pix.Persistencia
import me.rayll.pix.repository.ChavePixRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
){

    val persistencia = Persistencia()

    @Inject
    lateinit var itauClientItau: BuscarClientItau

    companion object {
        val CLIENT_ID = UUID.randomUUID().toString()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {
        //cenario
        `when`(itauClientItau.buscarConta(clienteId = CLIENT_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(persistencia.dadosDaContaResponse()))

        //ação
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID)
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        //validação
        with(response) {
            assertEquals(CLIENT_ID, clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando existente`() {

        //cenario
        repository.save(persistencia.retornaChave())

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("63657520325")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix 63657520325 existente", status.description)
        }

    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta do cliente`() {

        //cenario
        `when`(itauClientItau.buscarConta(clienteId = CLIENT_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(response) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau.", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando os parametros forem invalidos`() {

        //ação
        val response = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder().build()
            )
        }

        //validação
        with(response) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            //assertEquals("Dados invalidos", status.description)
        }
    }

    @MockBean(BuscarClientItau::class)
    fun itauClient(): BuscarClientItau? {
        return mock(BuscarClientItau::class.java)
    }

}