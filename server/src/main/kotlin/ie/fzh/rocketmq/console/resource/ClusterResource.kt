package ie.fzh.rocketmq.console.resource

import ie.fzh.rocketmq.console.service.ClusterService
import kotlinx.coroutines.runBlocking
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/cluster")
@Produces(MediaType.APPLICATION_JSON)
class ClusterResource {

    @Inject
    @field: Default
    lateinit var clusterService: ClusterService

    @GET
    fun find() = runBlocking {
        clusterService.findAll()
    }
}