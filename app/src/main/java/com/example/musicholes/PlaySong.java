package com.example.musicholes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PlaySong extends AppCompatActivity {

    private TextView songNameTextView, artistNameTextView, currentTimeTextView, durationTextView;
    private ImageView songThumbnailImageView;
    private SeekBar seekBar;
    private ImageView playButton, pauseButton, previousButton, nextButton, loopButton, shuffleButton;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private ArrayList<String> songList;
    private ArrayList<String> originalSongList; // Store the original order
    private int position;
    private boolean isLooping = false;
    private boolean isShuffling = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song); // Replace with your actual layout name

        songNameTextView = findViewById(R.id.songNameTextView);
        artistNameTextView = findViewById(R.id.textView3);
        currentTimeTextView = findViewById(R.id.textView);
        durationTextView = findViewById(R.id.textView2);
        songThumbnailImageView = findViewById(R.id.songThumbnailImageView);
        seekBar = findViewById(R.id.seekBar);
        playButton = findViewById(R.id.imageView5);
        pauseButton = findViewById(R.id.imageView7);
        previousButton = findViewById(R.id.imageView4);
        nextButton = findViewById(R.id.imageView6);
        loopButton = findViewById(R.id.imageView2); // Loop button
        shuffleButton = findViewById(R.id.imageView); // Shuffle button

        // Get the song list and position from the intent
        songList = getIntent().getStringArrayListExtra("songList");
        if (songList == null) {
            songList = new ArrayList<>();
        }
        originalSongList = new ArrayList<>(songList); // Store the original order
        position = getIntent().getIntExtra("Position", 0);

        if (songList != null && position < songList.size()) {
            playSong(position);
        }

        // Set up ImageView listeners
        playButton.setOnClickListener(v -> {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateSeekBar();
                updatePlayPauseButtons();
            }
        });

        pauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updatePlayPauseButtons();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (position > 0) {
                position--;
                playSong(position);
            } else {
                Toast.makeText(this, "No previous song", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (position < songList.size() - 1) {
                position++;
                playSong(position);
            } else {
                Toast.makeText(this, "No next song", Toast.LENGTH_SHORT).show();
            }
        });

        loopButton.setOnClickListener(v -> {
            isLooping = !isLooping;
            loopButton.setColorFilter(isLooping ?  0xFFC300  : 0xFFFFFFFF); // Change color to indicate loop status
        });

        shuffleButton.setOnClickListener(v -> {
            isShuffling = !isShuffling;
            shuffleButton.setColorFilter(isShuffling ? 0xFFC300  : 0xFFFFFFFF); // Change color to indicate shuffle status
            if (isShuffling) {
                Collections.shuffle(songList);
                position = 0; // Reset position to the first song after shuffling
                // Do not play immediately; wait until the current song finishes
            }
            else {
                songList = new ArrayList<>(originalSongList); // Reset to original order
                position = 0; // Reset position to the first song
            }
        });
    }

    private void playSong(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        String currentSongPath = songList.get(position);
        String currentSongName = new File(currentSongPath).getName().replace(".mp3", "");
        songNameTextView.setText(currentSongName);

        // Load the thumbnail
        loadThumbnail(currentSongPath);

        // Initialize media player
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(new File(currentSongPath)));
        mediaPlayer.start();

        // Update play/pause buttons
        updatePlayPauseButtons();

        // Set up the SeekBar
        seekBar.setMax(mediaPlayer.getDuration());
        durationTextView.setText(formatDuration(mediaPlayer.getDuration())); // Set duration text
        updateSeekBar();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser ) {
                if (fromUser ) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.post(updateSeekBarRunnable);
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            // Create a local copy of the position variable
            int currentPosition = position;
            if (isLooping) {
                playSong(currentPosition); // Loop the current song
            } else {
                if (isShuffling) {
                    currentPosition++; // Increment the local copy
                    if (currentPosition >= songList.size()) {
                        currentPosition = 0; // Loop back to the start if at the end
                    }
                    Log.d("PlaySong", "Shuffled to position: " + currentPosition);
                    playSong(currentPosition);
                } else {
                    playNextSong(); // Call the method to play the next song in normal order
                }
            }
        });

        // Update artist name
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(currentSongPath);
        String artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        artistNameTextView.setText(artistName != null ? artistName : "Unknown Artist");
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadThumbnail(String songPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(songPath);
            byte[] art = retriever.getEmbeddedPicture(); // Correct method name
            if (art != null) {
                // Load the thumbnail using Glide
                Glide.with(this)
                        .load(art)
                        .into(songThumbnailImageView);
            } else {
                // Fallback to default thumbnail if no art found
                songThumbnailImageView.setImageResource(R.drawable.ic_dashboard_black_24dp); // Default thumbnail
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle any errors that may occur
            songThumbnailImageView.setImageResource(R.drawable.ic_dashboard_black_24dp); // Default thumbnail on error
        } finally {
            if (retriever != null) {
                try {
                    retriever.release(); // Ensure retriever is released in finally block
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String formatDuration(int duration) {
        int minutes = (duration / 1000) / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void playNextSong() {
        if (position < (songList.size() - 1)) {
            position++; // Increment the global position variable
            playSong(position); // Call playSong with the updated position
        } else {
            finish(); // End activity if no more songs
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTimeTextView.setText(formatDuration(mediaPlayer.getCurrentPosition())); // Update current time
            handler.postDelayed(updateSeekBarRunnable, 1000);
        }
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    private void updatePlayPauseButtons() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            } else {
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}