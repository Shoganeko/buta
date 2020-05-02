package dev.shog.buta.handle.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason


/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
class TrackScheduler(private val parent: GuildMusicManager) : AudioEventAdapter() {
    private val queue: MutableList<AudioTrack> = mutableListOf()

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack) {
        parent.rescheduleTimer() // account for the activity

        if (!parent.player.startTrack(track, true)) {
            queue.add(track)
        }
    }

    /**
     * Get tracks
     */
    fun getTracks(): List<AudioTrack> = queue

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

        parent.player.startTrack(newTrack, false)
        return newTrack
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}