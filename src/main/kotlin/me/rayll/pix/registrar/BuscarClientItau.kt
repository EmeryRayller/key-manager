package me.rayll.pix.registrar

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("http://localhost:9091")
interface BuscarClientItau {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscarConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDoClienteEmBanco>
}