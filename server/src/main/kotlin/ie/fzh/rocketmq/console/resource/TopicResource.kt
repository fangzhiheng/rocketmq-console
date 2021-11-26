package ie.fzh.rocketmq.console.resource

import ie.fzh.rocketmq.console.common.R
import ie.fzh.rocketmq.console.common.failure
import ie.fzh.rocketmq.console.common.succeed
import ie.fzh.rocketmq.console.model.*
import ie.fzh.rocketmq.console.service.TopicService
import kotlinx.coroutines.runBlocking
import org.eclipse.microprofile.metrics.MetricUnits
import org.eclipse.microprofile.metrics.annotation.SimplyTimed
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/topic")
@Produces(MediaType.APPLICATION_JSON)
@SimplyTimed(
    name = "console.timed.resource.topic",
    unit = MetricUnits.MICROSECONDS,
    absolute = true
)
class TopicResource {

    @Inject
    @field: Default
    lateinit var topicService: TopicService

    @GET
    @APIResponse(
        responseCode = "200",
        description = "All Topics found",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = R::class)
        ), Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(name = "data", implementation = Topic::class)
        )]
    )
    fun findAll(
        @QueryParam("page")
        page: Int = 1,
        @QueryParam("pageSize")
        pageSize: Int = 10,
        @QueryParam("topic")
        topic: String?,
        @QueryParam("type")
        type: TopicType?
    ): Response = runBlocking {
        Response.ok(succeed(topicService.findAll(FindAllTopicRequest(page, pageSize, topic, type)))).build()
    }

    @GET
    @Path("/{topic}")
    @APIResponse(
        responseCode = "200",
        description = "Topic found",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = R::class)
        ), Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = Topic::class)
        )]
    )
    fun find(@PathParam("topic") topic: String): Response = runBlocking {
        // TODO
        Response.ok().build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(
        APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = Schema(implementation = R::class)
                )
            ]
        ),
        APIResponse(
            responseCode = "201",
            description = "Topic Created",
        ),
    )
    fun create(topic: Topic): Response = runBlocking {
        topic.validateCreate()
        if (topicService.exists(topic.name)) {
            return@runBlocking Response.status(Response.Status.BAD_REQUEST).entity(failure(TOPIC_EXISTS, topic.name)).build()
        }
        topicService.update(topic)
        Response.status(Response.Status.CREATED).entity(succeed(null)).build()
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(
        APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = Schema(implementation = R::class)
                )
            ]
        ),
        APIResponse(
            responseCode = "202",
            description = "Topic Accepted",
        ),
    )
    fun update(topic: Topic): Response = runBlocking {
        topic.validateUpdate()
        if (!topicService.exists(topic.name)) {
            return@runBlocking Response.status(Response.Status.BAD_REQUEST).entity(failure(TOPIC_NOT_EXISTS, topic.name)).build()
        }
        topicService.update(topic)
        Response.status(Response.Status.ACCEPTED).entity(succeed(null)).build()
    }

    @DELETE
    @APIResponse(
        responseCode = "200",
        description = "Topic Deleted",
    )
    fun delete(topic: String): Response = runBlocking {
        if (topicService.exists(topic)) {
            topicService.delete(topic)
        }
        Response.ok().build()
    }

    @HEAD
    @Path("{topic}")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Topic Exist",
        ),
        APIResponse(
            responseCode = "204",
            description = "Topic Non-Exist",
        ),
    )
    fun checkDuplicate(@PathParam("topic") topic: String): Response = runBlocking {
        (if (topicService.exists(topic)) Response.ok() else Response.noContent()).build()
    }
}