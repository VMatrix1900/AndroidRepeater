package com.example.vincent.repeater;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player = null;
    private final IBinder mBinder = new MusicBinder();

    @Override
    public void onCreate() { // initiate the MediaPlayer instance
        player = new MediaPlayer();

        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        return false; // don't rebind
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test);
        try {
            player.setDataSource(getApplicationContext(), myUri);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        try {
            player.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        player.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
// shuffle or loop
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
