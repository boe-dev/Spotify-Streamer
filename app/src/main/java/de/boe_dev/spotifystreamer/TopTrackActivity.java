package de.boe_dev.spotifystreamer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.boe_dev.spotifystreamer.functions.NetworkFunctions;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by benny on 17.06.15.
 */
public class TopTrackActivity extends AppCompatActivity {

//    private ListView trackListView;
//    private Toast mAppToast;

    private boolean showNowPlaying = false;
    private boolean mBufferBroadcasRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        // trackListView = (ListView) findViewById(R.id.list_view_tracks);
        getSupportActionBar().setTitle(getString(R.string.top_10_tracks));

        if (savedInstanceState == null) {
            Log.d("TopTrackActivity", "artistId = " + getIntent().getStringExtra("artistId"));
            Bundle args = new Bundle();
            args.putString("artistId", getIntent().getStringExtra("artistId"));
            TopTrackFragment fragment = new TopTrackFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_track_container, fragment)
                    .commit();
        } else {
            showNowPlaying = savedInstanceState.getBoolean("showNowPlaying");
        }

    }

    @Override
    public void onResume() {
        if (!mBufferBroadcasRegistered) {
            registerReceiver(broadcastBufferReceiver, new IntentFilter(MediaPlayerService.BUFFER));
            mBufferBroadcasRegistered = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mBufferBroadcasRegistered) {
            unregisterReceiver(broadcastBufferReceiver);
            mBufferBroadcasRegistered = false;
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
        outState.putBoolean("showNowPlaying", showNowPlaying);
    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String bufferValue = intent.getStringExtra("buffering");
            int bufferIntValue = Integer.parseInt(bufferValue);
            if (bufferIntValue == 5) {
                showNowPlaying = true;

            } else if (bufferIntValue == 3) {
                showNowPlaying = false;
            }

            supportInvalidateOptionsMenu();

        }
    };


}


