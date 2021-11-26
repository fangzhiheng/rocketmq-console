package ie.fzh.rocketmq.console

import io.quarkus.runtime.ShutdownEvent
import org.apache.rocketmq.acl.common.AclClientRPCHook
import org.apache.rocketmq.acl.common.SessionCredentials
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer
import org.apache.rocketmq.client.impl.MQClientAPIImpl
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl
import org.apache.rocketmq.client.impl.factory.MQClientInstance
import org.apache.rocketmq.client.producer.DefaultMQProducer
import org.apache.rocketmq.common.MixAll
import org.apache.rocketmq.remoting.RPCHook
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt
import org.apache.rocketmq.tools.admin.MQAdminExt
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import javax.enterprise.context.Dependent
import javax.enterprise.event.Observes
import javax.enterprise.inject.Produces
import javax.inject.Singleton

@Dependent
class RocketMQProvider {
    private val log: Logger = LoggerFactory.getLogger("RocketMQProvider")

    @ConfigProperty(name = "rocketmq.namesrvAddr", defaultValue = "")
    var namesrvAddr: String? = null

    @ConfigProperty(name = "rocketmq.acl.enabled", defaultValue = "false")
    var aclEnabled: Boolean = false

    @ConfigProperty(name = "rocketmq.acl.accessKey", defaultValue = "")
    var accessKey: String = ""

    @ConfigProperty(name = "rocketmq.acl.secretKey", defaultValue = "")
    var secretKey: String = ""

    private var rpcHook: RPCHook? = null
    private var mqAdminExt: DefaultMQAdminExt? = null
    private var producer: DefaultMQProducer? = null
    private var consumer: DefaultLitePullConsumer? = null

    private fun getNamesrvAddr0(): String {
        var addr = namesrvAddr
        if (addr == null) {
            addr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY)
        }
        if (addr == null) {
            addr = System.getenv(MixAll.NAMESRV_ADDR_ENV)
        }
        if (addr == null) {
            throw IllegalStateException()
        }
        return addr
    }

    private fun getRpcHook0(): RPCHook? {
        if (aclEnabled && rpcHook == null) {
            rpcHook = AclClientRPCHook(SessionCredentials(accessKey, secretKey))
        }
        return rpcHook
    }

    @Produces
    @Singleton
    fun mqadminExt(): MQAdminExt {
        if (mqAdminExt == null) {
            log.info("startup tool mqadmin")
            mqAdminExt = DefaultMQAdminExt(getRpcHook0())
            mqAdminExt!!.namesrvAddr = getNamesrvAddr0()
            mqAdminExt!!.start()
        }
        return mqAdminExt!!
    }

    @Produces
    @Singleton
    fun defaultLitePullConsumer(): DefaultLitePullConsumer {
        if (consumer == null) {
            log.info("startup tool consumer")
            consumer = DefaultLitePullConsumer(MixAll.TOOLS_CONSUMER_GROUP, getRpcHook0())
            consumer!!.namesrvAddr = getNamesrvAddr0()
            consumer!!.instanceName = "console-consumer-" + System.currentTimeMillis()
            consumer!!.start()
        }
        return consumer!!
    }

    @Produces
    @Singleton
    fun mQClientInstance(consumer: DefaultLitePullConsumer): MQClientInstance {
        val field = (DefaultLitePullConsumerImpl::class).java.getDeclaredField("mQClientFactory")
        val impl: DefaultLitePullConsumerImpl =
            get(consumer.javaClass.getDeclaredField("defaultLitePullConsumerImpl"), consumer)
        return get(field, impl)
    }

    @Produces
    @Singleton
    fun mQClientAPIImpl(instance: MQClientInstance): MQClientAPIImpl {
        return instance.mqClientAPIImpl
    }

    @Produces
    @Singleton
    fun defaultMQProducer(): DefaultMQProducer {
        if (producer == null) {
            producer = DefaultMQProducer(getRpcHook0())
            producer!!.producerGroup = MixAll.SELF_TEST_PRODUCER_GROUP
            producer!!.namesrvAddr = getNamesrvAddr0()
            producer!!.start()
        }
        return producer!!
    }

    fun onShutdown(@Observes event: ShutdownEvent) {
        if (mqAdminExt != null) {
            log.info("shutdown tool mqadmin")
            mqAdminExt?.shutdown()
        }

        if (producer != null) {
            log.info("shutdown tool consumer")
            producer?.shutdown()
        }

        if (consumer != null) {
            log.info("shutdown tool consumer")
            consumer?.shutdown()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R, T> get(field: Field, obj: T): R {
        if (!field.canAccess(obj)) {
            field.trySetAccessible()
        }
        return field.get(obj) as R
    }
}