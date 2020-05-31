package dev.shog.buta.commands.commands.`fun`

import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.*
import dev.shog.buta.api.obj.msg.MessageHandler
import dev.shog.buta.api.permission.PermissionFactory
import dev.shog.buta.handle.uno.handle.ButaAi
import dev.shog.buta.handle.uno.obj.Card
import dev.shog.buta.handle.uno.obj.CardColor
import dev.shog.buta.handle.uno.obj.CardType
import dev.shog.buta.handle.uno.obj.UnoGame
import dev.shog.buta.util.*
import dev.shog.lib.util.defaultFormat
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Play Uno
 */
val UNO_COMMAND = Command(CommandConfig("uno", PermissionFactory.hasPermission())) {
    if (!event.message.author.isPresent)
        return@Command sendGlobalMessage("error.invalid-arguments")

    val author = event.message.author.get()

    if (args.size >= 1) {
        when (args[0].toLowerCase()) {
            // When a user tries to end the game
            "end" -> {
                if (!UnoGame.games.containsKey(author.id))
                    return@Command sendMessage("not-created-game")

                return@Command sendMessage("success.manual-game-end")
                        .doOnNext { UnoGame.games.remove(author.id) }
                        .then()
            }

            "call" -> {
                if (!UnoGame.games.containsKey(author.id))
                    return@Command sendMessage("error.not-created-game")

                val game = UnoGame.getGame(author)
                val uno = game.second

                // Makes sure they've got 1 card left, then call Uno
                return@Command if (uno.user.cards.getSize() == 1)
                    sendMessage("success.called-uno").doOnNext { uno.userCalledUno = true }
                else sendMessage("error.cannot-call-uno")
            }

            "draw" -> {
                val game = UnoGame.getGame(author)
                val uno = game.second

                if (game.first)
                    return@Command sendMessage("not-seen-cards")
                            .doOnNext { UnoGame.games.remove(author.id) }
                            .then()

                return@Command event.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { embed ->
                                val drawn = uno.user.draw(1)

                                container.getEmbed("draw-card").applyEmbed(embed, event.message.author.get(),
                                        fields = hashMapOf(
                                                "buta-cards" to FieldReplacement(null, uno.buta.cards.getSize().toString().ar()),
                                                "last-played-card" to FieldReplacement(null, uno.playedCards.last().ar()),
                                                "drawn-card" to FieldReplacement(null, drawn[0].ar()),
                                                "user-cards" to FieldReplacement(null, getUserCards(uno).ar())
                                        )
                                )
                            }
                        }
                        .then()
            }

            "play" -> {
                if (args.size < 2)
                    return@Command sendGlobalMessage("error.invalid-arguments")

                val game = UnoGame.getGame(author)
                val uno = game.second

                if (game.first)
                    return@Command sendMessage("error.not-seen-cards")
                            .doOnNext { UnoGame.games.remove(author.id) }

                val number = args[1].toIntOrNull()?.minus(1)
                        ?: return@Command sendGlobalMessage("error.invalid-arguments")

                if (uno.user.cards.getSize() == 1 && !uno.userCalledUno) {
                    val drawn = uno.user.draw(2)

                    return@Command sendMessage("error.didnt-call-uno", drawn[0], drawn[1])
                }

                val card = try {
                    uno.user.cards.cards[number]
                } catch (ex: Exception) {
                    return@Command sendMessage("error.card-not-exist")
                }

                return@Command if (card.type == CardType.WILD_DEFAULT || card.type == CardType.WILD_DRAW) {
                    sendMessage("success.select-wild-card-color")
                            .doOnNext { msg ->
                                wildWaiting[event.message.author.get().id] =
                                        PendingWildCardColorSelect(card.type, game.second, System.currentTimeMillis(), msg)
                            }
                            .flatMap { msg ->
                                msg.addReaction(BLUE)
                                        .then(msg.addReaction(RED))
                                        .then(msg.addReaction(YELLOW))
                                        .then(msg.addReaction(GREEN))
                                        .map { msg }
                            }
                            .then()
                } else event.message.channel
                        .flatMap { ch -> playCard(event.message.author.get(), ch, game.second, card) }
            }
        }
    }

    val game = UnoGame.getGame(event.message.author.get())
    val uno = game.second

    return@Command event.message.channel
            .flatMap { ch ->
                val g = GuildFactory.getOrCreate(event.guildId.get().asLong())

                ch.createEmbed { embed ->
                    if (game.first) {
                        val init = uno.initGame()

                        container.getEmbed("init-game").applyEmbed(embed, event.message.author.get(),
                                hashMapOf("desc" to g.prefix.ar()),
                                hashMapOf(
                                        "first-played-card" to FieldReplacement(null, init.ar()),
                                        "user-cards" to FieldReplacement(null, getUserCards(uno).ar())
                                )
                        )
                    } else {
                        container.getEmbed("game-info").applyEmbed(embed, event.message.author.get(),
                                hashMapOf("desc" to uno.startedAt.defaultFormat().ar()),
                                hashMapOf(
                                        "buta-cards" to FieldReplacement(null, uno.buta.cards.getSize().toString().ar()),
                                        "last-played-card" to FieldReplacement(null, uno.playedCards.last().ar()),
                                        "user-cards" to FieldReplacement(null, getUserCards(uno).ar())
                                )
                        )
                    }
                }
            }
            .then()
}

