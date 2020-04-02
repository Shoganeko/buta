package dev.shog.buta.commands.commands.info

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.sendMessage
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.gateway.GatewayClient
import reactor.core.publisher.Mono

class PingCommand : Command(CommandConfig(
        "ping", 
        "View bot latency",
        Category.INFO,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> =
            e.sendMessage(container, "ping",
                    e.client.gatewayClientGroup.find(e.shardInfo.index).map(GatewayClient::getResponseTime)
            )
}