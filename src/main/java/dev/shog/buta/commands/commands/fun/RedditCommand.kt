package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig

import dev.shog.buta.handle.reddit.PostType
import dev.shog.buta.handle.reddit.RedditHandler
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.sendGlobalMessage
import dev.shog.buta.util.sendMessage
import discord4j.core.`object`.entity.channel.TextChannel

val REDDIT_COMMAND = Command(CommandConfig("reddit")) {
    if (args.isEmpty()) {
        return@Command sendGlobalMessage("error.invalid-arguments")
    } else {
        val post = RedditHandler.getPost(args[0], PostType.HOT)

        if (post == null)
            return@Command event.sendMessage(container, "invalid-subreddit").then()
        else {
            val nsfw = event.message.channel
                    .ofType(TextChannel::class.java)
                    .map { ch -> ch.isNsfw }
                    .map { nsfw -> !nsfw && post.data.getBoolean("over_18") }

            return@Command nsfw.flatMap { invalid ->
                if (invalid) {
                    event.sendMessage(container, "no-nsfw")
                } else {
                    event.message.channel
                            .flatMap { ch ->
                                ch.createEmbed { spec ->
                                    container.getEmbed("reddit-post").applyEmbed(
                                            spec, event.message.author.get(),
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