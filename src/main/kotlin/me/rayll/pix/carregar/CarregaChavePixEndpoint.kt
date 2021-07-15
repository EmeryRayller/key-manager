package me.rayll.pix.carregar

import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import me.rayll.CarregaChavePixRequest
import me.rayll.CarregaChavePixResponse
import me.rayll.KeymanagerCarregaGrpcServiceGrpc
import me.rayll.pix.clients.ClientBCB
import me.rayll.pix.repository.ChavePixRepository
import me.rayll.pix.shared.handlers.ErrorAroundHandler
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class CarregaChavePixEndpoint(
    val validator: Validator,
    val bcbClient: ClientBCB,
    val repository: ChavePixRepository
) : KeymanagerCarregaGrpcServiceGrpc.KeymanagerCarregaGrpcServiceImplBase(){

    override fun carrega(request: CarregaChavePixRequest, responseObserver: StreamObserver<CarregaChavePixResponse>) {

        val filtro = request.toModel(validator)
        val chaveInfo =filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo)) // 1
        responseObserver.onCompleted()
    }
}