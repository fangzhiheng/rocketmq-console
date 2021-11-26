package ie.fzh.rocketmq.console.model

import ie.fzh.rocketmq.console.common.failure
import org.apache.rocketmq.common.TopicConfig
import org.apache.rocketmq.common.constant.PermName
import javax.ws.rs.core.Response

abstract class AbstractResource {
    abstract fun validateCreate()
    abstract fun validateUpdate()
}

data class Cluster(
    val name: String,
    val brokers: List<Broker>
)

data class Broker(
    val cluster: String?,
    val name: String?,
    val address: Map<Long, String>?
)

enum class TopicType {
    USER,
    SYSTEM
}

data class Queue(
    val broker: String?,
    val read: Int = 4,
    val write: Int = 4,
    val permission: Int = -1
)

data class Group(
    var name: String?
)

data class Topic(
    var name: String,
    var brokers: List<Broker>?,
    var queues: List<Queue>?,
    var groups: List<Group>?,
    var type: TopicType? = TopicType.USER,
    var order: Map<String, Int>? = null,
    var permission: Int? = PermName.PERM_READ or PermName.PERM_WRITE // unset
) : AbstractResource() {
    fun toTopicConfig(): TopicConfig {
        val config = TopicConfig()
        config.topicName = name
        if (queues != null) {
            config.readQueueNums = if (queues!!.isEmpty()) TopicConfig.defaultReadQueueNums else queues!![0].read
            config.writeQueueNums = if (queues!!.isEmpty()) TopicConfig.defaultWriteQueueNums else queues!![0].write
        }
        config.isOrder = order != null
        if (permission != null) {
            config.perm = permission!!
        }
        return config
    }

    fun toOrderConf(): String? {
        if (order == null) {
            return null
        }
        return order!!.map { it.key + ":" + it.value }.joinToString(";")
    }

    fun parseOrderConf(conf: String?) {
        if (conf == null) {
            return
        }
        val order: MutableMap<String, Int> = mutableMapOf()
        val brokersConf = conf.split(";")
        for (brokerConf in brokersConf) {
            val split = brokerConf.split(":")
            if (split.size < 2) {
                continue
            }
            order[split[0]] = Integer.parseInt(split[1])
        }

        this.order = order
    }

    override fun validateCreate() {
        if (this.brokers == null) {
            throw ConsoleException(Response.Status.BAD_REQUEST, failure(PARAMETER_MISSED, "brokers"))
        }

        if (this.queues == null) {
            throw ConsoleException(Response.Status.BAD_REQUEST, failure(PARAMETER_MISSED, "queues"))
        }

        if (this.permission == null) {
            throw ConsoleException(Response.Status.BAD_REQUEST, failure(PARAMETER_MISSED, "permission"))
        }
    }

    override fun validateUpdate() {
        if (this.brokers == null) {
            throw ConsoleException(Response.Status.BAD_REQUEST, failure(PARAMETER_MISSED, "borkers"))
        }
    }
}