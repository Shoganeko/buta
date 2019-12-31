package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason


/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue: ArrayList<AudioTrack> = arrayListOf()

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.add(track)
        }
    }

    /**
     * Get tracks
     */
    fun getTracks(): List<AudioTrack> =
            queue.toList()

    /**
     * Clear tracks
     */
    fun clearTracks() {
        queue.clear()
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack(): AudioTrack? {
        if (queue.isEmpty())
            return null

        val newTrack = queue.first()
        queue.removeAt(0)

        player.startTrack(newTrack, false)
        return newTrack
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}