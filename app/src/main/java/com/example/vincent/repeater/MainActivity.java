package com.example.vincent.repeater;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    private ImageButton playButton;
    private SeekBar mSeekBar;
    private TextView currentTimeLabel;
    private TextView totalTimeLabel;
    private TextView songNameLabel;

    private MusicService mSrv;
    private boolean mBound = false;
    private Intent mIntent;
    // update seekBar and play time etc.
    private final Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIntent = new Intent(this, MusicService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        // also start service, keep running in the background
        startService(mIntent);

        playButton = (ImageButton) findViewById(R.id.play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        currentTimeLabel = (TextView) findViewById(R.id.current);
        totalTimeLabel = (TextView) findViewById(R.id.total);
        songNameLabel = (TextView) findViewById(R.id.title);

    }

    @Override
    protected void onStart() {
        // local broadcast manager to update ui(textView, seekBar max etc)
        LocalBroadcastManager.getInstance(this).registerReceiver(updateUI,
                new IntentFilter("UPDATE_UI"));

        // TODO start one broadcast immediately


        super.onStart();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateUI);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.postDelayed(updateSeekBar, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(updateSeekBar);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long min = TimeUnit.MILLISECONDS.toMinutes(progress);
            currentTimeLabel.setText(String.format("%d:%d",
                    min, TimeUnit.MILLISECONDS.toSeconds(progress) -
                            TimeUnit.MINUTES.toSeconds(min)));
            if (fromUser) {
                mSrv.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            uiHandler.removeCallbacks(updateSeekBar);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            uiHandler.postDelayed(updateSeekBar, 100);
        }
    };

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            int progress = mSrv.getProgress();
            mSeekBar.setProgress(progress);

            uiHandler.postDelayed(this, 500);
        }
    };

    private BroadcastReceiver updateUI = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String songTitle = intent.getStringExtra("Title");
            int total = intent.getIntExtra("Duration", 10000);
            Log.d("receiver", "receive" + songTitle + total);
            mSeekBar.setMax(total);
            songNameLabel.setText(songTitle);
            long min = TimeUnit.MILLISECONDS.toMinutes(total);
            totalTimeLabel.setText(String.format("%d:%d",
                    min, TimeUnit.MILLISECONDS.toSeconds(total) -
                            TimeUnit.MINUTES.toSeconds(min)));
        }
    };

    private void playPause() {
        mSrv.playPause();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            mSrv = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // unbind from service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        // stop service
        stopService(mIntent);
    }
}
