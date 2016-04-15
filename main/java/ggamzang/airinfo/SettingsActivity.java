package ggamzang.airinfo;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

interface AirInfoSharedPreferenceChangeListener{
    void onAirInfoSharedPreferenceChanged(String key, String value);
}
// NOTE : PreferenceActivity 상속받으면 Crash 발생
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String KEY_PREF_IS_AUTOUPDATE = "pref_isAutoUpdate";
    public static final String KEY_PREF_UPDATE_HOUR = "pref_updateHour";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        Log.e(StaticData.TAG,"onREsume");
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
            Toast.makeText(getApplicationContext(), "auto:"+autoUpdate, Toast.LENGTH_LONG).show();
        }
        else if(key.equals(KEY_PREF_UPDATE_HOUR)){
            String hour = sharedPreferences.getString(key, "");
            value = hour;
            Toast.makeText(getApplicationContext(), "hour:"+hour, Toast.LENGTH_LONG).show();
        }
        AirInfoEventManager.getInstance().notifyPreferenceChanged(key, value);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}