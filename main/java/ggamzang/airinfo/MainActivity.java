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
    TextView mTVSelected            = null;
    ListView mLVStationList         = null;
    EditText mETsearchStationName   = null;
    Button mBTNgetInfo              = null;
    TextView mTVtest = null;

    ArrayList<StationInfo> mStationList = null;
    ListView mListView = null;
    StationListAdapter mAdapter = null;

    SharedPreferences mPref = null;
    SharedPreferences.Editor mPrefEdit= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.inflateMenu(R.menu.toolbar_item);

        mTVSelected = (TextView)findViewById(R.id.tvSelectedStationName);
        mLVStationList = (ListView)findViewById(R.id.lvStationList);
        mETsearchStationName = (EditText)findViewById(R.id.etStationName);
        mBTNgetInfo = (Button)findViewById(R.id.btnGetInfo);
        mTVtest = (TextView)findViewById(R.id.test);

        if(mTVtest != null)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean isAuto = sharedPref.getBoolean(SettingsActivity.KEY_PREF_IS_AUTOUPDATE, false);
            String time = sharedPref.getString(SettingsActivity.KEY_PREF_UPDATE_HOUR, "");
            mTVtest.setText("isAuto:"+isAuto+", time:"+time);
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(this);//getSharedPreferences("myPref", Activity.MODE_PRIVATE);
        if(mPref != null)
            mPrefEdit = mPref.edit();

        if(mLVStationList != null) {
            mLVStationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mStationList != null) {
                        if (mTVSelected != null)
                            mTVSelected.setText(mStationList.get(position).getStationName());

                        mPrefEdit.putString(StaticData.PREF_STATION_KEY, mStationList.get(position).getStationName());
                        mPrefEdit.apply();
                    }
                }
            });
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

        Button btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mETsearchStationName.getText().length() > 0 ) {
                    StationInfoTask stationTask = new StationInfoTask();
                    if(stationTask != null)
                        stationTask.execute(mETsearchStationName.getText().toString());
                }
            }
        });

        mStationList = new ArrayList<StationInfo>();

        if(mTVSelected != null) {
            if(mPref != null) {
                mTVSelected.setText(mPref.getString(StaticData.PREF_STATION_KEY, ""));
            }

            if(mTVSelected.getText().length() > 0) {
                Boolean autoUpdate = mPref.getBoolean(StaticData.PREF_AUTOUPDATE_KEY, false);
                if(autoUpdate == true) {
                    // TODO : check whether alarm is already set or not.
                    RestartAlarm(mPref.getString(StaticData.PREF_AUTOUPDATETIME_KEY, StaticData.DEFAULT_UPDATEHOUR));
                }
            }
        }

        AirInfoEventManager.getInstance().addPreferenceListener(this);
    }

    private void RestartAlarm(String hour){
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, DustService.class), 0);

        am.cancel(alarmIntent);

        long updateHour = Long.getLong(hour);
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
                startActivity(intent);
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
                RestartAlarm(mPref.getString(StaticData.PREF_AUTOUPDATETIME_KEY, StaticData.DEFAULT_UPDATEHOUR));
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

    public class StationInfoTask extends AsyncTask<String, Integer, Boolean> {
        private String response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show progress
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if( mStationList.isEmpty() == false )
                mStationList.clear();

//            StringBuilder strBdstationName = new StringBuilder();
            int totalCount = 0;
            try {
                JSONObject json = new JSONObject(response);
                totalCount = json.getInt("totalCount");
                JSONArray jsonArr = json.getJSONArray("list");
                for(int i = 0 ; i < totalCount; i++){
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
//                    strBdstationName.append(jsonObj.getString("stationName") + ",");

                    mStationList.add(new StationInfo(jsonObj.getString("stationName"), jsonObj.getString("addr")));
                }
            }catch (JSONException e)
            {
                Log.e(StaticData.TAG, "Exception : " + e.toString());
            }

            mAdapter = new StationListAdapter( getApplicationContext(), mStationList);
            mListView = (ListView)findViewById(R.id.lvStationList);
            mListView.setAdapter(mAdapter);
        }

        // called when calling publishProgress() in doInBackground
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(StaticData.TAG, "Station search request for" + params[0]);
            StationInfoAPIClient mAirClient = new StationInfoAPIClient();
            response = mAirClient.getStationInfo(params[0]);
            return null;
        }
    }
}
