package com.ganeshsingha.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlin.system.exitProcess

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            when (intent.action) {
                ApplicationClass.PREVIOUS -> {
                    prevNextSong(false, context)
                }

                ApplicationClass.PLAY -> {
                    if (PlayerActivity.isPlaying){
                        pauseMusic()
                    }
                    else{
                        playMusic()
                    }
                }

                ApplicationClass.NEXT -> {
                    prevNextSong(true, context)
                }

                ApplicationClass.EXIT -> {
                    exitApplication()
                }
            }
        }
    }
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,1F)
        PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.pause_icon)
        NowPlayingFragment.binding.playPauseButtonNPF.setIconResource(R.drawable.pause_icon)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon,0F)
        PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.play_icon)
        NowPlayingFragment.binding.playPauseButtonNPF.setIconResource(R.drawable.play_icon)
    }
    private fun prevNextSong(increment: Boolean,context: Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPV[PlayerActivity.songPosition].title
        NowPlayingFragment.binding.songNameNPF.text = PlayerActivity.musicListPV[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPV[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite){
            PlayerActivity.binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
        }else{
            PlayerActivity.binding.favouriteButtonPA.setImageResource(R.drawable.favourite_empty)
        }
    }

}