package dev.shog.buta.commands.commands

import dev.shog.buta.EN_US
import dev.shog.buta.commands.api.factory.GuildFactory
import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.ICommand
import dev.shog.buta.commands.obj.LangFillableContent
import dev.shog.buta.commands.permission.PermissionFactory
import dev.shog.buta.handle.LangLoader
import dev.shog.buta.handle.uno.handle.ButaAi
import dev.shog.buta.handle.uno.obj.Card
import dev.shog.buta.handle.uno.obj.CardColor
import dev.shog.buta.handle.uno.obj.CardType
import dev.shog.buta.handle.uno.obj.UnoGame
import dev.shog.buta.util.*
import dev.shog.lib.util.defaultFormat
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

internal val lfc = LangFillableContent.getFromCommandName("uno")

/**
 * Play Uno
 */
object Uno : ICommand(lfc, true, Categories.FUN, PermissionFactory.hasPermission()) {
    private val dataPack by lazy {
        val resp = EN_US.getJSONObject("uno").getJSONObject("response")
        val error = resp.getJSONObject("error")
        val success = resp.getJSONObject("success")
        val embeds = resp.getJSONObject("embeds")
        val other = resp.getJSONObject("other")

        return@lazy LangLoader.FullMessageDataPack(error, success, embeds, other)
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

                    uno.addHistory(dataPack.success.getString("user-play-card").form(card))

                    when {
                        userWon ->
                            uno.addHistory(dataPack.success.getString("user-won-game"))

                        playedCard.shouldSkipTurn ->
                            uno.addHistory(dataPack.success.getString("user-skip-buta-turn"))

                        else -> ButaAi(uno).play().also { aiCard ->
                            val butaPlayed = uno.buta.play(aiCard)

                            uno.addHistory(dataPack.success.getString("buta-play-card").form(aiCard))

                            var cont = butaPlayed.shouldSkipTurn
                            while (cont) {
                                val aiPlay = ButaAi(uno).play()
                                val unoAiPlay = uno.buta.play(aiPlay)

                                uno.addHistory(dataPack.success.getString("buta-play-card-whileSkipped").form(aiPlay))

                                cont = unoAiPlay.shouldSkipTurn
                            }
                        }
                    }

                    var desc = uno.getHistory().removePrefix("\n")

                    if (uno.buta.cards.getSize() == 0) {
                        desc += dataPack.success.getString("buta-won-game")
                        uno.endGame(false)
                    }

                    dataPack.embeds.getJSONObject("play-cards").applyEmbed(embed, user,
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

    override fun invoke(e: MessageCreateEvent, args: MutableList<String>): Mono<Void> {
        if (!e.message.author.isPresent)
            return e.sendMessage(EN_US.getEntry("error.invalid_arguments")).then()

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

                                    dataPack.embeds.getJSONObject("draw-card").applyEmbed(embed, e.message.author.get(),
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
                        return e.sendMessage(EN_US.getEntry("error.invalid_arguments")).then()

                    val game = UnoGame.getGame(author)
                    val uno = game.second

                    if (game.first)
                        return e.sendMessage(dataPack.error.getString("not-seen-cards"))
                                .doOnNext { UnoGame.games.remove(author.id) }
                                .then()

                    val number = args[1].toIntOrNull()?.minus(1)
                            ?: return e.sendMessage(EN_US.getEntry("error.invalid_arguments")).then()

                    if (uno.user.cards.getSize() == 1 && !uno.userCalledUno) {
                        val drawn = uno.user.draw(2)

                        return e.sendMessage(dataPack.error.getString("didnt-call-uno").form(drawn[0], drawn[1]))
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
                .zipWith(GuildFactory.getObject(e.guildId.get().asLong()))
                .flatMap { zip ->
                    val ch = zip.t1
                    val g = zip.t2

                    ch.createEmbed { embed ->
                        if (game.first) {
                            val init = uno.initGame()

                            dataPack.embeds.getJSONObject("init-game").applyEmbed(embed, e.message.author.get(),
                                    hashMapOf("desc" to g.prefix.ar()),
                                    hashMapOf(
                                            "first-played-card" to FieldReplacement(null, init.ar()),
                                            "user-cards" to FieldReplacement(null, getUserCards(uno).ar())
                                    )
                            )
                        } else {
                            dataPack.embeds.getJSONObject("game-info").applyEmbed(embed, e.message.author.get(),
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
     * Build a user's cards.
     */
    private fun getUserCards(uno: UnoGame): String =
            buildString {
                uno.user.cards.cards.forEachIndexed { i, c ->
                    append(dataPack.other.getString("cards").form(i + 1, c))
                }
            }
}
