package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.commands.obj.Category
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.CommandConfig
import dev.shog.buta.commands.permission.PermissionFactory

import dev.shog.buta.handle.reddit.PostType
import dev.shog.buta.handle.reddit.RedditHandler
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class RedditCommand : Command(CommandConfig(
        "reddit",
        "Browse reddit.",
        Category.FUN,
        PermissionFactory.hasPermission()
)) {
    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<*> {
        if (args.isEmpty()) {
            return e.sendMessage("error.invalid-arguments")
        } else {
            val post = RedditHandler.getPost(args[0], PostType.HOT)

            if (post == null)
                return e.sendMessage(container, "invalid-subreddit").then()
            else {
                val nsfw = e.message.channel
                        .ofType(TextChannel::class.java)
                        .map { ch -> ch.isNsfw }
                        .map { nsfw -> !nsfw && post.data.getBoolean("over_18") }

                return nsfw.flatMap { invalid ->
                    if (invalid) {
                        e.sendMessage(container, "no-nsfw")
                    } else {
                        e.message.channel
                                .flatMap { ch ->
                                    ch.createEmbed { spec ->
                                        container.getEmbed("reddit-post").applyEmbed(
                                                spec, e.message.author.get(),
                                                hashMapOf(
                                                        "url" to post.data.getString("permalink").ar(),
                                                        "title" to post.data.getString("title").ar(),
                                                        "desc" to arrayListOf(
                                                                post.data.getLong("ups").toString(),
                                                                post.data.getLong("num_comments").toString()
                                                        ),
                                                        "image" to post.data.getString("url").ar()
                                                ), hashMapOf()
                                        )
                                    }
                                }
                    }
                }
            }
        }
    }
}