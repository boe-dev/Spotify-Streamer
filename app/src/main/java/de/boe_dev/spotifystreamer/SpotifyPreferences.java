package de.boe_dev.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * Created by ben on 24.08.15.
 */
public class SpotifyPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private boolean notification = true;
    private boolean country_code = false;
    private String countryCodeString = Locale.getDefault().getCountry();
    public static SharedPreferences sharedPreferences;
    private static SpotifyPreferences instance;

    public static SpotifyPreferences getInstance(final Context context) {
        return instance == null ? (instance = new SpotifyPreferences(context)) : instance;
    }

    private SpotifyPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        reload();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        reload();
    }

    private void reload() {
        notification = sharedPreferences.getBoolean("notification", true);
        country_code = sharedPreferences.getBoolean("country_code", false);
        countryCodeString = sharedPreferences.getString("own_country_code", Locale.getDefault().getCountry());
    }

    public boolean isNotification() {
        return notification;
    }

    public boolean isCountry_code() {
        return country_code;
    }

    public String getCountryCodeString() {
        return countryCodeString;
    }
}
