package ie.fzh.rocketmq.console.service

import ie.fzh.rocketmq.console.common.AbstractService
import ie.fzh.rocketmq.console.common.Slf4j
import ie.fzh.rocketmq.console.common.Slf4j.Companion.log
import ie.fzh.rocketmq.console.common.failure
import ie.fzh.rocketmq.console.model.*
import kotlinx.coroutines.*
import org.apache.rocketmq.client.exception.MQClientException
import org.apache.rocketmq.client.impl.MQClientAPIImpl
import org.apache.rocketmq.client.impl.factory.MQClientInstance
import org.apache.rocketmq.client.producer.DefaultMQProducer
import org.apache.rocketmq.common.namesrv.NamesrvUtil
import org.apache.rocketmq.common.protocol.ResponseCode
import org.apache.rocketmq.common.protocol.body.GroupList
import org.apache.rocketmq.common.protocol.route.TopicRouteData
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Default
import javax.inject.Inject

@Slf4j
@ApplicationScoped
class TopicService : AbstractService() {

    @Inject
    lateinit var mqClientInstance: MQClientInstance

    @Inject
    @field: Default
    lateinit var mqClientAPIImpl: MQClientAPIImpl

    @Inject
    @field: Default
    lateinit var producer: DefaultMQProducer

    fun findAll(request: FindAllTopicRequest) = runBlocking {
        val topics: MutableList<Deferred<List<Topic>>> = mutableListOf()
        if (request.type == null || request.type == TopicType.USER) {
            topics.add(async(Dispatchers.IO) {
                return@async admin.fetchAllTopicList().topicList
                    .map { return@map findTopic(it, false) }
            })
        }
        if (request.type == null || request.type == TopicType.SYSTEM) {
            topics.add(async(Dispatchers.IO) {
                return@async mqClientAPIImpl.getSystemTopicList(20000L).topicList
                    .map { return@map findTopic(it, true) }
            })
        }
        return@runBlocking topics.map { it.await() }.flatten().toList()
    }

    fun findTopic(name: String, system: Boolean) = runBlocking {
        val routeInfo = async(Dispatchers.IO) {
            try {
                admin.examineTopicRouteInfo(name)
            } catch (e: Exception) {
                if (e is MQClientException && e.responseCode == ResponseCode.TOPIC_NOT_EXIST) {
                    val data = TopicRouteData()
                    data.brokerDatas = emptyList()
                    data.queueDatas = emptyList()
                    data.filterServerTable = HashMap()
                    data.orderTopicConf = null
                    data
                } else throw e
            }
        }
        val groupList = async(Dispatchers.IO) {
            try {
                admin.queryTopicConsumeByWho(name)
            } catch (e: Exception) {
                if (e is MQClientException && e.responseCode == ResponseCode.TOPIC_NOT_EXIST) GroupList() else throw e
            }
        }
        val orderConf = async(Dispatchers.IO) {
            try {
                admin.getKVConfig(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, name)
            } catch (e: Exception) {
                if (e is MQClientException && e.responseCode == ResponseCode.QUERY_NOT_FOUND) null else throw e
            }
        }
        val brokers = routeInfo.await().brokerDatas.map { data ->
            Broker(
                cluster = data.cluster,
                name = data.brokerName,
                address = data.brokerAddrs
            )
        }

        val queues = routeInfo.await().queueDatas.map { queue ->
            Queue(
                broker = queue.brokerName,
                read = queue.readQueueNums,
                write = queue.writeQueueNums,
                permission = queue.perm
            )
        }

        val groups = groupList.await().groupList.map { group ->
            Group(name = group)
        }

        val topic = Topic(
            name = name,
            brokers = brokers,
            queues = queues,
            groups = groups,
        )

        if (system) {
            topic.type = TopicType.SYSTEM
        }

        topic.parseOrderConf(orderConf.await())
        return@runBlocking topic
    }

    fun find(): Unit {
        // TODO
    }

    fun update(topic: Topic) = runBlocking {
        val clusterInfo = admin.examineBrokerClusterInfo()
        val brokerNames = topic.brokers!!.map { it.name }.distinct().toSet()
        val brokerAddrTable = clusterInfo.brokerAddrTable

        for (brokerName in brokerNames) {
            val brokerData = brokerAddrTable[brokerName]
            if (brokerData == null) {
                log.warn("Create topic {} on a missed broker {}, ignored.", topic.name, brokerName)
                continue
            }
            launch(Dispatchers.IO) {
                admin.createAndUpdateTopicConfig(getMasterAddr(brokerData), topic.toTopicConfig())
            }
        }

        val orderConf = topic.toOrderConf()
        if (orderConf != null) {
            admin.createOrUpdateOrderConf(topic.name, orderConf, true)
        } else {
            admin.deleteKvConfig(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, topic.name)
        }
    }

    fun delete(topic: String) = runBlocking {
        val systemTopics = mqClientAPIImpl.getSystemTopicList(20000L).topicList
        if (systemTopics.contains(topic)) {
            throw ConsoleException(failure(TOPIC_UNABLE_DELETE))
        }
        val routeInfo = admin.examineTopicRouteInfo(topic)
        val addrs = routeInfo.brokerDatas.flatMap { it.brokerAddrs.values }.toSet()
        launch(Dispatchers.IO) { admin.deleteTopicInBroker(addrs, topic) }
        launch(Dispatchers.IO) {
            val namesrvAddrs: MutableSet<String> = mutableSetOf()
            for (addr in addrs) {
                val namrsrvAddr: String = admin.getBrokerConfig(addr)["namesrvAddr"].toString()
                namesrvAddrs.addAll(namrsrvAddr.split(";"))
            }
            admin.deleteTopicInNameServer(namesrvAddrs, topic)
        }
    }

    fun exists(name: String) = runBlocking {
        return@runBlocking awaitAll(
            async(Dispatchers.IO) { admin.fetchAllTopicList().topicList },
            async(Dispatchers.IO) { mqClientAPIImpl.getSystemTopicList(20000L).topicList },
        ).flatten().contains(name)
    }
}