package com.example.vincent.repeater;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player = null;
    private final IBinder mBinder = new MusicBinder();
    private int duration;
    private String songTitle;
    private AppStatus status;
    private ArrayList<Integer> songIds;
    private int position;// current song's position in all songs
    private MediaMetadataRetriever retriever;
    private boolean startImmediately = false; // start play music immediately after player is prepared

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
        status = (AppStatus) getApplicationContext();
        // initiate the MediaPlayer instance
        player = new MediaPlayer();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            songIds = extras.getIntegerArrayList("ids");
            position = extras.getInt("index");
        }
        Uri myUri = intent.getData();
        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, myUri);

        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);

        prepareSong(myUri);

        return START_STICKY;
    }

    private void prepareSong(Uri uri) {
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        retriever.setDataSource(getApplicationContext(), uri);
        try {
            player.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        player.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        playNext();
        // update ui
        updateUI();
    }


    public void playPrevious() {
        // loop or use default song, no need to reset(), no need to refresh ui.
        if (status.getRepeatMode() == AppStatus.REPEAT_ON | null == songIds) {
            player.seekTo(0);
        } else { // repeat off and have a playlist
            player.reset();
            int nextId;
            if (status.getShuffleMode() == AppStatus.SHUFFLE_OFF) {
                if (position == 0) {
                    nextId = 0;
                } else {
                    nextId = songIds.get((position--) % songIds.size());
                }
            } else {
                nextId = new Random().nextInt(songIds.size());
            }
            Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + nextId);
            prepareSong(uri);
        }
    }

    public void playNext() {
        // loop or use default song, no need to reset(), no need to refresh ui.
        if (status.getRepeatMode() == AppStatus.REPEAT_ON | null == songIds) {
            player.seekTo(0);
        } else { // repeat off and have a playlist
            player.reset();
            int nextId;
            if (status.getShuffleMode() == AppStatus.SHUFFLE_OFF) {
                nextId = songIds.get((position++) % songIds.size());
            } else {
                nextId = new Random().nextInt(songIds.size());
            }
            Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + nextId);
            prepareSong(uri);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = player.getDuration();
        songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (startImmediately) {
            player.start();
        }
        updateUI();
    }

    public void playPause() {
        startImmediately = true;
        if (this.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public void setPointA() {
        pointA = player.getCurrentPosition();
        status.toggleABReapeatMode();
    }

    public void setPointB() {
        pointB = player.getCurrentPosition();
        // start repeat;
        ABRepeatHandler.postDelayed(checkABRepeat, 100);
        status.toggleABReapeatMode();
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
        status.toggleABReapeatMode();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int getProgress() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
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

    public void forward() {
        int progress = getProgress() + 3000;
        int duration = getDuration();
        player.seekTo(progress < duration ? progress : duration);
    }

    public void rewind() {
        int progress = getProgress() - 3000;
        player.seekTo(progress > 0 ? progress : 0);
    }

    private void updateUI() {
        Intent updateIntent = new Intent("UPDATE_UI");
        // add data
        updateIntent.putExtra("Title", songTitle);
        updateIntent.putExtra("Duration", duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }
}
