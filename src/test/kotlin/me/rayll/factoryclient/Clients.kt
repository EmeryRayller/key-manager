package me.rayll.factoryclient

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import me.rayll.KeyManagerRemoveServiceGrpc
import me.rayll.KeyManagerServiceGrpc


//isso aqui é para criar a factory de client, ou seja, o que vai consumir nosso servidor
@Factory
class ClientRegistra {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) :
            KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {

        return KeyManagerServiceGrpc.newBlockingStub(channel)

    }
}


//isso aqui é para criar a factory de client, ou seja, o que vai consumir nosso servidor
//esse client é o de deletar
@Factory
open class ClientRemove {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) :
            KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub? {

        return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)

    }
}