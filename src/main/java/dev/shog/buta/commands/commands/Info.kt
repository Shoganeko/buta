package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.InfoCommand
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.util.update
import dev.shog.buta.util.yesNo
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat

/**
 * About Buta
 */
val PING = InfoCommand("Ping", "Pong!", hashMapOf(Pair("ping", "Get the ping of the bot.")), true, PermissionFactory.hasPermission(), arrayListOf()) {
    it.first.message.channel
            .flatMap { channel ->
                channel.createMessage("Pong! ${it.first.client.responseTime}ms")
            }.then()
}

/**
 * About Buta
 */
val ABOUT = InfoCommand("About", "About Buta!", hashMapOf(Pair("about", "About Chad!")), true, PermissionFactory.hasPermission(), arrayListOf()) {
    it.first.message.channel
            .flatMap { channel ->
                channel.createEmbed  { embed ->
                    embed.update(it.first.message.author.get())

                    embed.setTitle("ℹ About Buta")
                    embed.setDescription("Buta, formerly Chad, is a feature filled Discord bot with commands from moderation to gambling.")
                    embed.setUrl("https://github.com/shoganeko/buta")
                }
            }.then()
}

/**
 * Guild Info
 */
val GUILD_INFO = InfoCommand(
        "GuildInfo", "Information about a Guild!",
        hashMapOf(Pair("guildinfo", "Gets info about the guild!")),
        false,
        PermissionFactory.hasPermission(),
        arrayListOf()
) {
    val strBuilder = StringBuilder()
    it.first.guild.flatMap { guild ->
        Mono.just(guild.name)
                .doOnNext { name -> strBuilder.append("\n__Name__: $name") }
                .then(guild.owner)
                .doOnNext { o -> strBuilder.append("\n__Owner__: ${o.displayName}") }
                .thenMany(guild.roles)
                .count()
                .doOnNext { r -> strBuilder.append("\n__Role Amount__: ${r - 1}") }
                .doOnNext { strBuilder.append("\n__User Amount__: ${guild.memberCount.asInt}") }
                .thenMany(guild.channels)
                .count()
                .doOnNext { c -> strBuilder.append("\n__Channels__: $c") }
                .flatMap { _ -> it.first.message.channel }
                .flatMap { ch -> ch.createMessage(strBuilder.toString()) }
    }.then()
}

/**
 * Gets information on users.
 */
val USER_INFO = InfoCommand(
        "UserInfo", "Information about a User!!",
        hashMapOf(Pair("userinfo", "Gets info about yourself!"), Pair("userinfo [@user]", "Get information about someone else.")),
        false,
        PermissionFactory.hasPermission(),
        arrayListOf()
) { data ->
    data.first.guild.doOnNext { guild ->
        data.first.message.userMentions
                .collectList()
                .flatMap { users -> Mono.justOrEmpty(if (users.isNotEmpty()) users[0].id else data.first.member.get().id) }
                .flatMap { id -> guild.getMemberById(id) }
                .subscribe({ user ->
                    val strBuilder = StringBuilder()

                    Mono.just(user.username)
                            .doOnNext { name -> strBuilder.append("\n__Name__: $name#${user.discriminator}") }
                            .doOnNext { strBuilder.append("\n__Bot__: ${user.isBot.yesNo().capitalize()}") }
                            .thenMany(user.roles)
                            .count()
                            .doOnNext { r -> strBuilder.append("\n__Role Amount__: $r") }
                            .doOnNext { strBuilder.append("\n__Join Date__: ${SimpleDateFormat("MM/dd/yyyy").format(user.joinTime.toEpochMilli())}") }
                            .flatMap { data.first.message.channel }
                            .flatMap { ch -> ch.createMessage(strBuilder.toString()) }
                            .doOnError { er ->
                                er.printStackTrace()
                            }
                            .subscribe()
                }, {
                    data.first.message.channel
                            .flatMap { ch -> ch.createMessage("There was an issue getting the data!") }
                            .subscribe()
                })
    }.then()
}