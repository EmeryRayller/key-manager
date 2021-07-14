package me.rayll.pix.registrar
import me.rayll.TipoDeConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    val clientId: String,

    @field:NotNull
    val tipoDeChave: TipoDeChave,

    @field:NotEmpty
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoDeConta: TipoDeConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    var id: String = UUID.randomUUID().toString()

    val criadaEm = LocalDateTime.now()

}
