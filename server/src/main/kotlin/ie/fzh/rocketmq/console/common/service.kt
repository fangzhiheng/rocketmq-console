package ie.fzh.rocketmq.console.common

import org.apache.rocketmq.common.MixAll
import org.apache.rocketmq.common.protocol.route.BrokerData
import org.apache.rocketmq.tools.admin.MQAdminExt
import javax.inject.Inject

abstract class AbstractService {

    @Inject
    lateinit var admin: MQAdminExt

    protected fun getMasterAddr(data: BrokerData): String? {
        return data.brokerAddrs[MixAll.MASTER_ID]
    }
}