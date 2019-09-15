package dev.shog.buta.commands.api

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import dev.shog.buta.LOGGER
import dev.shog.buta.util.getType
import org.json.JSONObject
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * The Mojura/Buta API
 *
 * It's cached within the factories, they're individually cached, and cached at Mojura.
 */
object API {
    /**
     * The base URL for making request
     */
    private const val BASE_URL = "http://localhost:4070/v1/buta"

    /**
     * Gets an object.
     */
    fun getObject(type: Type, id: Long): Mono<HttpResponse<JsonNode>> =
            Mono.justOrEmpty(Unirest.get(BASE_URL)
                    .field("type", type.toString().toLowerCase())
                    .field("id", id.toString())
                    .asJson())
                    .flatMap { js -> if (js.code != 200) Mono.empty<HttpResponse<JsonNode>>() else Mono.just(js) }

    /**
     * Gets a [JSONObject]
     */
    fun getJsonObject(obj: Mono<HttpResponse<JsonNode>>): Mono<JSONObject> =
            obj.map { o -> JSONObject(o.body.`object`.get("response").toString()) }

    /**
     * Create an object.
     */
    fun createObject(type: Type, id: Long): Mono<HttpResponse<JsonNode>> =
            Mono.justOrEmpty(Unirest.put(BASE_URL)
                    .field("type", type.toString().toLowerCase())
                    .field("id", id.toString())
                    .asJson())
                    .flatMap { js -> if (js.code != 200) Mono.empty<HttpResponse<JsonNode>>() else Mono.just(js) }

    /**
     * Update an object
     */
    fun updateObject(type: Type, id: Long, pair: Pair<String, Any>): Mono<Boolean> =
            getType(pair.second)
                    .map { ty ->
                        Unirest.post(BASE_URL)
                                .field("type", type.toString().toLowerCase())
                                .field("id", id.toString())
                                .field("key", pair.first)
                                .field("value", pair.second.toString())
                                .field("valueType", ty)
                                .asJson()
                    }
                    .doOnNext { r -> println(r.body.`object`)}
                    .map { js -> js.code == 200 }

    /**
     * Deletes an object.
     */
    fun deleteObject(type: Type, id: Long): Mono<Boolean> = Mono.justOrEmpty(Unirest.delete(BASE_URL)
            .field("type", type.toString().toLowerCase())
            .field("id", id.toString())
            .asJson())
            .map { js -> js.code == 200 }

    /**
     * The type of object to manipulate.
     */
    enum class Type {
        GUILD, USER
    }
}