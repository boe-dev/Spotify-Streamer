package de.boe_dev.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.jar.Manifest;

/**
 * Created by benny on 25.07.15.
 */
public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top_track_container) != null) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_track_container, new TopTrackFragment(), "DFTAG")
                        .commit();
            }
        } else {
            getSupportActionBar().setElevation(0f);
        }
    }
}
