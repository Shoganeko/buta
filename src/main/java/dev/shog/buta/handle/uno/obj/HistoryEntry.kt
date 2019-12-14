package dev.shog.buta.handle.uno.obj

import kotlin.random.Random

/**
 * A history entry.
 *
 * @param time The time it occurred.
 * @param history The actual history test.
 */
data class HistoryEntry(val time: Long, val history: String, private val unique: Int = Random.nextInt())