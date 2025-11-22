package com.movieapp.tv

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : FragmentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingIndicator: ProgressBar
    private var videoUrl: String? = null

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        loadingIndicator = findViewById(R.id.loading_indicator)
        
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        if (videoUrl == null) return

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(videoUrl!!)
        player?.setMediaItem(mediaItem)
        
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> loadingIndicator.visibility = View.VISIBLE
                    Player.STATE_READY -> loadingIndicator.visibility = View.GONE
                    Player.STATE_ENDED -> finish()
                    else -> loadingIndicator.visibility = View.GONE
                }
            }
        })

        player?.prepare()
        player?.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
