package com.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;

import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.service.MusicPlayerActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 100;
    private SongModel selectedSong;
    private List<SongModel> songList = new ArrayList<>();
    private SongAdapter adapter;
    private ActivityResultLauncher<Intent> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && selectedSong != null) {
                            Uri uri = data.getData();
                            if (uri != null) {

                                String localPath = copyImageToInternal(uri);
                                if (localPath != null) {
                                    selectedSong.setCoverUri(localPath);
                                    saveCoverUri(selectedSong);
                                    int position = songList.indexOf(selectedSong);
                                    if (adapter != null && position != -1)
                                        adapter.notifyItemChanged(position);
                                }
                            }
                        }
                    }
                }
        );

        boolean hasAudioPermission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasAudioPermission =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                            == PackageManager.PERMISSION_GRANTED;
        } else {
            hasAudioPermission =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
        }

        if (!hasAudioPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.READ_MEDIA_AUDIO },
                        REQUEST_PERMISSION
                );
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                        REQUEST_PERMISSION
                );
            }
            return;
        }

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.toString();
                // Create a temporary 1-song list
                SongModel singleSong = new SongModel("Unknown", "Unknown", path, 0);
                ArrayList<SongModel> tempList = new ArrayList<>();
                tempList.add(singleSong);

                Intent playerIntent = new Intent(this, MusicPlayerActivity.class);
                playerIntent.putExtra("songPath", path);
                playerIntent.putExtra("songIndex", 0);
                playerIntent.putExtra("songList", tempList);
                startActivity(playerIntent);
                return; // skip loading full list
            }
        }

        loadSongs();

    }

    private void loadSongs(){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";

        Cursor cursor = contentResolver.query(uri, projection, selection, null, null);

        if(cursor != null && cursor.moveToFirst()){
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                songList.add(new SongModel(title, artist, contentUri.toString(), duration));
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.d("MusicPlayer", "Number of songs found: " + songList.size());

        loadCoverUri();
        setupRecyclerView();
    }

    private void setupRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(songList, new SongAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(SongModel song) {
                // start MusicPlayerActivity
                Intent activityIntent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                activityIntent.putExtra("songPath", song.getPath());
                activityIntent.putExtra("songIndex", songList.indexOf(song));
                activityIntent.putExtra("songList", new ArrayList<>(songList));

                startActivity(activityIntent);

                Toast.makeText(getApplicationContext(), "Playing " + song.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSelectCover(SongModel song, int position) {
                selectedSong = song;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                pickImageLauncher.launch(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private String copyImageToInternal(Uri uri){
        try{
            InputStream inputStream = getContentResolver().openInputStream(uri);

            File dir = new File(getFilesDir(), "covers");
            if(!dir.exists()) dir.mkdir();

            String filename = System.currentTimeMillis() + ".jpg";
            File file = new File(dir, filename);

            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while (true){
                assert inputStream != null;
                if (!((length = inputStream.read(buffer)) > 0)) break;
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return file.getAbsolutePath();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void saveCoverUri(SongModel song){
        SharedPreferences prefs = getSharedPreferences("covers", MODE_PRIVATE);
        prefs.edit().putString(song.getPath(), song.getCoverUri()).apply();
    }

    private void loadCoverUri(){
        SharedPreferences prefs = getSharedPreferences("covers", MODE_PRIVATE);
        for(SongModel song : songList){
            String path = prefs.getString(song.getPath(), null);
            if(path != null) song.setCoverUri(path);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if(requestCode == REQUEST_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadSongs();
            } else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}