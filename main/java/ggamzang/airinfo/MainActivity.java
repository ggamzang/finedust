package ggamzang.airinfo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements AirInfoSharedPreferenceChangeListener{
    private TextView mTVSelected            = null;
    private Button mBTNgetInfo              = null;

    private SharedPreferences mPref = null;

    private static final int RESULT_SEARCH  =   1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.inflateMenu(R.menu.toolbar_item);

        mTVSelected = (TextView)findViewById(R.id.tvSelectedStationName);
        mBTNgetInfo = (Button)findViewById(R.id.btnGetInfo);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(mTVSelected != null){
            mTVSelected.setText(mPref.getString(StaticData.PREF_STATION_KEY, ""));
        }

        if(mBTNgetInfo != null){
            mBTNgetInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTVSelected.getText().length() > 0) {
                        AirInfoTask airInfoTask = new AirInfoTask();
                        if (airInfoTask != null)
                            airInfoTask.execute(mTVSelected.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "지역을 입력하세요", Toast.LENGTH_LONG);
                    }
                }
            });
        }

        AirInfoEventManager.getInstance().addPreferenceListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case RESULT_SEARCH:
            {
                if(resultCode == RESULT_OK)
                {
                    Bundle bundle = data.getExtras();
                    String stationName = bundle.getString(SearchActivity.EXTRA_SEARCH_STATIONNAME);
                    if(mTVSelected != null){
                        mTVSelected.setText(stationName);
                    }
                    Intent serviceIntent = new Intent(MainActivity.this, DustService.class);
                    stopService(serviceIntent);
                    RestartAlarm(mPref.getString(SettingsActivity.KEY_PREF_UPDATE_HOUR, StaticData.DEFAULT_UPDATEHOUR));
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void RestartAlarm(String hour){
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, DustService.class), 0);

        am.cancel(alarmIntent);

        long updateHour = (long)Integer.parseInt(hour);
        long firstTime = SystemClock.elapsedRealtime();
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, updateHour * AlarmManager.INTERVAL_HOUR, alarmIntent);
    }

    private void CancelAlarm(){
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, DustService.class), 0);
        am.cancel(alarmIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search: {
                Toast.makeText(getApplicationContext(), "Search tapped", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SearchActivity.class);
                startActivityForResult(intent, RESULT_SEARCH);
                return true;
            }
            case R.id.action_settings: {
                Toast.makeText(getApplicationContext(), "Setting tapped", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAirInfoSharedPreferenceChanged(String key, String value) {
        Log.e(StaticData.TAG, "MainActivity get notification for " + key);
        if(SettingsActivity.KEY_PREF_IS_AUTOUPDATE.equals(key)){
            Log.e(StaticData.TAG, "value:"+value);
            if (value == "true") {
                RestartAlarm(mPref.getString(SettingsActivity.KEY_PREF_UPDATE_HOUR, StaticData.DEFAULT_UPDATEHOUR));
            } else if(value == "false"){
                CancelAlarm();
                Intent serviceIntent = new Intent(MainActivity.this, DustService.class);
                stopService(serviceIntent);
            }
        }
        else if(SettingsActivity.KEY_PREF_UPDATE_HOUR.equals(key)){
            Log.e(StaticData.TAG, "value:"+value);
            RestartAlarm(value);
        }
    }

    public class AirInfoTask extends AsyncTask<String, Integer, Boolean>{
        String response;
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(StaticData.TAG, "AirInfo request for " + params[0]);
            AirInfoAPIClient mAirClient = new AirInfoAPIClient();
            response = mAirClient.getStationInfo(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            getApplicationContext();
            try{
                JSONObject json = new JSONObject(response);
                JSONArray jsonArr = json.getJSONArray("list");
                if(jsonArr != null) {
                    JSONObject airInfoObj = jsonArr.getJSONObject(0);
                    String pm10Value = airInfoObj.getString(StaticData.PM10_VALUE_KEY);
                    String pm10Grade = null;
//                    String pm10Grade = airInfoObj.getString(StaticData.PM10_GRADE_KEY);
                    final String[] gradeString = {"", "좋음", "보통", "나쁨", "매우 나쁨"};
                    int grade = Integer.parseInt(airInfoObj.getString(StaticData.PM10_GRADE_KEY));
                    if(0 < grade && grade < 5)
                        pm10Grade = gradeString[grade];
                    else
                        Log.e(StaticData.TAG, "unexpected grade:" + grade);
                    Toast.makeText(getApplicationContext(), "pm10Value:" + pm10Value + "pm10Grade:" + pm10Grade, Toast.LENGTH_LONG).show();
                }
            }catch(JSONException e){
                Log.e(StaticData.TAG, "Exception : " + e.toString());
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public AirInfoTask() {
            super();
        }
    }
}
