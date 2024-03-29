package com.ganeshsingha.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganeshsingha.musicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var favouriteAdapter: FavouriteAdapter

    companion object{
        var favouriteSongs:ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(R.style.coolPink)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        favouriteSongs = checkPlaylist(favouriteSongs)

        binding.backButtonFA.setOnClickListener {
            finish()
        }

        binding.favouriteRecyclerView.setHasFixedSize(true)
        binding.favouriteRecyclerView.setItemViewCacheSize(13)
        binding.favouriteRecyclerView.layoutManager = GridLayoutManager(this,3)
        favouriteAdapter = FavouriteAdapter(this@FavouriteActivity, favouriteSongs )
        binding.favouriteRecyclerView.adapter = favouriteAdapter

        if (favouriteSongs.size<1){
            binding.shuffleButtonFA.visibility = View.INVISIBLE
        }
        binding.shuffleButtonFA.setOnClickListener {
            val intent = Intent(this,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavouriteShuffle")
            startActivity(intent)
        }
    }
}