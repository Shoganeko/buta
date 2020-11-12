package dev.shog.buta.api

//import com.gitlab.kordlib.common.entity.Snowflake
//import dev.shog.buta.APP
//import dev.shog.buta.CLIENT
//import dev.shog.buta.api.factory.GuildFactory
//import dev.shog.buta.handle.ButaConfig
//import io.ktor.application.*
//import io.ktor.features.*
//import io.ktor.http.*
//import io.ktor.jackson.*
//import io.ktor.locations.*
//import io.ktor.request.*
//import io.ktor.response.*
//import io.ktor.routing.*
//import io.ktor.serialization.*
//import io.ktor.server.engine.*
//import io.ktor.server.netty.*
//import kotlinx.coroutines.flow.toList
//import kotlinx.serialization.json.Json
//import java.text.DateFormat
//
//val MOJOR_SERVER = embeddedServer(Netty, 8014) {
//    install(ContentNegotiation) {
//        jackson {
//            dateFormat = DateFormat.getDateInstance()
//        }
//
//        register(ContentType.Application.Json, JacksonConverter())
//
////        serialization(
////                contentType = ContentType.Application.Json,
////                json = Json { }
////        )
//    }
//
//    // TODO improve auth system
//    val creds = APP.getConfigObject<ButaConfig>().sqlCredentials
//    val properUsername = creds?.first!!
//    val properPassword = creds?.second!!
//
//    install(Locations)
//
//    install(DefaultHeaders) {
//        header("Server", "Buta/v${APP.version}")
//    }
//
//    routing {
//        post("/roles") {
//            val params = call.receiveParameters()
//
//            val username = params["username"]
//            val password = params["password"]
//
//            val id = params["id"]?.toLongOrNull()
//
//            when {
//                username == null || password == null || id == null ->
//                    call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Arguments"))
//
//                username != properUsername || password != properPassword ->
//                    call.respond(HttpStatusCode.Unauthorized, hashMapOf("error" to "Invalid Authorization!"))
//
//                else -> {
//                    val roles = CLIENT!!.getGuild(Snowflake(id))
//
//                    if (roles == null)
//                        call.respond(HttpStatusCode.BadRequest)
//                    else {
//                        call.respond(
//                                HttpStatusCode.OK,
//                                roles.roles.toList()
//                                        .map { role ->
//                                            object {
//                                                val name = role.name
//                                                val color = role.color
//                                                val id = role.id.longValue.toString()
//                                                val mentionable = role.mentionable
//                                                val permissions = role.permissions
//                                            }
//                                        }
//                        )
//                    }
//                }
//            }
//        }
//
//        post("/refresh") {
//            val params = call.receiveParameters()
//
//            val username = params["username"]
//            val password = params["password"]
//
//            val id = params["id"]?.toLongOrNull()
//            val type = params["type"]
//
//            when {
//                username == null || password == null || id == null || type == null ->
//                    call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Arguments"))
//
//                username != properUsername || password != properPassword ->
//                    call.respond(HttpStatusCode.Unauthorized, hashMapOf("error" to "Invalid Authorization!"))
//
//                else -> {
//                    when (type.toLowerCase()) {
//                        "guild" -> {
//                            GuildFactory.cache.remove(id)
//
//                            call.respond(HttpStatusCode.OK, hashMapOf("response" to "OK :)"))
//                        }
//
//                        else ->
//                            call.respond(HttpStatusCode.BadRequest, hashMapOf("error" to "Invalid Type"))
//                    }
//                }
//            }
//        }
//    }
//}