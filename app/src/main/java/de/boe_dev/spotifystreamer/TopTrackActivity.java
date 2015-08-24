package de.boe_dev.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by benny on 17.06.15.
 */
public class TopTrackActivity extends AppCompatActivity {

    private static final String NOW_PLAYING = "showNowPlaying";
    private boolean showNowPlaying = false;
    private boolean mBroadcastRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        getSupportActionBar().setTitle(getString(R.string.top_10_tracks));

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString("artistId", getIntent().getStringExtra("artistId"));
            TopTrackFragment fragment = new TopTrackFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_track_container, fragment)
                    .commit();
        } else {
            showNowPlaying = savedInstanceState.getBoolean(NOW_PLAYING);
        }
    }

    @Override
    public void onResume() {
        if (!mBroadcastRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter(MediaPlayerService.DATA));
            mBroadcastRegistered = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mBroadcastRegistered) {
            unregisterReceiver(broadcastReceiver);
            mBroadcastRegistered = false;
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showNowPlaying) {
            menu.add(0, 0, 0, "Now Playing").setIcon(android.R.drawable.ic_media_play).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            /*
             * This was needed that the back in ActionBar works like back button on the phone.
             * In other case savedInstanceState of MainActivityFragment would be null
             */
            case android.R.id.home:
                onBackPressed();
                return true;

            case 0:
                FragmentManager fragmentManager = getSupportFragmentManager();
                MediaPlayerDialog mediaPlayerDialog = new MediaPlayerDialog(this, null, 0);
                mediaPlayerDialog.show(fragmentManager, "fragment_edit_name");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NOW_PLAYING, showNowPlaying);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String dataValue = intent.getStringExtra(getString(R.string.data));
            int dataIntValue = Integer.parseInt(dataValue);
            if (dataIntValue == 5) {
                showNowPlaying = true;
            } else if (dataIntValue == 3) {
                showNowPlaying = false;
            }
            supportInvalidateOptionsMenu();
        }
    };


}


