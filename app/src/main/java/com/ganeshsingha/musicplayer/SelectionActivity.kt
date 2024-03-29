package com.ganeshsingha.musicplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganeshsingha.musicplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySelectionBinding
    private lateinit var musicAdapter: MusicAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setTheme(R.style.coolPink)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.selectionRecyclerView.setItemViewCacheSize(10)
        binding.selectionRecyclerView.setHasFixedSize(true)
        binding.selectionRecyclerView.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this,MainActivity.musicListMA, selectionActivity = true)
        binding.selectionRecyclerView.adapter = musicAdapter

        binding.backButtonSA.setOnClickListener {
            finish()
        }

        // For search view
        binding.searchViewSA.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null){
                    MainActivity.searchMusicList = ArrayList()
                    val userInput = newText.lowercase()
                    for (song in MainActivity.musicListMA){
                        if (song.title.lowercase().contains(userInput)){
                            MainActivity.searchMusicList.add(song)
                        }
                    }
                    MainActivity.search = true
                    musicAdapter.updateMusicList(searchList = MainActivity.searchMusicList)
                }
                return true
            }
        })
    }
}