package com.example.vincent.repeater;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by Vincent on 1/4/15.
 * build music library activity
 */
public class LibraryActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private ListView songList;
    private SimpleCursorAdapter mAdapter;

    private String[] PROJ = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.BOOKMARK,
            MediaStore.Audio.AudioColumns.DURATION,
    };

    private String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " !=" + 0
            + " AND " + MediaStore.Audio.Media.DATA + " LIKE?";

    private String[] SELECTIONARGS = {""};

    private String[] fromColumns = {
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.BOOKMARK,
            MediaStore.Audio.AudioColumns.DURATION,
    };

    private int[] toViews = {
            R.id.title,
            R.id.lastTime,
            R.id.totalTime,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.music_lib, frameLayout);

        mDrawerList.setItemChecked(position, true);
        setTitle(navItems[position]);

        songList = (ListView) findViewById(R.id.music_lib);

        SELECTIONARGS[0] = Environment.getExternalStorageDirectory().getPath() + "/Repeater/%";


//        // Create a progress bar to display while the list loads
//        ProgressBar progressBar = new ProgressBar(this);
//        progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
//        progressBar.setIndeterminate(true);
//        songList.setEmptyView(progressBar);
//
//        // Must add the progress bar to the root of the layout
//        ViewGroup root = (ViewGroup) findViewById(R.id.music_lib);
//        root.addView(progressBar);

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.song, null,
                fromColumns, toViews, 0);

        mAdapter.setViewBinder(new CustomViewBinder());
        songList.setAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_library, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_rescan:
                mAdapter.notifyDataSetChanged();
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        menu.findItem(R.id.action_rescan).setVisible(!this.getDrawerOpen());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                PROJ, SELECTION, SELECTIONARGS, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)) {
                ((TextView) view).setText(Utilities.milliSecondsToTimer(cursor.getLong(columnIndex)));
                return true;
            }
            // For others, we simply return false so that the default binding
            // happens.
            return false;
        }

    }
}
