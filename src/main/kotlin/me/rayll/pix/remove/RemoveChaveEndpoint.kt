package me.rayll.pix.remove

import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.rayll.KeyManagerRemoveServiceGrpc
import me.rayll.RemoveChavePixRequest
import me.rayll.RemoveChavePixResponse
import me.rayll.pix.shared.handlers.ErrorAroundHandler

@ErrorAroundHandler
@Singleton
class RemoveChaveEndpoint(
    @Inject private val service: RemoveChaveService
) : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {

        service.remove(clientId = request.clientId, pixId = request.pixId)

        responseObserver.onNext(
            RemoveChavePixResponse.newBuilder()
                .setClientId(request.clientId)
                .setPixId(request.pixId)
                .build()
        )
        responseObserver.onCompleted()
    }
}