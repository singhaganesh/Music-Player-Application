package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.ganeshsingha.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val  view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.songNameNPF.isSelected = true
        binding.playPauseButtonNPF.setOnClickListener {
            if (PlayerActivity.isPlaying){
                pauseMusic()
            }
            else{
                playMusic()
            }
        }
        binding.nextButtonNPF.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            binding.songNameNPF.text = PlayerActivity.musicListPV[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,1F)
            playMusic()

        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java)
            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNPF.text = PlayerActivity.musicListPV[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying){
                binding.playPauseButtonNPF.setIconResource(R.drawable.pause_icon)
            }else{
                binding.playPauseButtonNPF.setIconResource(R.drawable.play_icon)
            }
        }
    }
    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseButtonNPF.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,1F)
        PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }
    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseButtonNPF.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon,0F)
        PlayerActivity.binding.playPauseButton.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }

}