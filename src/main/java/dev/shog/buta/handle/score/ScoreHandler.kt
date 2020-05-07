package dev.shog.buta.handle.score

object ScoreHandler {
    /**
     * A [user]'s scoreset.
     */
    data class ScoreSet(
            val user: Long,
            val one: Int,
            val two: Int,
            val three: Int,
            val oneLabel: String,
            val twoLabel: String,
            val threeLabel: String
    )
}