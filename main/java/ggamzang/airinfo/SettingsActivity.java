package ggamzang.airinfo;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity  {// PreferenceActivity 쓰면 Crash 발생

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}