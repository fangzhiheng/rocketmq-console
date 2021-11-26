package ie.fzh.rocketmq.console.common

import ie.fzh.rocketmq.console.common.Slf4j.Companion.log
import ie.fzh.rocketmq.console.model.ConsoleException
import ie.fzh.rocketmq.console.model.UNKNOWN_ERROR
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Slf4j
@Provider
class ExceptionHandler : ExceptionMapper<Exception> {

    @Context
    lateinit var info: UriInfo

    override fun toResponse(exception: Exception?): Response {
        if (exception is ConsoleException) {
            return toResponse(exception)
        }
        return toUnknownResponse(exception)
    }

    private fun toResponse(exception: ConsoleException): Response {
        return Response.status(exception.status)
            .entity(exception.body)
            .build()
    }

    private fun toUnknownResponse(error: Exception?): Response {
        log.error("Unknown error occurs when access '${info.path}'", error)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(failure(UNKNOWN_ERROR))
            .build()
    }
}