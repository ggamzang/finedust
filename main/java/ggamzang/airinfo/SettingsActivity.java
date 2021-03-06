package ggamzang.airinfo;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

interface AirInfoSharedPreferenceChangeListener{
    void onAirInfoSharedPreferenceChanged(String key, String value);
}
// NOTE : PreferenceActivity 상속받으면 Crash 발생
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String KEY_PREF_IS_AUTOUPDATE = "pref_isAutoUpdate";
    public static final String KEY_PREF_UPDATE_HOUR = "pref_updateHour";

    protected static ListPreference listPref = null;
    protected static CheckBoxPreference checkPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // listPref is set in onResume. not set in onCreate
        if(listPref != null) {
            String hour = sharedPref.getString(KEY_PREF_UPDATE_HOUR, "");
            listPref.setSummary(hour + "시간");
        }
        else{
            Log.e(StaticData.TAG, "listPref is null");
        }

        if(checkPref != null){
            String station = sharedPref.getString(StaticData.PREF_STATION_KEY, "");
            if(station.length() > 0){
                checkPref.setEnabled(true);
            }
            else{
                checkPref.setEnabled(false);
            }
        }

        Log.e(StaticData.TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        Log.e(StaticData.TAG, "onPause");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value = "error";
        if(key.equals(KEY_PREF_IS_AUTOUPDATE)){
            Boolean autoUpdate = sharedPreferences.getBoolean(key, false);
            value = autoUpdate.toString();
        }
        else if(key.equals(KEY_PREF_UPDATE_HOUR)){
            String hour = sharedPreferences.getString(key, "");
            value = hour;
            if(listPref != null) {
                listPref.setSummary(hour + "시간");
            }
        }
        else if(key.equals(StaticData.PREF_STATION_KEY)){
            if(checkPref != null){
                String station = sharedPreferences.getString(StaticData.PREF_STATION_KEY, "");
                if(station.length() > 0){
                    checkPref.setEnabled(true);
                }
                else{
                    checkPref.setEnabled(false);
                }
            }
        }
        AirInfoEventManager.getInstance().notifyPreferenceChanged(key, value);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(StaticData.TAG, "SettingsFragment - onCreate");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            listPref = (ListPreference)findPreference(KEY_PREF_UPDATE_HOUR);
            checkPref = (CheckBoxPreference)findPreference(KEY_PREF_IS_AUTOUPDATE);
        }
    }
}