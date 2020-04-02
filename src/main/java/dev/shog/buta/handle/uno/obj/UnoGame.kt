package dev.shog.buta.handle.uno.obj

import dev.shog.buta.handle.StatisticsManager
import dev.shog.lib.util.defaultFormat
import discord4j.core.`object`.entity.User
import discord4j.rest.util.Snowflake
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * The main Uno game. This holds all data, including the played [Card]s etc.
 *
 * @param discordUser The user who created the game instance.
 */
class UnoGame(private val discordUser: User) {
    /**
     * The [discordUser]'s player class.
     */
    val user = Player(this)

    /**
     * Buta's player class.
     */
    val buta = Player(this)

    /**
     * If the [player] is [buta], return [user]. Vice versa.
     */
    fun getOtherUser(player: Player): Player {
        return if (player == buta)
            user
        else buta
    }

    /**
     * The amount of times a [Card] has been used.
     */
    private val usedCards = ConcurrentHashMap<Card, Int>()

    /**
     * The history of the game. This is mostly just what either user played.
     */
    private val history = ArrayList<HistoryEntry>()

    /**
     * The played [Card]s.
     */
    val playedCards = ArrayList<Card>()

    /**
     * Ends the game after a user runs out of cards.
     */
    fun endGame(userWon: Boolean) {
        games.remove(discordUser.id)

        when (userWon) {
            true -> StatisticsManager.increaseStatistic("uno-games-won-by-user")
            false -> StatisticsManager.increaseStatistic("uno-games-won-by-buta")
        }
    }

    /**
     * Add to [history]
     */
    fun addHistory(hist: String) {
        history.add(HistoryEntry(System.currentTimeMillis(), hist))
    }

    /**
     * Get [history].
     */
    fun getHistory(maxEntries: Int = 5, includeTime: Boolean = false): String {
        val range = if (maxEntries > history.size)
            0 until history.size
        else history.size - 5 until history.size

        return buildString {
            range
                    .asSequence()
                    .map { history[it] }
                    .forEach { hist ->
                        if (includeTime)
                            append(hist.time.defaultFormat() + " " + hist.history)
                        else append(hist.history)
                    }
        }
    }

    /**
     * Draws a [Card], making sure the proper amounts have been played.
     *
     * 108 of cards in total.
     */
    fun drawCard(): Card {
        if (playedCards.size == 108) { // All possible cards have been played, shuffle.
            val lastCard = playedCards.last()

            playedCards.apply {
                clear()
                add(lastCard)
            }
        }

        return when (Random.nextInt(15)) {
            // Wild Cards
            0 -> {
                // Lesser chance to get +4
                return if (Random.nextInt(2) == 0) {
                    val pre = Card(null, CardType.WILD_DRAW, null)
                    if (canHaveCard(pre)) {
                        usedCards[pre] = (usedCards[pre] ?: 0) + 1
                        Card(null, CardType.WILD_DRAW, null)
                    } else drawCard()
                } else {
                    val pre = Card(null, CardType.WILD_DEFAULT, null)
                    if (canHaveCard(pre)) {
                        usedCards[pre] = (usedCards[pre] ?: 0) + 1
                        Card(null, CardType.WILD_DEFAULT, null)
                    } else drawCard()
                }
            }

            // Skip Cards
            1 -> {
                val color = randomColor()
                val pre = Card(color, CardType.SKIP, null)
                if (canHaveCard(pre)) {
                    usedCards[pre] = (usedCards[pre] ?: 0) + 1
                    Card(color, CardType.SKIP, null)
                } else drawCard()
            }

            // Reverse Cards
            2 -> {
                val color = randomColor()
                val pre = Card(color, CardType.REVERSE, null)
                if (canHaveCard(pre)) {
                    usedCards[pre] = (usedCards[pre] ?: 0) + 1
                    Card(color, CardType.REVERSE, null)
                } else drawCard()
            }

            // Draw Two
            3 -> {
                val color = randomColor()
                val pre = Card(color, CardType.DRAW_TWO, null)
                if (canHaveCard(pre)) {
                    usedCards[pre] = (usedCards[pre] ?: 0) + 1
                    Card(color, CardType.DRAW_TWO, null)
                } else drawCard()
            }

            // Reverse
            4 -> {
                val color = randomColor()
                val pre = Card(color, CardType.REVERSE, null)
                if (canHaveCard(pre)) {
                    usedCards[pre] = (usedCards[pre] ?: 0) + 1
                    Card(color, CardType.REVERSE, null)
                } else drawCard()
            }

            // Default
            else -> {
                val num = Random.nextInt(10) // 0 -> 9

                val color = randomColor()
                val pre = Card(color, CardType.REGULAR, num)
                if (canHaveCard(pre)) {
                    usedCards[pre] = (usedCards[pre] ?: 0) + 1
                    Card(color, CardType.REGULAR, num)
                } else drawCard()
            }
        }
    }

