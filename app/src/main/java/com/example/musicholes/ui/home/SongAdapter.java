package com.example.musicholes.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musicholes.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends ArrayAdapter<File> implements Filterable {
    private List<File> originalSongs;
    private List<File> filteredSongs;

    public SongAdapter(Context context, ArrayList<File> songs) {
        super(context, 0, songs);
        this.originalSongs = songs;
        this.filteredSongs = new ArrayList<>(songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_item, parent, false);
        }

        File song = getItem(position);
        TextView songName = convertView.findViewById(R.id.songName);
        ImageView songThumbnail = convertView.findViewById(R.id.songThumbnail);

        songName.setText(song.getName().replace(".mp3", ""));
        songThumbnail.setImageBitmap(getAlbumArt(song.getAbsolutePath()));

        return convertView;
    }

    private Bitmap getAlbumArt(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] art = mmr.getEmbeddedPicture();
        if (art != null) {
            return BitmapFactory.decodeByteArray(art, 0, art.length);
        }
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_thumbnail); // Default thumbnail if no art found
    }

    @Override
    public int getCount() {
        return filteredSongs.size();
    }

    @Override
    public File getItem(int position) {
        return filteredSongs.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                List<File> filteredList = new ArrayList<>();

                if (query.isEmpty()) {
                    filteredList.addAll(originalSongs);
                } else {
                    for (File song : originalSongs) {
                        if (song.getName().toLowerCase().contains(query)) {
                            filteredList.add(song);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredSongs.clear();
                filteredSongs.addAll((List<File>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}