package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ganeshsingha.musicplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding:ActivityPlaylistDetailsBinding
    lateinit var musicAdapter: MusicAdapter

    companion object{
        var currentPosition:Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setTheme(R.style.coolPink)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        currentPosition = intent.extras?.getInt("index") as Int
        PlaylistActivity.musicPlaylist.ref[currentPosition].playlist = checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPosition].playlist)
        binding.playlistDetailsRecyclerView.setItemViewCacheSize(10)
        binding.playlistDetailsRecyclerView.setHasFixedSize(true)
        binding.playlistDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this,PlaylistActivity.musicPlaylist.ref[currentPosition].playlist,true)
        binding.playlistDetailsRecyclerView.adapter = musicAdapter

        binding.backButtonPDA.setOnClickListener {
            finish()
        }
        binding.shuffleButtonPDA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }
        binding.addButtonPDA.setOnClickListener {
            startActivity(Intent(this,SelectionActivity::class.java))
        }
        binding.removeAllButtonPDA.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPosition].playlist.clear()
                    musicAdapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.playlistNamePDA.text = PlaylistActivity.musicPlaylist.ref[currentPosition].name
        binding.moreInfoPDA.text = "Total ${musicAdapter.itemCount} Songs.\n\n"+
                "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPosition].createdOn}\n\n"+
                " -- ${PlaylistActivity.musicPlaylist.ref[currentPosition].createdBy}"
        if (musicAdapter.itemCount>0){
//            Glide.with(this)
//                .load(PlaylistActivity.musicPlaylist.ref[currentPosition].playlist[0].artUri)
//                .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
//                .into(binding.playlistImagePDA)
            binding.shuffleButtonPDA.visibility = View.VISIBLE
        }
        musicAdapter.notifyDataSetChanged()
        // For storing favourite data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}