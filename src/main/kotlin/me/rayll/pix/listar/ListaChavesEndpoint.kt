package me.rayll.pix.listar

import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import me.rayll.*
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.handlers.ErrorAroundHandler
import java.time.ZoneId

@ErrorAroundHandler
@Singleton
class ListaChavesEndpoint(
    val repository: ChavePixRepository
) : KeymanagerListaServiceGrpc.KeymanagerListaServiceImplBase() {

    override fun lista(request: ListaChavesPixRequest, responseObserver: StreamObserver<ListaChavesPixResponse>) {

        if (request.clienteId.isNullOrBlank()) {
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio.")
        }

        val chaves = repository.findAllByClientId(request.clienteId).map {

            ListaChavesPixResponse.ChavePix.newBuilder() // 2
                .setPixId(it.id.toString())
                .setTipo(TipoDeChave.valueOf(it.tipoDeChave.name)) // 1
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name)) // 1
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder() // 1
            .setClienteId(request.clienteId.toString())
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }
}