package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.InfoCommand
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage

/**
 * About Buta
 */
val PING = InfoCommand("ping", true, PermissionFactory.hasPermission()) { it, resp ->
    it.first
            .sendMessage(formatText(resp.getString(resp.keySet().random()), it.first.client.responseTime))
            .then()
}.build().add()