package me.rayll.pix.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import me.rayll.pix.registrar.DadosDoClienteEmBanco


@Client("http://localhost:9091")
interface BuscarClientItau {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscarConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDoClienteEmBanco>
}