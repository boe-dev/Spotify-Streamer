package de.boe_dev.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        }

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
        }
        return super.onOptionsItemSelected(item);
    }


}


