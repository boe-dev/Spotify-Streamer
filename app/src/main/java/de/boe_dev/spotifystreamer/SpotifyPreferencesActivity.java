package de.boe_dev.spotifystreamer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by ben on 24.08.15.
 */
public class SpotifyPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
