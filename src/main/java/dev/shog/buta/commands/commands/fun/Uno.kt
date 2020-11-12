package dev.shog.buta.commands.commands.`fun`

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import dev.shog.buta.api.factory.GuildFactory
import dev.shog.buta.api.obj.*
import dev.shog.buta.handle.uno.handle.ButaAi
import dev.shog.buta.handle.uno.obj.Card
import dev.shog.buta.handle.uno.obj.CardColor
import dev.shog.buta.handle.uno.obj.CardType
import dev.shog.buta.handle.uno.obj.UnoGame
import dev.shog.buta.util.*
import dev.shog.lib.util.defaultFormat
import java.util.concurrent.ConcurrentHashMap

/**
 * Play Uno
 */
val UNO_COMMAND = Command(CommandConfig("uno", Category.FUN)) {
    val author = event.message.author ?: return@Command

    if (args.size >= 1) {
        when (args[0].toLowerCase()) {
            // When a user tries to end the game
            "end" -> {
                if (!UnoGame.games.containsKey(author.id)) {
                    sendMessage("You haven't created a game yet!")
                    return@Command
                }

                UnoGame.games.remove(author.id)
                sendMessage("Ended game!")

                return@Command
            }

            "call" -> {
                if (!UnoGame.games.containsKey(author.id)) {
                    sendMessage("You haven't created a game yet!")
                    return@Command
                }

                val game = UnoGame.getGame(author)
                val uno = game.second

                // Makes sure they've got 1 card left, then call Uno
                if (uno.user.cards.getSize() == 1) {
                    sendMessage("You have caled Uno!")

                    uno.userCalledUno = true
                } else sendMessage("You don't have 1 card left!")

                return@Command
            }

            "draw" -> {
                val game = UnoGame.getGame(author)
                val uno = game.second

                if (game.first) {
                    sendMessage("You haven't seen your cards yet, use `b!uno` first!")
                    UnoGame.games.remove(author.id)
                    return@Command
                }

                event.message.channel.createEmbed {
                    field {
                        name = "Buta"
                        value = uno.buta.cards.getSize().toString()
                        inline = true
                    }

                    field {
                        name = "Last Played Card"
                        value = uno.playedCards.last().toString()
                        inline = true
                    }

                    field {
                        val drawn = uno.user.draw(1)

                        name = "Drawn Card"
                        value = drawn[0].toString()
                        inline = true
                    }

                    field {
                        name = "Your Cards"
                        value = getUserCards(uno)
                        inline = false
                    }
                }

                return@Command
            }

            "play" -> {
                if (args.size < 2) {
                    sendMessage("Invalid arguments!")
                    return@Command
                }

                val game = UnoGame.getGame(author)
                val uno = game.second

                if (game.first) {
                    sendMessage("You haven't seen your cards yet, use `b!uno` first!")
                    return@Command
                }

                val number = args[1].toIntOrNull()?.minus(1)

                if (number == null) {
                    sendMessage("Invalid arguments!")
                    return@Command
                }

                if (uno.user.cards.getSize() == 1 && !uno.userCalledUno) {
                    val drawn = uno.user.draw(2)

                    sendMessage("You didn't call Uno! You have been given a ${drawn[0]} and a ${drawn[1]}.")
                    return@Command
                }

                val card = try {
                    uno.user.cards.cards[number]
                } catch (ex: Exception) {
                    sendMessage("That card doesn't exist!")
                    return@Command
                }

                if (card.type == CardType.WILD_DEFAULT || card.type == CardType.WILD_DRAW) {
                    val msg = sendMessage("What color for the wild card?")

                    wildWaiting[event.message.author!!.id] =
                            PendingWildCardColorSelect(card.type, game.second, System.currentTimeMillis(), msg)

                    properColors.forEach {
                        msg.addReaction(it)
                    }
                }

                playCard(event.message.getAuthorAsMember()!!, event.message.channel, game.second, card)

                return@Command
            }
        }
    }

    val game = UnoGame.getGame(event.message.author!!)
    val uno = game.second

    val guild = GuildFactory.getOrCreate(event.guildId?.longValue!!)

    event.message.channel.createEmbed {
        addFooter(event)

        if (game.first) {
            val init = uno.initGame()

            description = "You have started a game of Uno!\nSelect a playable card below, and play it with `{0}uno play **number**`.\nOnce you're about to play your last card, make sure to type `{0}uno call`."

            field {
                name = "First Played Card"
                value = init.toString()
                inline = true
            }

            field {
                name = "Your Cards"
                value = getUserCards(uno)
                inline = false
            }
        } else {
            description = "You started this game at `${uno.startedAt.defaultFormat()}`."

            field {
                name = "Buta"
                value = "${uno.buta.cards.getSize()} cards"
                inline = true
            }

            field {
                name = "Last Played Card"
                value = uno.playedCards.last().toString()
                inline = true
            }

            field {
                name = "Your Cards"
                value = getUserCards(uno)
                inline = false
            }
        }
    }
}

