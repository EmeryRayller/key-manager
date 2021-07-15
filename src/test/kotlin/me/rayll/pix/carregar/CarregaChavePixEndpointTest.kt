package me.rayll.pix.carregar

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import me.rayll.CarregaChavePixRequest
import me.rayll.KeymanagerCarregaGrpcServiceGrpc
import me.rayll.pix.Persistencia
import me.rayll.pix.clients.*
import me.rayll.pix.registrar.ChavePix
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.violations
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CarregaChavePixEndpointTest(
    val repository: ChavePixRepository,
    @Inject val grpcCarrega: KeymanagerCarregaGrpcServiceGrpc.KeymanagerCarregaGrpcServiceBlockingStub
) {

    val persistencia = Persistencia()

    @Inject
    lateinit var bcbClient: ClientBCB

    private lateinit var chave: ChavePix

    @BeforeEach
    internal fun setUp() {

        repository.deleteAll() //deletar primeiro todo o banco
        chave = repository.save(persistencia.retornaChave()) // salvar o primeiro registro para testar
    }

    @Test
    fun `deve carregar chave por pixId e clientId`() {

        //cenario
        val chaveEncontrada = repository.findByChave(persistencia.retornaChave().chave).get()

        //ação
        val response = grpcCarrega.carrega(
            CarregaChavePixRequest.newBuilder()
                .setPixId(
                    CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chave.id)
                        .setClienteId(chave.clientId)
                        .build()
                ).build())

        //validação
        with(response) {
            assertEquals(chaveEncontrada.id, this.pixId)
            assertEquals(chaveEncontrada.clientId, this.clienteId)
            assertEquals(chaveEncontrada.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {

        //ação
        val erros = assertThrows<StatusRuntimeException> {
            grpcCarrega.carrega(
                CarregaChavePixRequest.newBuilder()
                    .setPixId(
                        CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    ).build())
        }

        val description = erros.status.description

        val toSplit = description?.split(",")?.map {
            it.trim()
        }

        with(erros) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertThat(toSplit)
                .contains("pixId: não deve estar em branco")
                .contains("clienteId: não deve estar em branco")
                .contains("pixId: não é um uuid")
                .contains("clienteId: não é um uuid")

        }

    }

    @Test
    fun `nao deve carregar por pixId e clientId quando nao existir`() {

        // ação
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows<StatusRuntimeException> {
            grpcCarrega.carrega(CarregaChavePixRequest.newBuilder()
                .setPixId(
                    CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(clienteIdNaoExistente)
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertThat(status.description).containsIgnoringCase("Chave Pix não encontrada", )
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {

        //cenário
        Mockito.`when`(bcbClient.findByKey("algumacoisa@gmail.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val chaveNaoEncontrada = repository.findByChave(UUID.randomUUID().toString())

        //ação
        val response = grpcCarrega.carrega(
            CarregaChavePixRequest.newBuilder()
                .setChave("algumacoisa@gmail.com")
                .build())

        //validação
        assertTrue(chaveNaoEncontrada.isEmpty)
        assertNotNull(response)
        assertEquals(response.chave.chave, pixKeyDetailsResponse().key)
    }

    @MockBean(ClientBCB::class)
    fun clienteBCB(): ClientBCB? {
        return Mockito.mock(ClientBCB::class.java)
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "algumacoisa@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }
    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }
}