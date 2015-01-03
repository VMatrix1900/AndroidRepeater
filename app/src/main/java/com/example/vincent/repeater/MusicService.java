package com.example.vincent.repeater;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player = null;
    private final IBinder mBinder = new MusicBinder();
    private int duration;
    private String songTitle = "1.mp3";
    //    private AppStatus status;
    private SharedPreferences.Editor editor;

    private int pointA = 0;
    private int pointB = 0;
    private Handler ABRepeatHandler = new Handler();

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
        // get app status
//        status = (AppStatus) getApplicationContext();
        editor = ((AppStatus) getApplicationContext()).getSharedPreferences().edit();
        // initiate the MediaPlayer instance
        player = new MediaPlayer();

        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
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
        //update ui
        updateUI();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = player.getDuration();
        updateUI();
    }

    public void playPause() {
        if (this.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public void setPointA() {
        pointA = player.getCurrentPosition();
        // update app status
        editor.putInt(AppStatus.ABREPEAT_MODE, AppStatus.A_PRESSED);
        editor.commit();
    }

    public void setPointB() {
        pointB = player.getCurrentPosition();
        // start repeat;
        ABRepeatHandler.postDelayed(checkABRepeat, 100);
        // update app status
        editor.putInt(AppStatus.ABREPEAT_MODE, AppStatus.B_PRESSED);
        editor.commit();
    }

    private Runnable checkABRepeat = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                if (player.getCurrentPosition() > pointB) {
                    player.seekTo(pointA);
                }
            }
            ABRepeatHandler.postDelayed(checkABRepeat, 100);
        }
    };

    public void clearABRepeat() {
        ABRepeatHandler.removeCallbacks(checkABRepeat);
        pointA = 0;
        pointB = 0;
        // update app status
        editor.putInt(AppStatus.ABREPEAT_MODE, AppStatus.ABREPEAT_OFF);
        editor.commit();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int getProgress() {
        return player.getCurrentPosition();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void seekTo(int progress) {
        try {
            player.seekTo(progress);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        Intent updateIntent = new Intent("UPDATE_UI");
        // add data
        updateIntent.putExtra("Title", songTitle);
        updateIntent.putExtra("Duration", duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }
}
