package dev.shog.buta.handle.uno.handle

import dev.shog.buta.handle.uno.obj.Card
import dev.shog.buta.handle.uno.obj.CardColor
import dev.shog.buta.handle.uno.obj.CardType
import dev.shog.buta.handle.uno.obj.UnoGame

/**
 * Creates plays in a [UnoGame] for the bot.
 *
 * This does not actually play the card into the game, but finds the [Card] that the bot should play.
 */
class ButaAi(private val unoGame: UnoGame) {
    /**
     * The last played card
     */
    private lateinit var recentCard: Card

    /**
     * All cards
     */
    private lateinit var cards: ArrayList<Card>

    /**
     * Default wild cards.
     */
    private val wild = ArrayList<Card>()

    /**
     * +4 wild cards.
     */
    private val wildDraw = ArrayList<Card>()

    /**
     * The +2 cards.
     */
    private val plusTwo = ArrayList<Card>()

    /**
     * The skip cards
     */
    private val reverse = ArrayList<Card>()

    /**
     * The skip cards.
     */
    private val skip = ArrayList<Card>()

    /**
     * The regular numeric cards.
     */
    private val reg = ArrayList<Card>()

    /**
     * Calculates the different variables in [ButaAi].
     */
    private fun calculate() {
        wild.clear()
        wildDraw.clear()
        plusTwo.clear()
        reverse.clear()
        skip.clear()
        reg.clear()
        cards = unoGame.buta.cards.cards
        recentCard = unoGame.playedCards.last()

        cards.forEach { card ->
            when (card.type) {
                CardType.WILD_DEFAULT -> wild.add(card.getAsClearWild())
                CardType.WILD_DRAW -> wildDraw.add(card.getAsClearWild())
                CardType.SKIP -> skip.add(card)
                CardType.REVERSE -> reverse.add(card)
                CardType.DRAW_TWO -> plusTwo.add(card)
                CardType.REGULAR -> reg.add(card)
            }
        }
    }

    init {
        calculate()
    }

    /**
     * Gets a [Card] that's able to be played.
     */
    fun play(): Card {
        // If they've got less cards, attempt to get a +2 card.
        if (unoGame.user.cards.getSize() < cards.size && plusTwo.isNotEmpty()) {
            val card = attemptPlusTwo()

            if (card != null)
                return card
        }

        // If we've got more cards, attempt to skip them to be able to use more
        if (unoGame.user.cards.getSize() < cards.size && skip.isNotEmpty()) {
            val card = attemptSkip()

            if (card != null)
                return card
        }

        var card = attemptReverse()

        if (card != null)
            return card

        // We don't care anymore
        attemptSkip()
        attemptPlusTwo()

        card = attemptRegular()

        if (card != null)
            return card

        card = attemptWild()

        if (card != null)
            return card

        // Keep drawing until it finds the right card
        unoGame.buta.draw(1)
        drawAttempts++

        calculate()

        return play()
    }

    /**
     * The amount of times the bot had to draw until it found a card.
     */
    private var drawAttempts = 0

    /**
     * Attempts to play a wild [Card]. This is done when nothing else can played.
     */
    private fun attemptWild(): Card? {
        val draw = wildDraw.isNotEmpty()
        val reg = wild.isNotEmpty()

        if (draw || reg) {
            val largest = getLargestCard()

            when {
                // Draw isn't empty and regulars are: Play draw
                draw && !reg -> return Card(largest, CardType.WILD_DRAW, null)

                // Draw is empty and regulars aren't: Play regular
                reg && !draw -> return Card(largest, CardType.WILD_DEFAULT, null)

                draw && reg -> {
                    // If the user has more cards, play a regular one as we're ahead.
                    return if (unoGame.user.cards.getSize() > unoGame.user.cards.getSize()) {
                        Card(largest, CardType.WILD_DEFAULT, null)
                    } else { // If we're the same, or they're ahead, use +4
                        Card(largest, CardType.WILD_DRAW, null)
                    }
                }

                else -> throw Exception("Issue with attemptWild()")
            }
        }

        return null
    }

    /**
     * Attempts to play a +2 [Card]. Returns null if not.
     */
    private fun attemptPlusTwo(): Card? {
        plusTwo.forEach { crd ->
            if (recentCard.color == crd.color)
                return crd
        }

        return null
    }

    /**
     * Attempts to play a skip [Card]. Returns null if not.
     */
    private fun attemptSkip(): Card? {
        skip.forEach { crd ->
            if (recentCard.color == crd.color)
                return crd
        }

        return null
    }


    /**
     * Attempts to play a reverse [Card]. Returns null if not.
     *
     * This is basically useless, since it gets to the user either way. Used as a regular card.
     */
    private fun attemptReverse(): Card? {
        reverse.forEach { crd ->
            if (recentCard.color == crd.color)
                return crd
        }

        return null
    }

    /**
     * Attempts to play a regular [Card]. Returns null if not available.
     *
     * Checks color as well as the number
     */
    private fun attemptRegular(): Card? {
        reg.forEach { crd ->
            if (recentCard.color == crd.color || (recentCard.num != null && recentCard.num == crd.num))
                return crd
        }

        return null
    }

    /**
     * Gets the card color that has the most amount of [Card]s in Buta's deck.
     */
    private fun getLargestCard(): CardColor {
        var yellow = 0
        var green = 0
        var blue = 0
        var red = 0

        cards.forEach { card ->
            when (card.color) {
                CardColor.YELLOW -> yellow++
                CardColor.GREEN -> green++
                CardColor.BLUE -> blue++
                CardColor.RED -> red++
                else -> {
                }
            }
        }

        var biggestCard: CardColor? = null
        var biggestVal = 0

        arrayListOf(Pair(CardColor.YELLOW, yellow), Pair(CardColor.GREEN, green), Pair(CardColor.BLUE, blue), Pair(CardColor.RED, red)).forEach {
            if (it.second > biggestVal) {
                biggestCard = it.first
                biggestVal = it.second
            }
        }

        return biggestCard
                ?: arrayListOf(Pair(CardColor.YELLOW, yellow), Pair(CardColor.GREEN, green), Pair(CardColor.BLUE, blue), Pair(CardColor.RED, red))
                        .single { it.second > 0 }
                        .first
    }
}