package bop.provalayout;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    Global gbl = new Global();

    static String gps_min_distance_summary, gps_min_time_summary;
    static String gps_min_distance_key, gps_min_time_key;
    static String  gps_min_time_for_direction_key, gps_min_time_for_direction_summary;
    static String following_min_distance_summary, following_min_distance_key;
    static String following_default_interval_ring_key, following_default_interval_ring_summary;
    static String geoid_correction_summary, geoid_correction_key;
    static String pref_offlinemap_key, pref_offlinemap_summary;
    static String pref_offlinemap_zoom_max_key, pref_offlinemap_zoom_max_summary;
    static String pref_offlinemap_zoom_min_key, pref_offlinemap_zoom_min_summary;
    static String pref_def_key_zoom, pref_def_summary_zoom;


    private static Context mContext;

     private static void goToSettings() {
        try {
            ArrayList<String> ext = new ArrayList<>();
            ext.add("gpx");

            Intent intent = new Intent(mContext, FileChooser.class);
            intent.putStringArrayListExtra("extension", ext);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            //finish();
        } catch (Exception e) {
            Log.w("MY_CHECK", "ERRORE in goToSettings [" + e.toString() + "]");
        }
    }

    private static Preference.OnPreferenceClickListener pr_on_click_listener = new Preference.OnPreferenceClickListener(){

        @Override
        public boolean onPreferenceClick(Preference preference) {
            goToSettings();
            return true;
        }
    };

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            try {

                if (stringValue != null) {

                    String unit = "", s="";

                    if (preference.getKey().equals(gps_min_distance_key) ){
                        unit = "m";
                        s = gps_min_distance_summary;
                    }else if(preference.getKey().equals(gps_min_time_key)){
                        unit = "min";
                        s = gps_min_time_summary;
                    }else if(preference.getKey().equals(following_min_distance_key)){
                        unit = "m";
                        s = following_min_distance_summary;
                    }else if(preference.getKey().equals(geoid_correction_key)) {
                        unit = "m";
                        s = geoid_correction_summary;
                    }else if(preference.getKey().equals(following_default_interval_ring_key)) {
                        unit = "s";
                        s = following_default_interval_ring_summary;
                    }
                    else if(preference.getKey().equals(gps_min_time_for_direction_key)) {
                        unit = "s";
                        s = gps_min_time_for_direction_summary;
                    }
                    else if(preference.getKey().equals(pref_offlinemap_key)) {
                        unit = "";
                        s = pref_offlinemap_summary;
                    }
                    else if(preference.getKey().equals(pref_offlinemap_zoom_max_key)) {
                        unit = "x";
                        s = pref_offlinemap_zoom_max_summary;
                    }
                    else if(preference.getKey().equals(pref_offlinemap_zoom_min_key)) {
                        unit = "x";
                        s = pref_offlinemap_zoom_min_summary;
                    }
                    else if(preference.getKey().equals(pref_def_key_zoom)) {
                        unit = "";
                        s = pref_def_summary_zoom;
                    }
                    stringValue = s + ": " + stringValue + unit;

                    preference.setSummary(stringValue);
                }
            }catch(Exception e ){Log.w("MY_CHECK","Errore OnPreferenceChangeListener --> ["+e.toString()+"]");}

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    private static void bindPreferenceOnClick(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceClickListener(pr_on_click_listener);

    }

    private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();
        mContext = this;
        gps_min_distance_key  = getResources().getString(R.string.pref_gps_key_minum_distance);
        gps_min_time_key  = getResources().getString(R.string.pref_gps_key_minum_time);
        following_min_distance_key = getResources().getString(R.string.pref_following_key_minum_distance);
        geoid_correction_key = getResources().getString(R.string.pref_gps_key_geoid_correction);
        following_default_interval_ring_key = getResources().getString(R.string.pref_following_key_default_interval_ring);
        gps_min_time_for_direction_key = getResources().getString(R.string.pref_gps_key_minum_time_for_direction);
        pref_offlinemap_key = getResources().getString(R.string.pref_offlinemap_key);
        pref_offlinemap_zoom_max_key = getResources().getString(R.string.pref_offlinemap_zoom_max_key);
        pref_offlinemap_zoom_min_key = getResources().getString(R.string.pref_offlinemap_zoom_min_key);
        pref_def_key_zoom =getResources().getString(R.string.pref_def_key_zoom);

        gps_min_distance_summary  = getResources().getString(R.string.pref_gps_summary_minum_distance);
        gps_min_time_summary  = getResources().getString(R.string.pref_gps_summary_minum_time);
        following_min_distance_summary = getResources().getString(R.string.pref_following_summary_minum_distance);
        geoid_correction_summary = getResources().getString(R.string.pref_gps_summary_geoid_correction);
        following_default_interval_ring_summary = getResources().getString(R.string.pref_following_summary_default_interval_ring);
        gps_min_time_for_direction_summary = getResources().getString(R.string.pref_gps_summary_minum_time_for_direction);
        pref_offlinemap_summary = getResources().getString(R.string.pref_offlinemap_summary);
        pref_offlinemap_zoom_max_summary = getResources().getString(R.string.pref_offlinemap_zoom_max_summary);
        pref_offlinemap_zoom_min_summary = getResources().getString(R.string.pref_offlinemap_zoom_min_summary);
        pref_def_summary_zoom = getResources().getString(R.string.pref_def_summary_zoom);


    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Show the Up button in the action bar.
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //   actionBar.setBackgroundDrawable(getDrawable(R.drawable.img_toolbar));
            //}

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.col_background,null)));
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                //finish();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || DEFAULT_PreferenceFragment.class.getName().equals(fragmentName)
                || GPS_PreferenceFragment.class.getName().equals(fragmentName)
                || FacebookDataFragment.class.getName().equals(fragmentName)
                || TrackingPreferenceFragment.class.getName().equals(fragmentName)
                || OfflineMapsPreferenceFragment.class.getName().equals(fragmentName)
                || CreditsPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DEFAULT_PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_default_settings);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_def_key_zoom)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GPS_PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gps);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_gps_key_minum_distance)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_gps_key_minum_time_for_direction)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_gps_key_minum_time)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_gps_key_geoid_correction)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FacebookDataFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_facebook);
            setHasOptionsMenu(true);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TrackingPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_tracking);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_following_key_minum_distance)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_following_key_default_interval_ring)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OfflineMapsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_offline_map);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_offlinemap_key)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_offlinemap_zoom_max_key)));
            bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_offlinemap_zoom_min_key)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CreditsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_credits);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }
}
