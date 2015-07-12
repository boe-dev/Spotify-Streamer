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

    private ListView trackListView;
    private Toast mAppToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        trackListView = (ListView) findViewById(R.id.list_view_tracks);
        getSupportActionBar().setTitle(getString(R.string.top_10_tracks));
        if (NetworkFunctions.isNetworkOnline(getApplicationContext())) {
            new getArtistTopTracks().execute(getIntent().getStringExtra("artistId"));
        } else {
            errorMessage(R.string.no_connection);
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

    public class getArtistTopTracks extends AsyncTask<String, Void , Tracks> {

        @Override
        protected Tracks doInBackground(String... params) {
            if (params.length >= 1 && !params[0].equals("")) {
                Map<String, Object> options = new HashMap<>();

                try {
                    options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                    return new SpotifyApi().getService().getArtistTopTrack(params[0], options);
                } catch (NullPointerException e) {
                    Log.e(getClass().getName(), e.toString());
                    return null;
                }



            } else {
                this.cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            ArrayList<TopTrackModel> trackArrayAdapter = new ArrayList<TopTrackModel>();

            for (int i = 0; i < tracks.tracks.size(); i++) {

                if (tracks.tracks.get(i).album.images.size() >= 1) {
                    trackArrayAdapter.add(new TopTrackModel(
                            tracks.tracks.get(i).id.toString(),
                            tracks.tracks.get(i).album.images.get(0).url.toString(),
                            tracks.tracks.get(i).name,
                            tracks.tracks.get(i).album.name));
                }
            }

            ArrayAdapter<TopTrackModel> arrayAdapter = new TopTrackArrayAdapter(getApplicationContext(), trackArrayAdapter);
            trackListView.setAdapter(arrayAdapter);

        }
    }

    private void errorMessage(int errorRes) {
        ArrayList<String> cancelledMessage = new ArrayList<String>();
        cancelledMessage.add(getString(errorRes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_error_message, R.id.list_item_artist_textview, cancelledMessage);
        trackListView.setAdapter(arrayAdapter);
        if (mAppToast != null) {
            mAppToast.cancel();
        }
        mAppToast = Toast.makeText(getApplicationContext(), getString(errorRes), Toast.LENGTH_SHORT);
        mAppToast.show();
    }
}


