package de.boe_dev.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by benny on 25.07.15.
 */
public class TopTrackFragment extends Fragment {

    private ListView trackListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        trackListView = (ListView) rootView.findViewById(R.id.list_view_tracks);

        Bundle arguments = getArguments();
        if (arguments != null) {
            new getArtistTopTracks().execute(arguments.getString("artistId"));
        }

        return rootView;
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

            ArrayList<TopTrackModel> trackArrayAdapter = new ArrayList<>();

            for (int i = 0; i < tracks.tracks.size(); i++) {

                if (tracks.tracks.get(i).album.images.size() >= 1) {
                    trackArrayAdapter.add(new TopTrackModel(
                            tracks.tracks.get(i).id,
                            tracks.tracks.get(i).album.images.get(0).url,
                            tracks.tracks.get(i).name,
                            tracks.tracks.get(i).album.name,
                            tracks.tracks.get(i).preview_url));
                }
            }

            ArrayAdapter<TopTrackModel> arrayAdapter = new TopTrackArrayAdapter(getActivity(), trackArrayAdapter);
            trackListView.setAdapter(arrayAdapter);

        }
    }
}
