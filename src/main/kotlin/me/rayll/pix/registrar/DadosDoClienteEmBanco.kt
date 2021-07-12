package me.rayll.pix.registrar

import javax.persistence.Embeddable
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class DadosDoClienteEmBanco(
    @field:NotEmpty
    val tipo: String,
    @field:NotNull
    val instituicao: InstituicaoResponse,
    @field:NotEmpty
    val agencia: String,
    @field:NotEmpty
    val numero: String,
    @field:NotNull
    val titular: TitularResponse
) {
    fun paraContaAssociada() = ContaAssociada(
        instituicao = this.instituicao.nome,
        nomeDoTitular = this.titular.nome,
        cpfDoTitular = this.titular.cpf,
        agencia = this.agencia,
        numeroDaConta = this.numero,
    )
}

@Embeddable
data class ContaAssociada(
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String){

    companion object { val ITAU_UNIBANCO_ISPB: String = "60701190" }
}

data class TitularResponse(
    val nome: String,
    val cpf: String
)

data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)
