package com.cuckoo.app

import android.media.MediaPlayer

object MediaPlayerHolder {
    private var player: MediaPlayer? = null

    fun set(mp: MediaPlayer) {
        player?.release()
        player = mp
    }

    fun stop() {
        player?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        player = null
    }
}
