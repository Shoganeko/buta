package dev.shog.buta.handle.uno.obj

import dev.shog.buta.handle.uno.UnoPlayCardResult

/**
 * A user within the Uno Game.
 */
class Player internal constructor(private val uno: UnoGame) {
    /**
     * The player's cards.
     */
    val cards = CardCollection()

    /**
     * Draws a card for the player [times] times.
     */
    fun draw(times: Int = 1): ArrayList<Card> {
        val drawn = arrayListOf<Card>()

        repeat(times) {
            cards.add(uno.drawCard().also {
                drawn.add(it)
            })
        }

        return drawn
    }

    /**
     * Play a card.
     *
     * @return A uno played card result.
     */
    fun play(card: Card): UnoPlayCardResult {
        // If the user's card array contains the card they're attempting to play, and if they can play the card.
        if (!cards.contains(card.getAsClearWild()) || !uno.canPlayCard(card.getAsClearWild()))
            return UnoPlayCardResult(successful = false, shouldSkipTurn = false)

        // If the next user's turn has been skipped
        var turnSkipped = false

        when (card.type) {
            // Changes the color
            CardType.WILD_DEFAULT, CardType.REVERSE, CardType.REGULAR -> {
            }

            // Changes color and the other user and draws 4
            CardType.WILD_DRAW -> {
                turnSkipped = true
                uno.getOtherUser(this).draw(4)
            }

            // Skips the other user's turn
            CardType.SKIP -> {
                turnSkipped = true
            }

            // Gets the other user and draws 2
            CardType.DRAW_TWO -> {
                turnSkipped = true
                uno.getOtherUser(this).draw(2)
            }
        }

        // Removes the card from the list, and adds to the played cards.
        cards.remove(card)
        uno.playedCards.add(card)

        return UnoPlayCardResult(true, turnSkipped)
    }
}