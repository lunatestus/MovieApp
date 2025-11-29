package com.movieapp.tv

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView

/**
 * Video Player with Space Mono font, fade animations, and custom seeking
 */
class PlayerActivity : FragmentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var videoUrl: String? = null

    // Control views
    private var controlsRoot: View? = null
    private var topGradientOverlay: View? = null
    private var gradientOverlay: View? = null
    private var bottomControls: View? = null
    private var videoTitleView: android.widget.TextView? = null
    private var playPauseButton: ImageButton? = null
    private var playPauseContainer: View? = null
    private var progressBar: DefaultTimeBar? = null
    private var audioTrackButton: View? = null
    private var subtitleButton: View? = null
    private var loadingSpinner: ProgressBar? = null
    private var videoTitle: String? = null

    // Controls visibility
    private var areControlsVisible = true
    private val controlsHandler = Handler(Looper.getMainLooper())
    private val hideControlsDelay = 4000L
    private val fadeAnimationDuration = 250L

    // Seeking configuration
    private val baseSeekMs = 10_000L // 10 seconds base seek
    private val acceleratedSeekMs = 90_000L // 90 seconds when holding (very fast)
    private val seekAccelerationDelayMs = 200L // Time before acceleration kicks in (very quick)
    private val seekRepeatIntervalMs = 50L // Interval for repeated seeks when holding (very fast)

    private var isSeekingForward = false
    private var isSeekingBackward = false
    private var seekHoldStartTime = 0L
    private val seekHandler = Handler(Looper.getMainLooper())

    // Focus tracking for D-pad navigation
    private enum class FocusedControl { PROGRESS_BAR, PLAY_PAUSE, AUDIO_TRACK, SUBTITLE }
    private var currentFocus = FocusedControl.PLAY_PAUSE

    private val hideControlsRunnable = Runnable {
        hideControls()
    }

    private val seekForwardRunnable = object : Runnable {
        override fun run() {
            if (isSeekingForward) {
                val seekAmount = getSeekAmount()
                seekBy(seekAmount)
                seekHandler.postDelayed(this, seekRepeatIntervalMs)
            }
        }
    }

    private val seekBackwardRunnable = object : Runnable {
        override fun run() {
            if (isSeekingBackward) {
                val seekAmount = getSeekAmount()
                seekBy(-seekAmount)
                seekHandler.postDelayed(this, seekRepeatIntervalMs)
            }
        }
    }

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE)
        playerView = findViewById(R.id.player_view)
        loadingSpinner = findViewById(R.id.loading_spinner)

        setupLoadingSpinner()
        setupSubtitleStyle()
    }

    private fun setupLoadingSpinner() {
        // Start smooth fast rotation animation
        loadingSpinner?.apply {
            visibility = View.VISIBLE
            animate()
                .rotationBy(360f)
                .setDuration(500)
                .setInterpolator(LinearInterpolator())
                .withEndAction(object : Runnable {
                    override fun run() {
                        if (visibility == View.VISIBLE) {
                            rotation = 0f
                            animate()
                                .rotationBy(360f)
                                .setDuration(500)
                                .setInterpolator(LinearInterpolator())
                                .withEndAction(this)
                                .start()
                        }
                    }
                })
                .start()
        }
    }

    private fun showLoading() {
        loadingSpinner?.visibility = View.VISIBLE
        setupLoadingSpinner()
    }

    private fun hideLoading() {
        loadingSpinner?.animate()?.cancel()
        loadingSpinner?.visibility = View.GONE
    }

    @OptIn(UnstableApi::class)
    private fun setupSubtitleStyle() {
        val spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.space_mono)

        val captionStyle = CaptionStyleCompat(
            Color.WHITE,
            Color.parseColor("#80000000"),
            Color.TRANSPARENT,
            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
            Color.BLACK,
            spaceMonoTypeface
        )

        playerView.subtitleView?.setStyle(captionStyle)
        playerView.subtitleView?.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18f)
    }

    @OptIn(UnstableApi::class)
    private fun setupControlViews() {
        // Get references to control views
        controlsRoot = playerView.findViewById(R.id.controls_root)
        topGradientOverlay = playerView.findViewById(R.id.top_gradient_overlay)
        gradientOverlay = playerView.findViewById(R.id.gradient_overlay)
        bottomControls = playerView.findViewById(R.id.bottom_controls)
        videoTitleView = playerView.findViewById(R.id.video_title)
        playPauseButton = playerView.findViewById(androidx.media3.ui.R.id.exo_play_pause)
        playPauseContainer = playerView.findViewById(R.id.play_pause_container)
        progressBar = playerView.findViewById(androidx.media3.ui.R.id.exo_progress)
        audioTrackButton = playerView.findViewById(R.id.btn_audio_track)
        subtitleButton = playerView.findViewById(R.id.btn_subtitle)

        // Set video title
        videoTitleView?.text = videoTitle ?: ""

        // Disable all native focus - we handle D-pad navigation ourselves
        playPauseButton?.isFocusable = false
        playPauseContainer?.isFocusable = false
        audioTrackButton?.isFocusable = false
        subtitleButton?.isFocusable = false
        progressBar?.isFocusable = false

        // Set initial visibility
        controlsRoot?.alpha = 1f
        topGradientOverlay?.alpha = 1f
        gradientOverlay?.alpha = 1f
        bottomControls?.alpha = 1f
        videoTitleView?.alpha = 1f

        // Disable default controller auto-hide (we handle it ourselves)
        playerView.controllerShowTimeoutMs = -1
        playerView.controllerHideOnTouch = false

        // Update focus visual
        updateFocusVisual()

        // Schedule auto-hide
        scheduleHideControls()
    }

    private fun showControls() {
        if (!areControlsVisible) {
            areControlsVisible = true
            // Reset focus to play/pause button when controls become visible
            currentFocus = FocusedControl.PLAY_PAUSE
            updateFocusVisual()
            // Enable touch events
            controlsRoot?.visibility = View.VISIBLE
            // Fade in all controls together
            topGradientOverlay?.animate()?.alpha(1f)?.setDuration(fadeAnimationDuration)?.start()
            videoTitleView?.animate()?.alpha(1f)?.setDuration(fadeAnimationDuration)?.start()
            gradientOverlay?.animate()?.alpha(1f)?.setDuration(fadeAnimationDuration)?.start()
            bottomControls?.animate()?.alpha(1f)?.setDuration(fadeAnimationDuration)?.start()
        }
        scheduleHideControls()
    }

    private fun hideControls() {
        if (areControlsVisible) {
            areControlsVisible = false
            // Fade out all controls together, then disable touch events
            topGradientOverlay?.animate()?.alpha(0f)?.setDuration(fadeAnimationDuration)?.start()
            videoTitleView?.animate()?.alpha(0f)?.setDuration(fadeAnimationDuration)?.start()
            gradientOverlay?.animate()?.alpha(0f)?.setDuration(fadeAnimationDuration)?.start()
            bottomControls?.animate()?.alpha(0f)?.setDuration(fadeAnimationDuration)
                ?.withEndAction {
                    // Disable touch events after fade completes
                    controlsRoot?.visibility = View.INVISIBLE
                }?.start()
        }
    }

    private fun toggleControls() {
        if (areControlsVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun scheduleHideControls() {
        controlsHandler.removeCallbacks(hideControlsRunnable)
        controlsHandler.postDelayed(hideControlsRunnable, hideControlsDelay)
    }

    @OptIn(UnstableApi::class)
    private fun updateFocusVisual() {
        // Visual feedback for focused control
        val isPlayPauseFocused = currentFocus == FocusedControl.PLAY_PAUSE
        val isProgressFocused = currentFocus == FocusedControl.PROGRESS_BAR
        val isAudioFocused = currentFocus == FocusedControl.AUDIO_TRACK
        val isSubtitleFocused = currentFocus == FocusedControl.SUBTITLE

        // Play/pause container - white border when focused
        playPauseContainer?.setBackgroundResource(
            if (isPlayPauseFocused) R.drawable.play_button_focused_bg
            else R.drawable.play_button_bg
        )
        playPauseContainer?.alpha = if (isPlayPauseFocused) 1f else 0.7f

        // Audio track button - white border when focused
        audioTrackButton?.setBackgroundResource(
            if (isAudioFocused) R.drawable.capsule_button_focused_bg
            else R.drawable.capsule_button_bg
        )
        audioTrackButton?.alpha = if (isAudioFocused) 1f else 0.7f

        // Subtitle button - white border when focused
        subtitleButton?.setBackgroundResource(
            if (isSubtitleFocused) R.drawable.capsule_button_focused_bg
            else R.drawable.capsule_button_bg
        )
        subtitleButton?.alpha = if (isSubtitleFocused) 1f else 0.7f

        // Show/hide scrubber based on progress bar focus
        progressBar?.setScrubberColor(if (isProgressFocused) Color.WHITE else Color.TRANSPARENT)
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

        player = ExoPlayer.Builder(this).build().apply {
            // Use closest sync for smoother seeking
            setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
        playerView.player = player

        // Setup control views after player is attached
        playerView.post { setupControlViews() }

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Always schedule hide controls, regardless of play state
                scheduleHideControls()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> showLoading()
                    Player.STATE_READY -> hideLoading()
                    Player.STATE_ENDED -> hideLoading()
                    Player.STATE_IDLE -> hideLoading()
                }
            }
        })

        val mediaItem = MediaItem.fromUri(videoUrl!!)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun releasePlayer() {
        controlsHandler.removeCallbacksAndMessages(null)
        seekHandler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }

    private fun getSeekAmount(): Long {
        val holdDuration = System.currentTimeMillis() - seekHoldStartTime
        return if (holdDuration > seekAccelerationDelayMs) {
            acceleratedSeekMs
        } else {
            baseSeekMs
        }
    }

    private fun seekBy(deltaMs: Long) {
        player?.let { p ->
            val currentPos = p.currentPosition
            val duration = p.duration
            val newPos = (currentPos + deltaMs).coerceIn(0, duration)
            p.seekTo(newPos)
        }
    }

    private fun togglePlayPause() {
        player?.let { p ->
            if (p.isPlaying) {
                p.pause()
            } else {
                p.play()
            }
        }
    }

    // Intercept all key events before they reach any views
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.dispatchKeyEvent(event)

        // Handle key down events
        if (event.action == KeyEvent.ACTION_DOWN) {
            val handled = handleKeyDown(event.keyCode, event)
            if (handled) return true
        }

        // Handle key up events
        if (event.action == KeyEvent.ACTION_UP) {
            val handled = handleKeyUp(event.keyCode, event)
            if (handled) return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun handleKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle back button separately - check visibility BEFORE showing controls
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (areControlsVisible) {
                // If controls are visible, hide them instead of exiting
                hideControls()
                controlsHandler.removeCallbacks(hideControlsRunnable)
            } else {
                // If controls are hidden, exit the player
                finish()
            }
            return true
        }

        // Show controls on any other key press
        if (!areControlsVisible) {
            showControls()
            // If just showing controls, consume the event
            return true
        }

        when (keyCode) {

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                showControls()
                when (currentFocus) {
                    FocusedControl.PLAY_PAUSE -> togglePlayPause()
                    FocusedControl.PROGRESS_BAR -> togglePlayPause()
                    FocusedControl.AUDIO_TRACK -> showAudioTrackSelector()
                    FocusedControl.SUBTITLE -> showSubtitleTrackSelector()
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                showControls()
                togglePlayPause()
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                showControls()
                when (currentFocus) {
                    FocusedControl.PROGRESS_BAR -> {
                        // Seek backward
                        if (!isSeekingBackward) {
                            isSeekingBackward = true
                            seekHoldStartTime = System.currentTimeMillis()
                            seekBy(-baseSeekMs)
                            seekHandler.postDelayed(seekBackwardRunnable, seekRepeatIntervalMs)
                        }
                    }
                    FocusedControl.PLAY_PAUSE -> {
                        // Already at leftmost on bottom row, do nothing
                    }
                    FocusedControl.AUDIO_TRACK -> {
                        // Move to play/pause
                        currentFocus = FocusedControl.PLAY_PAUSE
                        updateFocusVisual()
                    }
                    FocusedControl.SUBTITLE -> {
                        // Move to audio track
                        currentFocus = FocusedControl.AUDIO_TRACK
                        updateFocusVisual()
                    }
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                showControls()
                when (currentFocus) {
                    FocusedControl.PROGRESS_BAR -> {
                        // Seek forward
                        if (!isSeekingForward) {
                            isSeekingForward = true
                            seekHoldStartTime = System.currentTimeMillis()
                            seekBy(baseSeekMs)
                            seekHandler.postDelayed(seekForwardRunnable, seekRepeatIntervalMs)
                        }
                    }
                    FocusedControl.PLAY_PAUSE -> {
                        // Move to audio track
                        currentFocus = FocusedControl.AUDIO_TRACK
                        updateFocusVisual()
                    }
                    FocusedControl.AUDIO_TRACK -> {
                        // Move to subtitle
                        currentFocus = FocusedControl.SUBTITLE
                        updateFocusVisual()
                    }
                    FocusedControl.SUBTITLE -> {
                        // Already at rightmost, do nothing
                    }
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                // Move focus from progress bar to bottom row
                showControls()
                if (currentFocus == FocusedControl.PROGRESS_BAR) {
                    currentFocus = FocusedControl.PLAY_PAUSE
                    updateFocusVisual()
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                // Move focus from bottom row to progress bar
                showControls()
                when (currentFocus) {
                    FocusedControl.PLAY_PAUSE,
                    FocusedControl.AUDIO_TRACK,
                    FocusedControl.SUBTITLE -> {
                        currentFocus = FocusedControl.PROGRESS_BAR
                        updateFocusVisual()
                    }
                    else -> {}
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                showControls()
                if (!isSeekingForward) {
                    isSeekingForward = true
                    seekHoldStartTime = System.currentTimeMillis()
                    seekBy(baseSeekMs)
                    seekHandler.postDelayed(seekForwardRunnable, seekRepeatIntervalMs)
                }
                return true
            }

            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                showControls()
                if (!isSeekingBackward) {
                    isSeekingBackward = true
                    seekHoldStartTime = System.currentTimeMillis()
                    seekBy(-baseSeekMs)
                    seekHandler.postDelayed(seekBackwardRunnable, seekRepeatIntervalMs)
                }
                return true
            }
        }
        return false
    }

    private fun handleKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                isSeekingForward = false
                seekHandler.removeCallbacks(seekForwardRunnable)
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                isSeekingBackward = false
                seekHandler.removeCallbacks(seekBackwardRunnable)
                return true
            }
        }
        return false
    }

    @OptIn(UnstableApi::class)
    private fun showAudioTrackSelector() {
        val p = player ?: return

        val trackGroups = mutableListOf<Pair<String, Tracks.Group>>()

        for (group in p.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                trackGroups.add(Pair(getTrackName(group, 0), group))
            }
        }

        if (trackGroups.isEmpty()) {
            return
        }

        val trackNames = trackGroups.map { it.first }.toTypedArray()
        var selectedIndex = trackGroups.indexOfFirst { it.second.isSelected }
        if (selectedIndex < 0) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Audio Track")
            .setSingleChoiceItems(trackNames, selectedIndex) { dialog, which ->
                val selectedGroup = trackGroups[which].second
                val params = p.trackSelectionParameters.buildUpon()
                    .setOverrideForType(
                        TrackSelectionOverride(selectedGroup.mediaTrackGroup, 0)
                    )
                    .build()
                p.trackSelectionParameters = params
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun showSubtitleTrackSelector() {
        val p = player ?: return

        val trackGroups = mutableListOf<Pair<String, Tracks.Group?>>()
        trackGroups.add(Pair("Off", null)) // Option to disable subtitles

        for (group in p.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                trackGroups.add(Pair(getTrackName(group, 0), group))
            }
        }

        val trackNames = trackGroups.map { it.first }.toTypedArray()

        // Find currently selected subtitle or "Off"
        var selectedIndex = 0
        for ((index, pair) in trackGroups.withIndex()) {
            if (pair.second?.isSelected == true) {
                selectedIndex = index
                break
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Subtitles")
            .setSingleChoiceItems(trackNames, selectedIndex) { dialog, which ->
                if (which == 0) {
                    // Disable subtitles
                    val params = p.trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                    p.trackSelectionParameters = params
                } else {
                    val selectedGroup = trackGroups[which].second
                    if (selectedGroup != null) {
                        val params = p.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setOverrideForType(
                                TrackSelectionOverride(selectedGroup.mediaTrackGroup, 0)
                            )
                            .build()
                        p.trackSelectionParameters = params
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun getTrackName(group: Tracks.Group, trackIndex: Int): String {
        val format = group.getTrackFormat(trackIndex)
        val language = format.language?.let { java.util.Locale(it).displayLanguage } ?: "Unknown"
        val label = format.label

        return when {
            !label.isNullOrEmpty() -> label
            else -> language
        }
    }
}
