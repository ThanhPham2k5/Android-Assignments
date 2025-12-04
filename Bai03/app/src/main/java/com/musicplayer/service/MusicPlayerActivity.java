package com.musicplayer.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.musicplayer.R;
import com.musicplayer.SongModel;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {

    private TextView durationText, titleText;
    private SeekBar seekBar;
    private ImageButton previousBtn, playBtn, nextBtn, stopBtn;
    private ImageView coverImg;
    private MusicService musicService;
    private boolean bound = false;
    private boolean receiverRegistered = false;
    private int currentIndex = 0;

    @SuppressLint("DefaultLocale")
    private String formatDuration(long duration) {
        int minutes = (int) (duration / 1000) / 60;
        int seconds = (int) (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
            musicService.requestUIUpdate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    private final BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_UPDATE_UI.equals(intent.getAction())) {

                // Update current index
                int newIndex = intent.getIntExtra("currentIndex", -1);
                if(newIndex != currentIndex) currentIndex = newIndex;

                // Update title
                String title = intent.getStringExtra("title");
                if (title != null) titleText.setText(title);

                //Update cover
                String coverUri = intent.getStringExtra("cover");
                Glide.with(coverImg.getContext())
                        .load(coverUri)
                        .placeholder(R.drawable.ic_music_note)
                        .into(coverImg);


                int duration = intent.getIntExtra("duration", 0);
                if(duration > 0){
                    seekBar.setMax(duration); // Update SeekBar max
                    durationText.setTag(formatDuration(duration));// Update total duration
                }

                // Update play button
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                if (isPlaying) {
                    playBtn.setImageResource(R.drawable.pause_button);
                } else {
                    playBtn.setImageResource(R.drawable.play_1000_svgrepo_com);
                }

                // Update seekbar progress
                int currentPos = intent.getIntExtra("currentPosition", 0);
                seekBar.setProgress(currentPos);
                String current = formatDuration(currentPos);
                String totalStr = (String) durationText.getTag();
                durationText.setText(current + " / " + totalStr);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        coverImg = findViewById(R.id.coverImg);
        titleText = findViewById(R.id.titleText);
        durationText = findViewById(R.id.durationText);
        seekBar = findViewById(R.id.songSeekBar);
        previousBtn = findViewById(R.id.previousMusicBtn);
        playBtn = findViewById(R.id.playMusicBtn);
        nextBtn = findViewById(R.id.nextMusicBtn);
        stopBtn = findViewById(R.id.stopMusicBtn);

        //get data from Intent
        Intent intent = getIntent();
        if(intent == null) return;

        String action = intent.getAction();
        Uri uri = intent.getData();


        // CASE 1: Open from File Manager
        if (Intent.ACTION_VIEW.equals(action) && uri != null) {
            String path = uri.toString();

            String title = null;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, uri);

                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (title == null) {
                    title = cleanTitle(uri.getLastPathSegment());
                }

                retriever.release();
            } catch (Exception e) {
                title = uri.getLastPathSegment();
            }

            // create a single-song list
            ArrayList<SongModel> oneSong = new ArrayList<>();
            oneSong.add(new SongModel(title, "Unknown", path, 0));

            // start foreground service
            Intent serviceIntent = new Intent(this, MusicService.class);
            serviceIntent.putExtra("songPath", path);
            serviceIntent.putExtra("songIndex", 0);
            serviceIntent.putExtra("songList", oneSong);

            startForegroundService(serviceIntent);

            setupButtons();
            setupSeekBar();
            return;
        }


        // CASE 2: Open from MainActivity (playlist)
        String songPath = intent.getStringExtra("songPath");
        this.currentIndex = intent.getIntExtra("songIndex", 0);
        List<SongModel> songList = intent.getParcelableArrayListExtra("songList");

        if (songPath == null || songPath.isEmpty()) return;
        if (songList == null || songList.isEmpty()) return;

        //start Foreground Service
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("songPath", songPath);
        serviceIntent.putExtra("songIndex", currentIndex);
        serviceIntent.putExtra("songList", new ArrayList<>(songList));

        startForegroundService(serviceIntent);


        setupButtons();
        setupSeekBar();
    }

    private String cleanTitle(String raw) {
        if (raw == null) return "";

        // get text behind "/"
        int slashIndex = raw.lastIndexOf("/");
        if (slashIndex != -1) {
            raw = raw.substring(slashIndex + 1);
        }

        // remove extension
        int dotIndex = raw.lastIndexOf(".");
        if (dotIndex != -1) {
            raw = raw.substring(0, dotIndex);
        }

        return raw;
    }

    private void setupButtons() {
        playBtn.setOnClickListener(v -> {
            if (bound && musicService != null) musicService.togglePlayPause();
        });
        previousBtn.setOnClickListener(v -> {
            if (bound && musicService != null) musicService.playPreviousSong();
        });
        nextBtn.setOnClickListener(v -> {
            if (bound && musicService != null) musicService.playNextSong();
        });
        stopBtn.setOnClickListener(v -> {
            if (bound && musicService != null) {
                musicService.stopMusic();
                finish(); // go back to main activity
            }
        });
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && bound && musicService != null) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.ACTION_UPDATE_UI);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0;
        registerReceiver(musicReceiver, filter, flags);
        receiverRegistered = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }

        if(isFinishing()){
            if(receiverRegistered){
                try{
                    unregisterReceiver(musicReceiver);
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                receiverRegistered = false;
            }
            stopService(new Intent(this, MusicService.class));
        }

    }
}