package com.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.musicplayer.MusicPlayerActivity;
import com.musicplayer.R;
import com.musicplayer.SongModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;

    private List<SongModel> songList = new ArrayList<>();

    private int currentIndex = -1;

    private boolean isPlaying;

    private boolean isForeground = false;

    public static final String ACTION_UPDATE_UI = "ACTION_UPDATE_UI";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            sendUIUpdate();
            if (isPlaying) {
                handler.postDelayed(this, 500);
            }
        }
    };

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_PAUSE:
                    togglePlayPause();
                    break;
                case ACTION_NEXT:
                    playNextSong();
                    break;
                case ACTION_PREVIOUS:
                    playPreviousSong();
                    break;
            }
            return START_NOT_STICKY;
        }
        if (intent != null && intent.hasExtra("songPath")) {

            isPlaying = false;

            String songPath = intent.getStringExtra("songPath");
            currentIndex = intent.getIntExtra("songIndex", 0);
            songList = intent.getParcelableArrayListExtra("songList");
            if (songPath != null) {
                playSong(songPath);
            }
        }
        return START_STICKY;
    }

    public void playPreviousSong(){
        if(mediaPlayer == null) return;
        if(songList == null || songList.isEmpty()) return;

        currentIndex--;
        if(currentIndex < 0) currentIndex = songList.size()-1;

        playSong(songList.get(currentIndex).getPath());
        updateNotification();
    }
    public void playNextSong(){
        if(mediaPlayer == null) return;
        if(songList == null || songList.isEmpty()) return;

        currentIndex++;
        if(currentIndex >= songList.size()) currentIndex = 0;

        playSong(songList.get(currentIndex).getPath());
        updateNotification();
    }

    public void togglePlayPause(){
        if(mediaPlayer == null) return;
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPlaying = false;
            handler.removeCallbacks(updateRunnable);
        } else{
            mediaPlayer.start();
            isPlaying = true;
            handler.postDelayed(updateRunnable, 0);
        }
        updateNotification();
        sendUIUpdate();
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        handler.removeCallbacks(updateRunnable);

        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
        isForeground = false;
    }
    public void requestUIUpdate() {
        sendUIUpdate();
    }

    private void playSong(String songPath){
        try {
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, Uri.parse(songPath));
            handler.removeCallbacks(updateRunnable);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                sendUIUpdate();
                handler.postDelayed(updateRunnable, 500);

                if (!isForeground) {
                    startForeground(1, createNotification());
                    isForeground = true;
                }
            });

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private Notification createNotification(){
        String channelId = "music_player_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId, "Music Player", NotificationManager.IMPORTANCE_LOW);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        int playPauseIcon = isPlaying ? R.drawable.pause_button : R.drawable.play_button;
        String playPauseText = isPlaying ? "Pause" : "Play";

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        PendingIntent playPausePI = PendingIntent.getService(this, 0,
                new Intent(this, MusicService.class).setAction(ACTION_PLAY_PAUSE), flags);

        PendingIntent nextPI = PendingIntent.getService(this, 1,
                new Intent(this, MusicService.class).setAction(ACTION_NEXT), flags);

        PendingIntent prevPI = PendingIntent.getService(this, 2,
                new Intent(this, MusicService.class).setAction(ACTION_PREVIOUS), flags);


        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 4, new Intent(this, MusicPlayerActivity.class),
                PendingIntent.FLAG_IMMUTABLE);


        String title = currentIndex >= 0 && currentIndex < songList.size()
                ? songList.get(currentIndex).getTitle()
                : "Unknown";
        String artist = currentIndex >= 0 && currentIndex < songList.size()
                ? songList.get(currentIndex).getArtist()
                : "Unknown";

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.previous_button, "Previous", prevPI)
                .addAction(playPauseIcon, playPauseText, playPausePI)
                .addAction(R.drawable.next_button, "Next", nextPI)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setOngoing(true)
                .build();
    }

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            sendUIUpdate();
        }
    }

    private void sendUIUpdate() {
        if (songList == null || songList.isEmpty() || currentIndex < 0 || currentIndex >= songList.size()) return;

        SongModel currentSong = songList.get(currentIndex);

        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.setPackage(getPackageName());
        intent.putExtra("isPlaying", isPlaying);
        intent.putExtra("currentIndex", currentIndex);
        intent.putExtra("currentPosition", getCurrentPosition());
        intent.putExtra("duration", mediaPlayer != null ? mediaPlayer.getDuration() : 0);
        intent.putExtra("title", currentSong.getTitle());
        intent.putExtra("cover", currentSong.getCoverUri());
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        handler.removeCallbacks(updateRunnable);
    }
}
