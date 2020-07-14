package dev.shog.buta.api

import dev.shog.buta.APP
import dev.shog.buta.CLIENT
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.handle.ButaConfig
import discord4j.common.util.Snowflake
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.serialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.text.DateFormat
import kotlin.coroutines.CoroutineContext

val MOJOR_SERVER = embeddedServer(Netty, 8014) {
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
        }

        register(ContentType.Application.Json, JacksonConverter())

        serialization(
                contentType = ContentType.Application.Json,
                json = Json(DefaultJsonConfiguration)
        )
    }

    // TODO improve auth system
    val creds = APP.getConfigObject<ButaConfig>().sqlCredentials
    val properUsername = creds?.first!!
    val properPassword = creds?.second!!

    install(Locations)

    install(DefaultHeaders) {
        header("Server", "Buta/v${APP.version}")
    }

    routing {
        post("/roles") {
            val params = call.receiveParameters()

            val username = params["username"]
            val password = params["password"]

            val id = params["id"]?.toLongOrNull()

            when {
                username == null || password == null || id == null ->
                    call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Arguments"))

                username != properUsername || password != properPassword ->
                    call.respond(HttpStatusCode.Unauthorized, hashMapOf("error" to "Invalid Authorization!"))

                else -> {
                    val roles = CLIENT!!
                            .getGuildById(Snowflake.of(id))
                            .flatMapMany { guild -> guild.roles }
                            .filter { role -> !role.isEveryone }
                            .map { role ->
                                object {
                                    val name = role.name
                                    val color = role.color
                                    val id = role.id.asLong().toString()
                                    val mentionable = role.isMentionable
                                    val permissions = role.permissions
                                }
                            }
                            .collectList()
                            .toFuture()
                            .join()

                    call.respond(HttpStatusCode.OK, roles)
                }
            }
        }

        post("/refresh") {
            val params = call.receiveParameters()

            val username = params["username"]
            val password = params["password"]

            val id = params["id"]?.toLongOrNull()
            val type = params["type"]

            when {
                username == null || password == null || id == null || type == null ->
                    call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Arguments"))

                username != properUsername || password != properPassword ->
                    call.respond(HttpStatusCode.Unauthorized, hashMapOf("error" to "Invalid Authorization!"))

                else -> {
                    when (type.toLowerCase()) {
                        "guild" -> {
                            GuildFactory.cache.remove(id)

                            call.respond(HttpStatusCode.OK, hashMapOf("response" to "OK :)"))
                        }

                        else ->
                            call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Type"))
                    }
                }
            }
        }
    }
}