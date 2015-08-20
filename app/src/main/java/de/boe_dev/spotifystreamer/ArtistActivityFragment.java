package de.boe_dev.spotifystreamer;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

import de.boe_dev.spotifystreamer.functions.NetworkFunctions;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistActivityFragment extends Fragment {

    private static final String ARTIST = "artistName";
    private static final String SELECTED_KEY = "selected_position";
    private Toast mAppToast;
    private int mPosition = ListView.INVALID_POSITION;

    private static ListView artistListView;
    private EditText searchArtistEditText;



    public ArtistActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);
        artistListView = (ListView) rootView.findViewById(R.id.list_view_artist);
        searchArtistEditText = (EditText) rootView.findViewById(R.id.edit_text_search_artist);

        searchArtistEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (NetworkFunctions.isNetworkOnline(getActivity())) {
                    new searchArtistIdTask().execute(v.getText().toString());
                } else {
                    errorMessage(R.string.no_connection);
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!NetworkFunctions.isNetworkOnline(getActivity())) {
            errorMessage(R.string.no_connection);
        } else {
            if (savedInstanceState != null) {
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
                new searchArtistIdTask().execute(savedInstanceState.getString(ARTIST));
                Log.d(ARTIST,"" + savedInstanceState.getString(ARTIST));
            }
        }

        Intent player = new Intent(getActivity().getApplicationContext(), MediaPlayerService.class);
        getActivity().getApplicationContext().startService(player);


    }

    public class searchArtistIdTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {

            SpotifyService spotify = new SpotifyApi().getService();
            if (params.length >= 1 && !params[0].equals("")) {
                return spotify.searchArtists(params[0]);
            } else {
                this.cancel(true);
                return null;
            }


        }


        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            ArrayList<ArtistModel> artistIdArrayAdapter = new ArrayList<>();

            if (artistsPager.artists.items.size() >= 1) {

                for (int i = 0; i < artistsPager.artists.items.size(); i++) {

                    if (artistsPager.artists.items.get(i).images.size() >= 1) {
                        artistIdArrayAdapter.add(new ArtistModel(artistsPager.artists.items.get(i).id,
                                artistsPager.artists.items.get(i).images.get(0).url,
                                artistsPager.artists.items.get(i).name ));
                    } else {
                        artistIdArrayAdapter.add(new ArtistModel(artistsPager.artists.items.get(i).id,
                                "",
                                artistsPager.artists.items.get(i).name ));
                    }
                }

                final ArrayAdapter<ArtistModel> arrayAdapter = new ArtistArrayAdapter(getActivity(), artistIdArrayAdapter);
                artistListView.setAdapter(arrayAdapter);
                if (mPosition != ListView.INVALID_POSITION) {
                    artistListView.setSelection(mPosition);
                }

                artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mPosition = position;
                        if (getActivity().findViewById(R.id.top_track_container) != null) {
                            Bundle args = new Bundle();
                            args.putString("artistId", arrayAdapter.getItem(position).getId());
                            TopTrackFragment fragment = new TopTrackFragment();
                            fragment.setArguments(args);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.top_track_container, fragment, "DFTAG")
                                    .commit();
                        } else {
                            Intent title = new Intent(getActivity(), TopTrackActivity.class);
                            title.putExtra("artistId", arrayAdapter.getItem(position).getId());
                            getActivity().startActivity(title);
                        }
                    }
                });

            } else {
                errorMessage(R.string.no_artist_found);
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            errorMessage(R.string.no_artist_found);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARTIST, searchArtistEditText.getText().toString());
        if(mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

    }

    private void errorMessage(int errorRes) {
        ArrayList<String> cancelledMessage = new ArrayList<>();
        cancelledMessage.add(getString(errorRes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_error_message, R.id.list_item_artist_textview, cancelledMessage);
        artistListView.setAdapter(arrayAdapter);
        if (mAppToast != null) {
            mAppToast.cancel();
        }
        mAppToast = Toast.makeText(getActivity(), getString(errorRes), Toast.LENGTH_SHORT);
        mAppToast.show();
    }
}
