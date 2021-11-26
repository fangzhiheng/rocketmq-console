package ie.fzh.rocketmq.console.common

data class R<T>(
    val succeed: Boolean,
    val code: String,
    val data: T?
)

fun succeed(): R<Any> {
    return R(true, "", null)
}

fun <T> succeed(data: T?): R<T> {
    return R(true, "", data)
}

fun failure(code: String): R<Any> {
    return R(false, code, null)
}

fun <T> failure(code: String, data: T?): R<T> {
    return R(false, code, data)
}


