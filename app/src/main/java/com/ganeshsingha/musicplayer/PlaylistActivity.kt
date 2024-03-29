package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganeshsingha.musicplayer.databinding.ActivityMainBinding
import com.ganeshsingha.musicplayer.databinding.ActivityPlaylistBinding
import com.ganeshsingha.musicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistViewAdapter: PlaylistViewAdapter

    companion object{
        var musicPlaylist:MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(R.style.coolPink)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.playlistRecyclerView.setHasFixedSize(true)
        binding.playlistRecyclerView.setItemViewCacheSize(13)
        binding.playlistRecyclerView.layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
        playlistViewAdapter = PlaylistViewAdapter(this@PlaylistActivity, playlistList = musicPlaylist.ref)
        binding.playlistRecyclerView.adapter = playlistViewAdapter
        binding.backButtonPLA.setOnClickListener {
            finish()
        }
        binding.addPlaylistButton.setOnClickListener {
            customAlertDialog()
        }
    }

    private fun customAlertDialog() {
        val customDialog = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Name")
            .setPositiveButton("ADD") { dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.yourName.text
                 if (playlistName != null && createdBy != null){
                     if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                         addPlaylist(playlistName.toString(),createdBy.toString())
                     }
                 }
                dialog.dismiss()
            }.show()

    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExist = false
        for (i in musicPlaylist.ref){
            if (name.equals(i.name)){
                playlistExist = true
                break
            }
        }
        if (playlistExist){
            Toast.makeText(this@PlaylistActivity,"Playlist Exist",Toast.LENGTH_SHORT).show()
        }else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calender = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calender)
            musicPlaylist.ref.add(tempPlaylist)
            playlistViewAdapter.refreshPlaylist()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playlistViewAdapter.notifyDataSetChanged()
    }
}