package com.insane.eyewalk.config

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri.*

class Player(private val context: Context, private val media: Int) {

    private var mediaPlayer = MediaPlayer()

    fun play() {
        mediaPlayer.setDataSource(this.context, parse("android.resource://${this.context.packageName}/${media}"))
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    fun stop() {
        mediaPlayer.stop()
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
    }

}