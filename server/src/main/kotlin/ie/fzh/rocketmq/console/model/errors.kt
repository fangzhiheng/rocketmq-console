package ie.fzh.rocketmq.console.model

import ie.fzh.rocketmq.console.common.R
import javax.ws.rs.core.Response

class ConsoleException : RuntimeException {
    var body: R<Any>?
    var status: Response.Status

    constructor(message: String, throwable: Throwable, status: Response.Status, body: R<Any>? = null) : super(
        message,
        throwable
    ) {
        this.status = status
        this.body = body
    }

    constructor(throwable: Throwable, status: Response.Status, body: R<Any>? = null) : super(throwable) {
        this.status = status
        this.body = body
    }

    constructor(status: Response.Status, body: R<Any>?) : super() {
        this.status = status
        this.body = body
    }

    constructor(body: R<Any>? = null) : this(Response.Status.OK, body)
}

const val UNKNOWN_ERROR = "error.unknown"

const val PARAMETER_INVALID = "error.parameter.invalid"
const val PARAMETER_MISSED = "error.parameter.missed"

const val TOPIC_NOT_EXISTS = "error.topic.not-exists"
const val TOPIC_EXISTS = "error.topic.exists"
const val TOPIC_UNABLE_DELETE = "error.topic.unable-to-delete"