package com.techvipin130524.mp3player;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private ArrayList<String> songList = new ArrayList<>();
    private MusicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Edge-to-Edge Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // RecyclerView Setup
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check Permissions and Load Songs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+), request READ_MEDIA_AUDIO permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
            } else {
                getAllAudioFiles();
            }
        } else {
            // For Android 6.0+ (API 23+), request READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                getAllAudioFiles();
            }
        }
    }

    // Fetch All Audio Files Using MediaStore API
    public void getAllAudioFiles() {
        songList.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Query MediaStore for audio files
            String[] projection = {MediaStore.Audio.Media.DATA};
            try (Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null)) {

                if (cursor != null) {
                    int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    while (cursor.moveToNext()) {
                        String path = cursor.getString(dataIndex);
                        if (path != null && path.endsWith(".mp3")) {
                            songList.add(path);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error reading audio files: " + e.getMessage());
            }
        }

        // Initialize Adapter and Set to RecyclerView
        adapter = new MusicAdapter(songList, this);
        recyclerView.setAdapter(adapter);
        Log.d("MainActivity", "Songs loaded: " + songList.size());
    }

    // Handle Permission Results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getAllAudioFiles();
        } else {
            Log.e("MainActivity", "Permission denied.");
        }
    }
}
