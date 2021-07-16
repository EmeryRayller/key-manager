package me.rayll.pix.shared.handlers

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import me.rayll.pix.shared.exceptions.AcessoNegadoException
import me.rayll.pix.shared.exceptions.ChavePixExistenteException
import me.rayll.pix.shared.exceptions.ChavePixNaoExistenteException
import me.rayll.pix.shared.exceptions.DadosDoClienteNaoEncontratoException
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {

            return context.proceed()
        }catch (ex: Exception){

            val responseObserver = context.parameterValues[1] as StreamObserver<*>


            val status = when(ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT.withCause(ex).withDescription(ex.message)
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withCause(ex).withDescription(ex.message)
                is DadosDoClienteNaoEncontratoException -> Status.FAILED_PRECONDITION.withCause(ex).withDescription(ex.message)
                is ChavePixNaoExistenteException -> Status.NOT_FOUND.withCause(ex).withDescription(ex.message)
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withCause(ex).withDescription(ex.message)
                is AcessoNegadoException -> Status.ABORTED.withCause(ex).withDescription(ex.message)
                else -> Status.UNKNOWN
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}