package dev.shog.buta.handle.uno.obj

/**
 * The cards
 */
class CardCollection {
    /**
     * The size of [cards].
     */
    fun getSize() = cards.size

    /**
     * The player's cards.
     */
    val cards = ArrayList<Card>()

    /**
     * Add a card
     */
    fun add(card: Card) {
        cards.add(card)
    }

    /**
     * If the collection contains the inputted card, remove it.
     */
    fun remove(card: Card) {
        when (card.type) {
            CardType.WILD_DEFAULT -> {
                cards.forEach {
                    if (it.type == CardType.WILD_DEFAULT) {
                        cards.remove(it)
                        return
                    }
                }
            }

            CardType.WILD_DRAW -> {
                cards.forEach {
                    if (it.type == CardType.WILD_DRAW) {
                        cards.remove(it)
                        return
                    }
                }
            }

            else -> cards.remove(card)
        }
    }

    /**
     * If the collection contains the inputted card.
     */
    fun contains(card: Card): Boolean {
        when (card.type) {
            CardType.WILD_DEFAULT -> {
                cards.forEach {
                    if (it.type == CardType.WILD_DEFAULT)
                        return true
                }

                return false
            }

            CardType.WILD_DRAW -> {
                cards.forEach {
                    if (it.type == CardType.WILD_DRAW)
                        return true
                }

                return false
            }

            CardType.REGULAR -> {
                cards.forEach {
                    if (it.color == card.color || it.num == card.num)
                        return true
                }

                return false
            }

            CardType.REVERSE, CardType.DRAW_TWO, CardType.SKIP -> {
                cards.forEach {
                    if (it.type == card.type || it.color == card.color)
                        return true
                }

                return false
            }
        }
    }
}