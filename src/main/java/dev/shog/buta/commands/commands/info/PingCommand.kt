package dev.shog.buta.commands.commands.info

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.sendMessage
import discord4j.gateway.GatewayClient

val PING_COMMAND = Command(CommandConfig("ping")) {
    event.sendMessage(container, "default",
            event.client.gatewayClientGroup.find(event.shardInfo.index).map(GatewayClient::getResponseTime).get().toMillis()
    )
}