/**
 * If Buta is waiting for a user to fulfill their Wild request
 */
val wildWaiting = ConcurrentHashMap<Snowflake, PendingWildCardColorSelect>()

/**
 * If the user has selected the color for their wild card, or if they're just playing a card.
 */
private fun playCard(user: User, channel: MessageChannel, uno: UnoGame, card: Card): Mono<*> {
    val playedCard = uno.user.play(card)
    if (!playedCard.successful)
        return channel.createMessage(UNO_COMMAND.container.getMessage("error.cant-play-card"))

    val userWon = uno.user.cards.getSize() == 0

    return channel
            .createEmbed { embed ->
                embed.update(user)

                uno.addHistory(UNO_COMMAND.container.getMessage("success.user-play-card", card))

                when {
                    userWon ->
                        uno.addHistory(UNO_COMMAND.container.getMessage("success.user-won-game", card))

                    playedCard.shouldSkipTurn ->
                        uno.addHistory(UNO_COMMAND.container.getMessage("success.user-skip-buta-turn"))

                    else -> ButaAi(uno).play().also { aiCard ->
                        val butaPlayed = uno.buta.play(aiCard)

                        uno.addHistory(UNO_COMMAND.container.getMessage("success.buta-play-card", aiCard))

                        var cont = butaPlayed.shouldSkipTurn
                        while (cont) {
                            val aiPlay = ButaAi(uno).play()
                            val unoAiPlay = uno.buta.play(aiPlay)

                            uno.addHistory(UNO_COMMAND.container.getMessage("success.buta-play-card-whileSkipped", aiPlay))

                            cont = unoAiPlay.shouldSkipTurn
                        }
                    }
                }

                var desc = uno.getHistory().removePrefix("\n")

                if (uno.buta.cards.getSize() == 0) {
                    desc += UNO_COMMAND.container.getMessage("success.buta-won-game")
                    uno.endGame(false)
                }

                UNO_COMMAND.container.getEmbed("play-cards").applyEmbed(embed, user,
                        hashMapOf("desc" to desc.ar()),
                        hashMapOf(
                                "buta-cards" to FieldReplacement(null, uno.buta.cards.getSize().toString().ar()),
                                "last-played-card" to FieldReplacement(null, uno.playedCards.last().ar()),
                                "user-cards" to FieldReplacement(null, getUserCards(uno).ar())
                        )
                )
            }
            .then()
}

private val BLUE: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD8")
private val RED: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD5")
private val YELLOW: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD9")
private val GREEN: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD7")

/**
 * Proper reaction emojis.
 */
val properColors = arrayListOf(BLUE, RED, YELLOW, GREEN)

/**
 * Complete a wild card request with the inputted color.
 */
fun completedWildCard(ev: ReactionAddEvent): Mono<*> {
    val selectedColor = when (ev.emoji) {
        BLUE -> CardColor.BLUE
        RED -> CardColor.RED
        GREEN -> CardColor.GREEN
        YELLOW -> CardColor.YELLOW

        else -> return ev.channel.flatMap { ch -> ch.createMessage("Invalid emoji, somehow.") }
    }

    val pending = wildWaiting[ev.userId]
            ?: return ev.channel.flatMap { ch -> ch.createMessage("Can't find pending request!") }

    wildWaiting.remove(ev.userId)

    return pending.message
            .delete()
            .then(ev.user)
            .zipWith(ev.channel)
            .flatMap { userChannel ->
                playCard(userChannel.t1, userChannel.t2, pending.unoGame, Card(selectedColor, pending.wildCardType, null))
            }
}

data class PendingWildCardColorSelect(val wildCardType: CardType, val unoGame: UnoGame, val time: Long, val message: Message)

/**
 * Build a user's cards.
 */
private fun getUserCards(uno: UnoGame): String {
    val result = buildString {
        uno.user.cards.cards.forEachIndexed { i, c ->
            append(UNO_COMMAND.container.getMessage("other.cards", i + 1, c))
        }
    }

    return if (result.isBlank())
        "none"
    else result
}
