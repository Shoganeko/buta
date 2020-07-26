package dev.shog.buta.handle

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import dev.shog.buta.APP
import java.lang.Exception

object Mongo {
    private var client: MongoClient? = null

    private fun makeClient() {
        val password = APP.getConfigObject<ButaConfig>().mongoPass

        client = MongoClients.create("mongodb+srv://mojor:${password}@shogdev.uytz5.mongodb.net/users?retryWrites=true&w=majority")
    }

    fun getClient(): MongoClient {
        if (client == null)
            makeClient()

        return client ?: throw Exception("Failed to load Mongo Client")
    }
}