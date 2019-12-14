package dev.shog.buta.commands.commands

import dev.shog.buta.EN_US
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.commands.obj.LangFillableContent
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.LangLoader
import dev.shog.buta.handle.obj.getField
import dev.shog.buta.handle.uno.handle.ButaAi
import dev.shog.buta.handle.uno.obj.UnoGame
import dev.shog.buta.util.format
import dev.shog.buta.util.formatText
import dev.shog.buta.util.sendMessage
import dev.shog.buta.util.update
import dev.shog.buta.handle.uno.obj.Card
import dev.shog.buta.handle.uno.obj.CardColor
import dev.shog.buta.handle.uno.obj.CardType
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * Play Uno
 */
class Uno(lfc: LangFillableContent = LangFillableContent.getFromCommandName("uno")) : Command(lfc, true, Categories.FUN, PermissionFactory.hasPermission()) {
    companion object {
        private val dataPack by lazy {
            val resp = EN_US.get().getJSONObject("uno").getJSONObject("response")
            val error = resp.getJSONObject("error")
            val success = resp.getJSONObject("success")
            val fields = resp.getJSONObject("fields")
            val other = resp.getJSONObject("other")

            return@lazy LangLoader.FullMessageDataPack(error, success, fields, other)
        }

        /**
         * If Buta is waiting for a user to fulfill their Wild request
         */
        val wildWaiting = ConcurrentHashMap<Snowflake, PendingWildCardColorSelect>()

        /**
         * If the user has selected the color for their wild card, or if they're just playing a card.
         */
        private fun playCard(user: User, channel: MessageChannel, uno: UnoGame, card: Card): Mono<Void> {
            val playedCard = uno.user.play(card)
            if (!playedCard.successful)
                return channel
                        .createMessage(dataPack.error.getString("cant-play-card"))
                        .then()

            val userWon = uno.user.cards.getSize() == 0

            return channel
                    .createEmbed { embed ->
                        embed.update(user)

                        uno.addHistory(formatText(dataPack.success.getString("user-play-card"), card))

                        when {
                            userWon ->
                                uno.addHistory(dataPack.success.getString("user-won-game"))

                            playedCard.shouldSkipTurn ->
                                uno.addHistory(dataPack.success.getString("user-skip-buta-turn"))

                            else -> ButaAi(uno).play().also { aiCard ->
                                val butaPlayed = uno.buta.play(aiCard)

                                uno.addHistory(formatText(dataPack.success.getString("buta-play-card"), aiCard))

                                var cont = butaPlayed.shouldSkipTurn
                                while (cont) {
                                    val aiPlay = ButaAi(uno).play()
                                    val unoAiPlay = uno.buta.play(aiPlay)

                                    uno.addHistory(formatText(dataPack.success.getString("buta-play-card-whileSkipped"), aiPlay))

                                    cont = unoAiPlay.shouldSkipTurn
                                }
                            }
                        }

                        var desc = uno.getHistory().removePrefix("\n")

                        val butaCards = dataPack.fields.getJSONObject("buta-cards-field").getField()
                        val lastPlayedCard = dataPack.fields.getJSONObject("last-played-card").getField()
                        val userCards = dataPack.fields.getJSONObject("user-cards").getField()

                        val built = buildString {
                            uno.user.cards.cards.forEachIndexed { i, c ->
                                append(formatText(dataPack.other.getString("cards"), i + 1, c))
                            }
                        }

                        embed.addField(butaCards.title, formatText(butaCards.desc, uno.buta.cards.getSize()), true)
                        embed.addField(lastPlayedCard.title, formatText(lastPlayedCard.desc, uno.playedCards.last()), true)
                        embed.addField(userCards.title, formatText(userCards.desc, built), false)

                        if (uno.buta.cards.getSize() == 0) {
                            desc += dataPack.success.getString("buta-won-game")
                            uno.endGame(false)
                        }

                        embed.setDescription(desc)
                    }
                    .then()
        }

        val BLUE: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD8")
        val RED: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD5")
        val YELLOW: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD9")
        val GREEN: ReactionEmoji = ReactionEmoji.unicode("\uD83D\uDCD7")

        /**
         * Proper reaction emojis.
         */
        val properColors = arrayListOf(BLUE, RED, YELLOW, GREEN)

        /**
         * Complete a wild card request with the inputted color.
         */
        fun completedWildCard(ev: ReactionAddEvent): Mono<Void> {
            val selectedColor = when (ev.emoji) {
                BLUE -> CardColor.BLUE
                RED -> CardColor.RED
                GREEN -> CardColor.GREEN
                YELLOW -> CardColor.YELLOW

                else -> return ev.channel
                        .flatMap { ch -> ch.createMessage("Invalid emoji, somehow.") }
                        .then()
            }

            val pending = wildWaiting[ev.userId]
                    ?: return ev.channel
                            .flatMap { ch -> ch.createMessage("Can't find pending request!") }
                            .then()

            wildWaiting.remove(ev.userId)

            return pending.message
                    .delete()
                    .then(ev.user)
                    .zipWith(ev.channel)
                    .flatMap { userChannel -> playCard(userChannel.t1, userChannel.t2, pending.unoGame, Card(selectedColor, pending.wildCardType, null)) }
        }

        data class PendingWildCardColorSelect(val wildCardType: CardType, val unoGame: UnoGame, val time: Long, val message: Message)
    }

    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> {
        if (!e.message.author.isPresent)
            return e.sendMessage(EN_US.get().getJSONObject("errors").getString("internal_error")).then()

        val author = e.message.author.get()

        if (args.size >= 1) {
            when (args[0].toLowerCase()) {
                // When a user tries to end the game
                "end" -> {
                    if (!UnoGame.games.containsKey(author.id))
                        return e.sendMessage(dataPack.error.getString("not-created-game")).then()

                    return e.sendMessage(dataPack.success.getString("manual-game-end"))
                            .doOnNext { UnoGame.games.remove(author.id) }
                            .then()
                }

                "call" -> {
                    if (!UnoGame.games.containsKey(author.id))
                        return e.sendMessage(dataPack.error.getString("not-created-game")).then()

                    val game = UnoGame.getGame(author)
                    val uno = game.second

                    // Makes sure they've got 1 card left, then call Uno
                    return if (uno.user.cards.getSize() == 1)
                        e.sendMessage(dataPack.success.getString("calledUno"))
                                .doOnNext { uno.userCalledUno = true }
                                .then()
                    else e.sendMessage(dataPack.error.getString("cannot-call-uno"))
                            .then()
                }

                "draw" -> {
                    val game = UnoGame.getGame(author)
                    val uno = game.second

                    if (game.first)
                        return e.sendMessage(dataPack.error.getString("not-seen-cards"))
                                .doOnNext { UnoGame.games.remove(author.id) }
                                .then()


                    return e.message.channel
                            .flatMap { ch ->
                                ch.createEmbed { embed ->
                                    val drawn = uno.user.draw(1)

                                    val lastPlayedCard = dataPack.fields.getJSONObject("last-played-card").getField()
                                    val drawnCard = dataPack.fields.getJSONObject("drawn-card").getField()
                                    val butaCards = dataPack.fields.getJSONObject("buta-cards-field").getField()
                                    val userCards = dataPack.fields.getJSONObject("user-cards").getField()

                                    embed.addField(butaCards.title, formatText(butaCards.desc, uno.buta.cards.getSize()), true)
                                    embed.addField(lastPlayedCard.title, formatText(lastPlayedCard.desc, uno.playedCards.last()), true)
                                    embed.addField(drawnCard.title, formatText(drawnCard.desc, drawn[0]), true)

                                    val built = buildString {
                                        uno.user.cards.cards.forEachIndexed { i, c ->
                                            append(formatText(dataPack.other.getString("cards"), i + 1, c))
                                        }
                                    }

                                    embed.addField(userCards.title, formatText(userCards.desc, built), false)
                                }
                            }
                            .then()
                }

                "play" -> {
                    if (args.size < 2)
                        return e.sendMessage(EN_US.get().getJSONObject("error").getString("invalid_arguments")).then()

                    val game = UnoGame.getGame(author)
                    val uno = game.second

                    if (game.first)
                        return e.sendMessage(dataPack.error.getString("not-seen-cards"))
                                .doOnNext { UnoGame.games.remove(author.id) }
                                .then()

                    val number = args[1].toIntOrNull()?.minus(1)
                            ?: return e.sendMessage(EN_US.get().getJSONObject("error").getString("invalid_arguments")).then()

                    if (uno.user.cards.getSize() == 1 && !uno.userCalledUno) {
                        val drawn = uno.user.draw(2)

                        return e.sendMessage(formatText(dataPack.error.getString("didnt-call-uno"), drawn[0], drawn[1]))
                                .then()
                    }

                    val card = try {
                        uno.user.cards.cards[number]
                    } catch (ex: Exception) {
                        return e.sendMessage(dataPack.error.getString("card-not-exist")).then()
                    }

                    return if (card.type == CardType.WILD_DEFAULT || card.type == CardType.WILD_DRAW) {
                        e.sendMessage(dataPack.success.getString("select-wild-card-color"))
                                .doOnNext { msg ->
                                    wildWaiting[e.message.author.get().id] =
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
                    } else e.message.channel
                            .flatMap { ch -> playCard(e.message.author.get(), ch, game.second, card) }
                }
            }
        }

        val game = UnoGame.getGame(e.message.author.get())
        val uno = game.second

        return e.message.channel
                .flatMap { ch ->
                    ch.createEmbed { embed ->
                        if (game.first) {
                            // TODO add proper prefix
                            embed.setDescription(formatText(dataPack.success.getString("created-game-first"), "b!"))

                            val init = uno.initGame()
                            val firstPlayedCard = dataPack.fields.getJSONObject("first-played-card").getField()
                            val userCards = dataPack.fields.getJSONObject("user-cards").getField()

                            embed.addField(firstPlayedCard.title, formatText(firstPlayedCard.desc, init), true)

                            val built = buildString {
                                uno.user.cards.cards.forEachIndexed { i, c ->
                                    append(formatText(dataPack.other.getString("cards"), i + 1, c))
                                }
                            }

                            embed.addField(userCards.title, formatText(userCards.desc, built), false)
                        } else {
                            embed.setDescription(formatText(dataPack.success.getString("game-start-time"), uno.startedAt.format()))

                            val butaCards = dataPack.fields.getJSONObject("buta-cards-field").getField()
                            val lastPlayedCard = dataPack.fields.getJSONObject("last-played-card").getField()
                            val userCards = dataPack.fields.getJSONObject("user-cards").getField()

                            embed.addField(butaCards.title, formatText(butaCards.desc, uno.buta.cards.getSize()), true)
                            embed.addField(lastPlayedCard.title, formatText(lastPlayedCard.desc, uno.playedCards.last()), true)

                            val built = buildString {
                                uno.user.cards.cards.forEachIndexed { i, c ->
                                    append(formatText(dataPack.other.getString("cards"), i + 1, c))
                                }
                            }

                            embed.addField(userCards.title, formatText(userCards.desc, built), false)
                        }
                    }
                }
                .then()
    }
}
