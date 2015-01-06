package com.example.vincent.repeater;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


public class PlayingActivity extends BaseActivity implements EditDialogFragment.NoticeDialogListener {

    private ImageButton playButton;
    private SeekBar mSeekBar;
    private TextView currentTimeLabel;
    private TextView totalTimeLabel;
    private TextView songNameLabel;
    private TextView ABRepeatButton;
    private ListView tagsList;
    private ImageButton loopButton;
    private ImageButton shuffleButton;
    private ImageButton ffButton;
    private ImageButton rwButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private TagAdapter tagAdapter;

    private MusicService mSrv = null;
    private boolean mBound = false;
    private Intent mIntent;
    // update seekBar and play time etc.
    private final Handler uiHandler = new Handler();
    private AppStatus status;

    private SpannableString text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.playing_layout, frameLayout);

        mDrawerList.setItemChecked(position, true);
        setTitle(navItems[position]);

        // get application
        status = (AppStatus) getApplication();
        // bind service
        mIntent = new Intent(this, MusicService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        // also start service, keep running in the background
        startService(mIntent);
        // get view
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

        ABRepeatButton = (TextView) findViewById(R.id.ABRepeat);
        text = new SpannableString("A \u2194 B");
        renderABRepeatButton(status.getABRepeatMode());
        ABRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ABRepeat();
            }
        });

        loopButton = (ImageButton) findViewById(R.id.loop);
        renderLoopButton();
        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.toggleReapeatMode();
                renderLoopButton();
            }
        });

        shuffleButton = (ImageButton) findViewById(R.id.shuffle);
        renderShuffleButton();
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.toggleShuffleMode();
                renderShuffleButton();
            }
        });

        prevButton = (ImageButton) findViewById(R.id.previousList);
        rwButton = (ImageButton) findViewById(R.id.previousSong);
        ffButton = (ImageButton) findViewById(R.id.nextSong);
        nextButton = (ImageButton) findViewById(R.id.nextList);

        rwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewind();
            }
        });

        ffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward();
            }
        });


        tagsList = (ListView) findViewById(R.id.tags);

        tagAdapter = new TagAdapter(this);

        tagsList.setAdapter(tagAdapter);

        tagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSrv.seekTo(((Tag) tagAdapter.getItem(position)).getStartTime());
            }
        });

        tagsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        tagAdapter.removeTags(tagsList.getCheckedItemPositions());

                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_edit:
                        EditDialogFragment editFragment = new EditDialogFragment();
                        editFragment.show(getFragmentManager(), "editor");
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextmenu_play, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });

    }

    private void forward() {
        // only work when playing
        if (mSrv.isPlaying()) {
            mSrv.forward();
        }
    }

    private void rewind() {
        // only work when playing
        if (mSrv.isPlaying()) {
            mSrv.rewind();
        }
    }

    private void renderLoopButton() {
        switch (status.getRepeatMode()) {
            case AppStatus.REPEAT_ON:
                loopButton.setImageResource(R.drawable.matte_repeat_song);
                break;
            case AppStatus.REPEAT_OFF:
                loopButton.setImageResource(R.drawable.matte_perm_repeat_song);
                break;
        }
    }

    private void renderShuffleButton() {
        switch (status.getShuffleMode()) {
            case AppStatus.SHUFFLE_ON:
                shuffleButton.setImageResource(R.drawable.matte_shuffle_all);
                break;
            case AppStatus.SHUFFLE_OFF:
                shuffleButton.setImageResource(R.drawable.matte_perm_shuffle_all);
                break;
        }
    }

    private void renderPlayButton() {
        if (mSrv == null) {
            playButton.setImageResource(R.drawable.play_big);
        } else {
            if (mSrv.isPlaying()) {
                playButton.setImageResource(R.drawable.pause_big);
            } else {
                playButton.setImageResource(R.drawable.play_big);
            }
        }
    }

    private void renderABRepeatButton(int ABRepeatMode) {
        switch (ABRepeatMode) {
            case AppStatus.A_PRESSED:
                text.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, 0);
                ABRepeatButton.setText(text, TextView.BufferType.SPANNABLE);
                break;
            case AppStatus.B_PRESSED:
                text.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, 0);
                text.setSpan(new ForegroundColorSpan(Color.RED), 4, 5, 0);
                ABRepeatButton.setText(text, TextView.BufferType.SPANNABLE);
                break;
            case AppStatus.ABREPEAT_OFF:
                ABRepeatButton.setText(R.string.repeatText);
                break;
        }
    }

    private void ABRepeat() {
        switch (status.getABRepeatMode()) {
            case AppStatus.ABREPEAT_OFF:
                renderABRepeatButton(AppStatus.A_PRESSED);
                // set repeat start point
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        mSrv.setPointA();
                        return null;
                    }
                }.execute();
                break;
            case AppStatus.A_PRESSED:
                renderABRepeatButton(AppStatus.B_PRESSED);
                // set repeat end point
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        mSrv.setPointB();
                        return null;
                    }
                }.execute();
                break;
            case AppStatus.B_PRESSED:
                renderABRepeatButton(AppStatus.ABREPEAT_OFF);
                // clear repeat range
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        mSrv.clearABRepeat();
                        return null;
                    }
                }.execute();
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_tag:
                int startTime = 0;
                int endTime = mSrv.getDuration();
                int progress = mSrv.getProgress();
                if (progress > 2500) {
                    startTime = progress - 2500;
                }
                if (progress < endTime - 2500) {
                    endTime = progress + 2500;
                }
                tagAdapter.addTag(startTime, endTime, "default");
                tagAdapter.notifyDataSetChanged();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        menu.findItem(R.id.action_add_tag).setVisible(!this.getDrawerOpen());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        // local broadcast manager to update ui(textView, seekBar max etc)
        LocalBroadcastManager.getInstance(this).registerReceiver(updateUI,
                new IntentFilter("UPDATE_UI"));
        // play state may change, rerender the button image.
        renderPlayButton();

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
        uiHandler.postDelayed(updateSeekBar, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(updateSeekBar);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            currentTimeLabel.setText(Utilities.milliSecondsToTimer(progress));
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
            if (mSrv != null) {
                int progress = mSrv.getProgress();
                mSeekBar.setProgress(progress);

                uiHandler.postDelayed(this, 500);
            } else uiHandler.postDelayed(this, 500);
        }
    };

    private BroadcastReceiver updateUI = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String songTitle = intent.getStringExtra("Title");
            int total = intent.getIntExtra("Duration", 10000);
//            Log.d("receiver", "receive" + songTitle + total);
            mSeekBar.setMax(total);
            songNameLabel.setText(songTitle);
            totalTimeLabel.setText(Utilities.milliSecondsToTimer(total));
        }
    };

    private void playPause() {
        if (mSrv.isPlaying()) {
            uiHandler.removeCallbacks(updateSeekBar);
            playButton.setImageResource(R.drawable.play_big);
        } else {
            uiHandler.postDelayed(updateSeekBar, 100);
            playButton.setImageResource(R.drawable.pause_big);
        }

        // playstate toggle in the background.
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                mSrv.playPause();
                return null;
            }
        }.execute();

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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String description) {
        // update description
        tagAdapter.changeAllTagDescription(tagsList.getCheckedItemPositions(), description);

    }
}
