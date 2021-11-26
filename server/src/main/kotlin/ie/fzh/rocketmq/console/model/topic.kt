package ie.fzh.rocketmq.console.model

data class FindAllTopicRequest(
    var page: Int = 1,
    var pageSize: Int = 10,
    var topic: String?,
    var type: TopicType?
)