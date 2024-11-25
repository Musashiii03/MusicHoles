package com.example.musicholes.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicholes.PlaySong;
import com.example.musicholes.R;

import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> songs; // List of song names
    private ArrayList<String> songPaths; // List of corresponding song paths

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        listView = findViewById(R.id.listView1);
        songs = new ArrayList<>();
        songPaths = new ArrayList<>();

        // Get the songs passed from DashboardFragment
        Intent intent = getIntent();
        songPaths = intent.getStringArrayListExtra("songs");

        if (songPaths != null && !songPaths.isEmpty()) {
            for (String songPath : songPaths) {
                String songName = songPath.substring(songPath.lastIndexOf("/") + 1);
                songs.add(songName);
            }

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                    String selectedSongPath = songPaths.get(position);
                    Intent playIntent = new Intent(SongListActivity.this, PlaySong.class);
                    playIntent.putExtra("songList", songPaths); // Pass the song list
                    playIntent.putExtra("Position", position); // Pass the selected position
                    startActivity(playIntent);
                }
            });
        } else {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no songs are found
        }
    }
}