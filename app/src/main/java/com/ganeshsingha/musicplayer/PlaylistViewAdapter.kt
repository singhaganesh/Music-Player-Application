package com.ganeshsingha.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ganeshsingha.musicplayer.databinding.FavouriteViewBinding
import com.ganeshsingha.musicplayer.databinding.PlaylistViewBinding

class PlaylistViewAdapter(private val context: Context, private var playlistList: ArrayList<Playlist>) : RecyclerView.Adapter<PlaylistViewAdapter.PlaylistHolder>(){
    class PlaylistHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImage
        val name = binding.playlistName
        val delete = binding.playlistDeleteButton
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        return PlaylistHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete playlist")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                   dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
        }
        holder.root.setOnClickListener {
            val intent = Intent(context,PlaylistDetails::class.java)
            intent.putExtra("index",position)
            ContextCompat.startActivity(context,intent,null)
        }
//        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size>0){
//            Glide.with(context)
//                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
//                .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
//                .into(holder.image)
//        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }
    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}