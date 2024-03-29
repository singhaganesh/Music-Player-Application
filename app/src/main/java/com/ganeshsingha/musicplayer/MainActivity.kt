package com.ganeshsingha.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ganeshsingha.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var musicListMA: ArrayList<Music>
        lateinit var searchMusicList : ArrayList<Music>
        var search : Boolean = false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for navigation drawer
        toggle = ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //        requestRuntimePermission()
        if (requestRuntimePermission()){
            initializeLayout()
            // For retrieving favourite data using shared preferences
            FavouriteActivity.favouriteSongs = ArrayList()
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSong",null)
            val typeToken = object : TypeToken<ArrayList<Music>>(){}.type

            if (jsonString != null){
                val data:ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }
            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("MusicPlaylist",null)

            if (jsonStringPlaylist != null){
                val dataPlaylist:MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist,MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }
        }
        binding.shuffleButton.setOnClickListener {
            val intent = Intent(this@MainActivity,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }
        binding.favouriteButton.setOnClickListener {
            startActivity(Intent(this@MainActivity,FavouriteActivity::class.java))
        }
        binding.playlistButton.setOnClickListener {
            startActivity(Intent(this@MainActivity,PlaylistActivity::class.java))
        }
        binding.navigationView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.navFeedback ->
                    Toast.makeText(this@MainActivity,"coming soon",Toast.LENGTH_SHORT).show()
                R.id.navSetting ->
                    Toast.makeText(this@MainActivity,"coming soon",Toast.LENGTH_SHORT).show()
                R.id.navAbout ->
                    startActivity(Intent(this@MainActivity,AboutActivity::class.java))
                R.id.navExit -> {
                    if (!PlayerActivity.isPlaying){
                        exitApplication()
                    }
                    else{
                        finish()
                    }

                }
            }
            true
        }


    }
    fun showAlertDialog(){
        //  MaterialAlertDialogBuilder or AlertDialog
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Exit")
            .setMessage("Do you want to close app ?")
            .setPositiveButton("Yes") { _, _ ->
                exitApplication()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val customDialog = builder.create()
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


    // for requesting permission

    private fun requestRuntimePermission():Boolean{

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),13)
                return false
            }
            return true

        }else{
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),13)
                return false
            }
            return true
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
                initializeLayout()
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),13)
                }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),13)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun initializeLayout(){
        search = false
        musicListMA = getAllAudio()
        binding.musicRecyclerView.setHasFixedSize(true)
        binding.musicRecyclerView.setItemViewCacheSize(13)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, musicListMA)
        binding.musicRecyclerView.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : "+musicAdapter.itemCount
    }
    @SuppressLint("Range")
    private fun getAllAudio():ArrayList<Music>{
        val teamList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,selection,null,MediaStore.Audio.Media.DATE_ADDED + " DESC",null)

        if (cursor != null){
            if (cursor.moveToFirst()){
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content//media/external/audio/albumert")
                    val artUriC = Uri.withAppendedPath(uri,albumIdC).toString()
                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, duration = durationC, path = pathC, artUri = artUriC)
                    val file = File(music.path)
                    if (file.exists()){
                        teamList.add(music)
                    }
                }while (cursor.moveToNext())
                cursor.close()
            }
        }
        return teamList
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d("execute onDestroy","true")
//        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
//            exitApplication()
//        }
//    }

    override fun onResume() {
        super.onResume()
        // For storing favourite data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSong",jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.main.isDrawerOpen(GravityCompat.START)){
            binding.main.closeDrawer(GravityCompat.START)
        }
        else{
            if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
                exitApplication()
            }else{
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu,menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null){
                    searchMusicList = ArrayList()
                    val userInput = newText.lowercase()
                    for (song in musicListMA){
                        if (song.title.lowercase().contains(userInput)){
                            searchMusicList.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updateMusicList(searchList = searchMusicList)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}