package de.boe_dev.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

    private Toast mAppToast;
    private ListView trackListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.media_player_menu, menu);
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

            final ArrayList<TopTrackModel> trackArrayAdapter = new ArrayList<>();

            if (tracks.tracks.size() >= 1) {

                for (int i = 0; i < tracks.tracks.size(); i++) {

                    if (tracks.tracks.get(i).album.images.size() >= 1) {
                        trackArrayAdapter.add(new TopTrackModel(
                                tracks.tracks.get(i).id,
                                tracks.tracks.get(i).artists.get(0).name,
                                tracks.tracks.get(i).album.images.get(1).url,
                                tracks.tracks.get(i).name,
                                tracks.tracks.get(i).album.name,
                                tracks.tracks.get(i).preview_url));
                    }
                }

                final ArrayAdapter<TopTrackModel> arrayAdapter = new TopTrackArrayAdapter(getActivity(), trackArrayAdapter);
                trackListView.setAdapter(arrayAdapter);
                trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d("TopTrackFragment", arrayAdapter.getItem(position).getPreviewUrl());
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        MediaPlayerDialog mediaPlayerDialog = new MediaPlayerDialog(getActivity(), trackArrayAdapter, position);
                        mediaPlayerDialog.show(fragmentManager, "fragment_edit_name");





                    }
                });
            } else {
                errorMessage(R.string.no_tracks_found);
            }

        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            errorMessage(R.string.no_tracks_found);
        }
    }



    private void errorMessage(int errorRes) {
        ArrayList<String> cancelledMessage = new ArrayList<>();
        cancelledMessage.add(getString(errorRes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_error_message, R.id.list_item_artist_textview, cancelledMessage);
        trackListView.setAdapter(arrayAdapter);
        if (mAppToast != null) {
            mAppToast.cancel();
        }
        mAppToast = Toast.makeText(getActivity(), getString(errorRes), Toast.LENGTH_SHORT);
        mAppToast.show();
    }
}