    /**
     * Gets a random [CardColor].
     */
    private fun randomColor(): CardColor = CardColor.values().random()

    /**
     * Checks if the user can have a [Card].
     *
     * Makes sure there's a proper amount of cards within the game.
     */
    private fun canHaveCard(Card: Card): Boolean {
        return when (Card.type) {
            CardType.WILD_DEFAULT, CardType.SKIP, CardType.WILD_DRAW -> (usedCards[Card] ?: 0) < 4

            CardType.REVERSE, CardType.DRAW_TWO -> (usedCards[Card] ?: 0) < 2

            CardType.REGULAR -> {
                if (Card.num!! == 0) {
                    (usedCards[Card] ?: 0) == 0
                } else (usedCards[Card] ?: 0) < 2
            }
        }
    }

    /**
     * Checks if the user can play the current [c].
     *
     * If the [c] is wild, it can be played no matter what. If it's not, it must check if the
     */
    fun canPlayCard(c: Card): Boolean {
        val card = c.getAsClearWild()

        return when (card.type) {
            CardType.WILD_DEFAULT, CardType.WILD_DRAW -> true

            else -> {
                val otherCard = playedCards.last()

                // Checks if the color is the same, the number is the same if it is a number card, or if the type of a non-regular card is the same.
                (card.color == otherCard.color
                        || (card.num != null && otherCard.num != null && otherCard.num == card.num)
                        || (card.type != CardType.REGULAR && card.type == otherCard.type))
            }
        }
    }

    /**
     * Places down and returns the initial [Card] of the game. Cannot be anything but a [CardType.REGULAR]
     */
    private fun getInitialCard(): Card {
        if (playedCards.size != 0) throw IllegalArgumentException("Game already started!")

        val card = drawCard()

        return when (card.type) {
            CardType.REGULAR -> {
                playedCards.add(card)
                card
            }

            else -> getInitialCard()
        }
    }

    /**
     * Gives the Buta and the player 7 cards, and returns [getInitialCard].
     */
    fun initGame(): Card {
        user.draw(7)
        buta.draw(7)

        return getInitialCard()
    }

    /**
     * What time the game started at.
     */
    val startedAt = System.currentTimeMillis()

    /**
     * If the user called Uno, meaning that they had 1 card left.
     */
    var userCalledUno = false

    companion object {
        /**
         * Store of [UnoGame]s
         */
        val games = ConcurrentHashMap<Snowflake, UnoGame>()

        /**
         * Gets or creates an [UnoGame] from [games].
         *
         * Returns if it has just started in a [Pair].
         */
        fun getGame(user: User): Pair<Boolean, UnoGame> {
            if (!games.containsKey(user.id)) {
                games[user.id] = UnoGame(user)
                return Pair(true, games[user.id]!!)
            }

            return Pair(false, games[user.id]!!)
        }
    }
}