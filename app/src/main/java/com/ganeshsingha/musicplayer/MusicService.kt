package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.app.MediaRouteButton
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat

class MusicService : Service(),AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }
    inner class MyBinder:Binder(){
        fun currentService(): MusicService {
            return this@MusicService
        }
    }
    @SuppressLint("ForegroundServiceType")
    fun showNotification(playPauseButton: Int,playbackSpeed:Float){

        val intent = Intent(baseContext,MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val previousIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(baseContext,0,previousIntent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val  exitPendingIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)


        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPV[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPV[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.music_player_icon))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(R.drawable.previous,"Previous",previousPendingIntent)
            .addAction(playPauseButton,"Play",playPendingIntent)
            .addAction(R.drawable.next,"Next",nextPendingIntent)
            .addAction(R.drawable.exit_icon,"Exit",exitPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(),playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

        startForeground(13,notification)
    }
    fun createMediaPlayer(){
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) {
                PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            }
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPV[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,0F)
            PlayerActivity.binding.textViewSeekbarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.textViewSeekbarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPV[PlayerActivity.songPosition].id
        }catch (e:Exception){
            return
        }
    }
    fun seekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.textViewSeekbarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            // pause music
            PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.play_icon)
            NowPlayingFragment.binding.playPauseButtonNPF.setIconResource(R.drawable.play_icon)
            showNotification(R.drawable.play_icon,0F)
            PlayerActivity.isPlaying = false
            Log.d("audioPlaying", "false")
            mediaPlayer!!.pause()
        }
        else{
            // play music
            PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.pause_icon)
            NowPlayingFragment.binding.playPauseButtonNPF.setIconResource(R.drawable.pause_icon)
            showNotification(R.drawable.pause_icon,1F)
            PlayerActivity.isPlaying = true
            Log.d("audioPlaying", "true")
            mediaPlayer!!.start()
        }
    }

}