/**
 * If Buta is waiting for a user to fulfill their Wild request
 */
val wildWaiting = ConcurrentHashMap<Snowflake, PendingWildCardColorSelect>()

/**
 * If the user has selected the color for their wild card, or if they're just playing a card.
 */
private suspend fun playCard(user: Member, channel: MessageChannelBehavior, uno: UnoGame, card: Card) {
    val playedCard = uno.user.play(card)
    if (!playedCard.successful) {
        channel.createMessage("You can't play that card!")
        return
    }

    val userWon = uno.user.cards.getSize() == 0

    channel.createEmbed {
        addFooter(user)

        uno.addHistory("\n:white_small_square: You played a ${card}.")

        when {
            userWon ->
                uno.addHistory("\n:white_small_square: You have won! \uD83D\uDC51") // crown emoji

            playedCard.shouldSkipTurn ->
                uno.addHistory("\n:white_small_square: You skipped Buta's turn!")

            else -> ButaAi(uno).play().also { aiCard ->
                val butaPlayed = uno.buta.play(aiCard)

                uno.addHistory("\n:white_small_square: Buta played a ${aiCard}.")

                var cont = butaPlayed.shouldSkipTurn
                while (cont) {
                    val aiPlay = ButaAi(uno).play()
                    val unoAiPlay = uno.buta.play(aiPlay)

                    uno.addHistory("\n:white_small_square: Your turn was skipped, so Buta played a ${aiPlay}.")

                    cont = unoAiPlay.shouldSkipTurn
                }
            }
        }

        var desc = uno.getHistory().removePrefix("\n")

        if (uno.buta.cards.getSize() == 0) {
            desc += "\n:white_small_square: Buta has won the game!"
            uno.endGame(false)
        }

        channel.createEmbed {
            description = desc

            field {
                name = "Buta"
                value = uno.buta.cards.getSize().toString()
                inline = true
            }

            field {
                name = "Last Played Card"
                value = uno.playedCards.last().toString()
                inline = true
            }

            field {
                name = "Your Cards"
                value = getUserCards(uno)
            }
        }
    }
}

private val BLUE: ReactionEmoji = ReactionEmoji.Unicode("\uD83D\uDCD8")
private val RED: ReactionEmoji = ReactionEmoji.Unicode("\uD83D\uDCD5")
private val YELLOW: ReactionEmoji = ReactionEmoji.Unicode("\uD83D\uDCD9")
private val GREEN: ReactionEmoji = ReactionEmoji.Unicode("\uD83D\uDCD7")

/**
 * Proper reaction emojis.
 */
val properColors = arrayListOf(BLUE, RED, YELLOW, GREEN)

/**
 * Complete a wild card request with the inputted color.
 */
suspend fun completedWildCard(ev: ReactionAddEvent) {
    val selectedColor = when (ev.emoji) {
        BLUE -> CardColor.BLUE
        RED -> CardColor.RED
        GREEN -> CardColor.GREEN
        YELLOW -> CardColor.YELLOW

        else -> {
            ev.message.channel.createMessage("There was an internal issue.")
            return
        }
    }

    val pending = wildWaiting[ev.userId]

    if (pending == null) {
        ev.message.channel.createMessage("A pending request could not be found!")
        return
    }

    wildWaiting.remove(ev.userId)

    pending.message.delete()

    playCard(ev.getUserAsMember()!!, ev.message.channel, pending.unoGame, Card(selectedColor, pending.wildCardType, null))
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
