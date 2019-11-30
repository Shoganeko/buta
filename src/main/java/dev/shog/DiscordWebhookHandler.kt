package dev.shog

import dev.shog.buta.FileHandler
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.system.exitProcess

/**
 * The Discord Webhook Handler
 */
object DiscordWebhookHandler {
    /**
     * The default user for the webhooks
     */
    internal object DefaultUser {
        const val username = "Buta"
        const val avatarUrl = "https://cdn.discordapp.com/attachments/521062156024938498/636701089424605185/IMG_20191023_180024.jpg"
    }

    /**
     * The webhook URL
     */
    private lateinit var URL: String

    /**
     * Initialize!
     */
    fun init() {
        val url = FileHandler.get("discord-webhook").toString()

        if (url == "null")
            exitProcess(-1)

        URL = url
    }

    /**
     * Send a message through the webhook.
     */
    fun sendMessage(message: String): Mono<Void> =
            getJsonObject()
                    .doOnNext { js -> js.put("content", message) }
                    .flatMap { js -> makeRequest(js) }
                    .map { req -> parseResponse(req) ?: ":)" }
                    .doOnNext { resp -> if (resp != ":)") throw Exception("Invalid response from Discord.") }
                    .then()

    /**
     * Build the JSON object.
     */
    private fun getJsonObject(): Mono<JSONObject> =
            JSONObject()
                    .toMono()
                    .doOnNext { js -> js.put("username", DefaultUser.username) }
                    .doOnNext { js -> js.put("avatar_url", DefaultUser.avatarUrl) }
                    .doOnNext { js -> js.put("tts", false) }

    /**
     * Parse the response given by Discord.
     *
     * If it's null, then the request is unsuccessful.
     */
    private fun parseResponse(resp: HttpResponse<String>): String? {
        return if (resp.status == 204)
            null
        else {
            return try {
                JSONObject(resp.body).toString()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Creates the request.
     */
    private fun makeRequest(jsonObject: JSONObject): Mono<HttpResponse<String>> =
            Unirest.post(URL)
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toString())
                    .asStringAsync()
                    .toMono()
}