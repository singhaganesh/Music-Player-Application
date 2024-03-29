package com.ganeshsingha.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ganeshsingha.musicplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlayerActivity : AppCompatActivity(),ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPV: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min_15: Boolean = false
        var min_30: Boolean = false
        var min_60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeLayout()
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.previousButtonPA.setOnClickListener {
            prevNextSong(false)
        }
        binding.nextButtonPA.setOnClickListener {
            prevNextSong(true)
        }
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.repeatButtonPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatButtonPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
            } else {
                repeat = false
                binding.repeatButtonPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.color_pink
                    )
                )
            }
        }
        binding.backButtonPA.setOnClickListener {
            finish()
        }
        binding.equalizerButtonPA.setOnClickListener {

            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(
                    this@PlayerActivity,
                    "Equalizer Feature not Supported!!",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
        binding.timerButtonPA.setOnClickListener {
            val timer = min_15 || min_30 || min_60
            if (!timer) {
                showBottomSheetDialog()
            } else {
//                MaterialAlertDialogBuilder or AlertDialog
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Stop timer")
                    .setMessage("Do you want to stop timer ?")
                    .setPositiveButton("Yes") { _, _ ->
                        min_15 = false
                        min_30 = false
                        min_60 = false
                        binding.timerButtonPA.setColorFilter(
                            ContextCompat.getColor(
                                this,
                                R.color.color_pink
                            )
                        )
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog: AlertDialog = builder.create()
                val positiveButton = customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val negativeButton = customDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                if (positiveButton != null && negativeButton != null) {
                    positiveButton.setTextColor(Color.RED)
                    negativeButton.setTextColor(Color.RED)
                    customDialog.show()
                } else {
                    Log.e("MainActivity", "Positive or negative button is null")
                }
            }
        }
        binding.shareButtonPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPV[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File !!"))
        }
        binding.favouriteButtonPA.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.favouriteButtonPA.setImageResource(R.drawable.favourite_empty)
                if (fIndex>=0){
                    FavouriteActivity.favouriteSongs.removeAt(fIndex)
                }
            }else{
                isFavourite = true
                binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
                FavouriteActivity.favouriteSongs.add(musicListPV[songPosition])
            }
        }
    }

    private fun setLayout() {
        fIndex = favouriteChecker(musicListPV[songPosition].id)
        binding.songNamePA.text = musicListPV[songPosition].title
        if (repeat) {
            binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if (min_15 || min_30 || min_60) {
            binding.timerButtonPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }
        if (isFavourite) {
            binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
        } else {
            binding.favouriteButtonPA.setImageResource(R.drawable.favourite_empty)
        }
    }
        private fun createMediaPlayer() {
            try {
                if (musicService!!.mediaPlayer == null) {
                    musicService!!.mediaPlayer = MediaPlayer()
                }
                musicService!!.mediaPlayer!!.reset()
                musicService!!.mediaPlayer!!.setDataSource(musicListPV[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                isPlaying = true
                Log.d("audioPlaying", "true")
                binding.playPauseButton.setIconResource(R.drawable.pause_icon)
                musicService!!.showNotification(R.drawable.pause_icon,1F)
                binding.textViewSeekbarStart.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.textViewSeekbarEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = 0
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                nowPlayingId = musicListPV[songPosition].id
            } catch (e: Exception) {
                return
            }
        }

        private fun initializeLayout() {
            songPosition = intent.getIntExtra("index", 0)
            when (intent.getStringExtra("class")) {
                "FavouriteAdapter" ->{
                    musicListPV = ArrayList()
                    musicListPV.addAll(FavouriteActivity.favouriteSongs)
                    startMusicService()
                    setLayout()
                }
                "NowPlaying" -> {
                    setLayout()
                    binding.textViewSeekbarStart.text =
                        formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                    binding.textViewSeekbarEnd.text =
                        formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                    binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                    binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                    if (isPlaying) {
                        binding.playPauseButton.setIconResource(R.drawable.pause_icon)
                    } else {
                        binding.playPauseButton.setIconResource(R.drawable.play_icon)
                    }
                }

                "MusicAdapterSearch" -> {
                    musicListPV = ArrayList()
                    musicListPV.addAll(MainActivity.searchMusicList)
                    startMusicService()
                    setLayout()
                }

                "MusicAdapter" -> {
                    musicListPV = ArrayList()
                    musicListPV.addAll(MainActivity.musicListMA)
                    startMusicService()
                    setLayout()
                }

                "MainActivity" -> {
                    musicListPV = ArrayList()
                    musicListPV.addAll(MainActivity.musicListMA)
                    musicListPV.shuffle()
                    startMusicService()
                    setLayout()
                }
                "FavouriteShuffle" ->{
                    musicListPV = ArrayList()
                    musicListPV.addAll(FavouriteActivity.favouriteSongs)
                    musicListPV.shuffle()
                    startMusicService()
                    setLayout()
                }
                "PlaylistDetailsAdapter" ->{
                    musicListPV = ArrayList()
                    musicListPV.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist)
                    startMusicService()
                    setLayout()
                }
                "PlaylistDetailsShuffle" ->{
                    musicListPV = ArrayList()
                    musicListPV.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist)
                    startMusicService()
                    musicListPV.shuffle()
                    setLayout()
                }
            }
        }

        private fun playMusic() {
            binding.playPauseButton.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon,1F)
            isPlaying = true
            Log.d("audioPlaying", "true")
            musicService!!.mediaPlayer!!.start()
        }

        private fun pauseMusic() {
            binding.playPauseButton.setIconResource(R.drawable.play_icon)
            musicService!!.showNotification(R.drawable.play_icon,0F)
            isPlaying = false
            Log.d("audioPlaying", "false")
            musicService!!.mediaPlayer!!.pause()
        }

        private fun prevNextSong(increment: Boolean) {
            if (increment) {
                setSongPosition(true)
                setLayout()
                createMediaPlayer()
            } else {
                setSongPosition(false)
                setLayout()
                createMediaPlayer()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            createMediaPlayer()
            musicService!!.seekBarSetup()
            musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(musicService,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }

        override fun onCompletion(mp: MediaPlayer?) {
            setSongPosition(true)
            createMediaPlayer()
            try {
                setLayout()
            } catch (e: Exception) {
                return
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 13 || resultCode == RESULT_OK) {
                return
            }

        }

        private fun showBottomSheetDialog() {
            val dialog = BottomSheetDialog(this@PlayerActivity)
            dialog.setContentView(R.layout.buttom_sheet_dialog_timer)
            dialog.show()
            dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.timerButtonPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
                min_15 = true
                Thread {
                    Thread.sleep((15 * 60 * 1000).toLong())
                    if (min_15) exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.timerButtonPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
                min_30 = true
                Thread {
                    Thread.sleep((30 * 60 * 1000).toLong())
                    if (min_30) exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
                Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT)
                    .show()
                binding.timerButtonPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
                min_60 = true
                Thread {
                    Thread.sleep((60 * 60 * 1000).toLong())
                    if (min_60) exitApplication()
                }.start()
                dialog.dismiss()
            }
        }

        private fun startMusicService() {
            // for starting service
            val intent = Intent(this, MusicService::class.java)
            bindService(intent, this, BIND_AUTO_CREATE)
            startService(intent)
        }
    private fun getMusicDetails(contentUri:Uri):Music{
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri,projection,null,null,null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music(id = "Unknown", title = path.toString(), album = "Unknown", artist = "Unknown",duration,
                artUri = "Unknown", path = path.toString())
        }finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPV[songPosition].id == "Unknown" && !isPlaying){
            exitApplication()
        }
    }

    }
