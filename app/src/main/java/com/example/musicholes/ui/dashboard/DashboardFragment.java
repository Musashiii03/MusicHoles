package com.example.musicholes.ui.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.musicholes.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardFragment extends Fragment {

    private static final int REQUEST_CODE = 100;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> folderList;
    private HashMap<String, List<String>> songMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        listView = view.findViewById(R.id.listView1);
        folderList = new ArrayList<>();
        songMap = new HashMap<>();

        // Check for permissions and load music folders if granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            loadMusicFolders();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedFolder = folderList.get(position);
                List<String> songs = songMap.get(selectedFolder);
                if (songs != null && !songs.isEmpty()) {
                    Intent intent = new Intent(getActivity(), SongListActivity.class);
                    intent.putStringArrayListExtra("songs", new ArrayList<>(songs));
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No songs found in this folder", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadMusicFolders() {
        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME, // The name of the audio file
                MediaStore.Audio.Media.DATA // Full path to the audio file
        };

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        )) {
            if (cursor != null) {
                int displayNameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(displayNameIndex);
                    String dataPath = cursor.getString(dataIndex);

                    // Ensure the data path is valid
                    if (dataPath != null && !dataPath.isEmpty()) {
                        String folderPath = new File(dataPath).getParent(); // Get the folder from the full path
                        String folderName = new File(folderPath).getName(); // Extract the folder name

                        if (!folderList.contains(folderName)) {
                            folderList.add(folderName);
                            songMap.put(folderName, new ArrayList<>());
                        }

                        // Add the full path to the songMap
                        songMap.get(folderName).add(dataPath);
                    }
                }
            }
        }

        // Initialize the adapter after loading folders
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, folderList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {  // Corrected this line
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusicFolders();
            } else {
                Toast.makeText(getActivity(), "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}