package me.rayll.pix.carregar

import com.google.protobuf.Timestamp
import me.rayll.CarregaChavePixResponse
import me.rayll.TipoDeChave
import me.rayll.TipoDeConta
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(CarregaChavePixResponse.ChavePix // 1
                .newBuilder()
                .setTipo(TipoDeChave.valueOf(chaveInfo.tipoChave.name)) // 2
                .setChave(chaveInfo.chave)
                .setConta(CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder() // 1
                    .setTipo(TipoDeConta.valueOf(chaveInfo.tipoDeConta.name)) // 2
                    .setInstituicao(chaveInfo.conta.instituicao) // 1 (Conta)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}
