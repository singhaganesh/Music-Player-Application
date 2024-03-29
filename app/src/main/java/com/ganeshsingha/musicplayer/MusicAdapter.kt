package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ganeshsingha.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context: Context,private var musicList: ArrayList<Music>,private val playlistDetails: Boolean = false,val selectionActivity: Boolean = false) : RecyclerView.Adapter<MusicAdapter.MusicHolder>(){
    class MusicHolder(binding:MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDurationMV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MusicHolder {
        return MusicHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MusicAdapter.MusicHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
//        Glide.with(context)
//            .load(musicList[position].artUri)
//            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
//            .into(holder.image)
        when{
            playlistDetails ->{
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", position = position)
                }
            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.color_pink))
                    }
                    else{
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                    }
                }
            }
            else ->{
                holder.root.setOnClickListener {
                    when{
                        MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", position = position)
                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent(ref = "NowPlaying", position = PlayerActivity.songPosition)
                        else -> sendIntent(ref = "MusicAdapter", position = position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref:String,position:Int){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",position)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
    private fun addSong(song:Music):Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist.forEachIndexed { index, music ->
            if (song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist.add(song)
        return true
    }
    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPosition].playlist
        notifyDataSetChanged()
    }
}