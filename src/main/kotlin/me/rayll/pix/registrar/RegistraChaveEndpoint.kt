package me.rayll.pix.registrar

import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.KeyManagerServiceGrpc
import me.rayll.RegistraChavePixRequest
import me.rayll.RegistraChavePixResponse
import me.rayll.pix.shared.handlers.ErrorAroundHandler
import javax.validation.Validator

@Singleton
@ErrorAroundHandler
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService, @Inject val validator: Validator)
    : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChave: NovaChavePix = request.paraNovaChavePix(validator)
        val chavePix = service.registra(novaChave)
        responseObserver.onNext(
            RegistraChavePixResponse.newBuilder()
                .setClienteId(chavePix.clientId)
                .setPixId(chavePix.id)
                .build())
        responseObserver.onCompleted()
    }
}