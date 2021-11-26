package ie.fzh.rocketmq.console.service

import ie.fzh.rocketmq.console.common.AbstractService
import ie.fzh.rocketmq.console.model.Broker
import ie.fzh.rocketmq.console.model.Cluster
import kotlinx.coroutines.runBlocking
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ClusterService : AbstractService() {

    fun findAll() = runBlocking {
        val clusterInfo = admin.examineBrokerClusterInfo()
        clusterInfo.clusterAddrTable.map {
            Cluster(it.key, it.value.filter { broker -> broker in clusterInfo.brokerAddrTable }
                .map { broker ->
                    Broker(
                        it.key,
                        broker,
                        clusterInfo.brokerAddrTable[broker]!!.brokerAddrs
                    )
                }
                .toList()
            )
        }
    }
}