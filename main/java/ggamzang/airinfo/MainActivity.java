package ggamzang.airinfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.widget.Toolbar;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements AirInfoSharedPreferenceChangeListener{
    private TextView mTVSelected            = null;
    private Button mBTNgetInfo              = null;
    private TextView mTVAirInfo             = null;

    private SharedPreferences mPref         = null;

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
        mTVAirInfo  = (TextView)findViewById(R.id.tvAirInfo);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(mTVSelected != null){
            mTVSelected.setText(mPref.getString(StaticData.PREF_STATION_KEY, ""));

            if (mTVSelected.getText().length() > 0) {
                AirInfoTask airInfoTask = new AirInfoTask();
                if (airInfoTask != null)
                    airInfoTask.execute(mTVSelected.getText().toString());
            }
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
                    if( mPref.getBoolean(SettingsActivity.KEY_PREF_IS_AUTOUPDATE, false) == true ) {
                        Intent serviceIntent = new Intent(MainActivity.this, DustService.class);
                        stopService(serviceIntent);
                        RestartAlarm(mPref.getString(SettingsActivity.KEY_PREF_UPDATE_HOUR, StaticData.DEFAULT_UPDATEHOUR));
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTVSelected != null && mTVSelected.getText().length() > 0) {
            AirInfoTask airInfoTask = new AirInfoTask();
            if (airInfoTask != null)
                airInfoTask.execute(mTVSelected.getText().toString());
        }
    }

    private void RestartAlarm(String hour){
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, DustService.class), 0);

        am.cancel(alarmIntent);
        long updateHour = 1;
        try {
            updateHour = (long) Integer.parseInt(hour);
        } catch (NumberFormatException e){
            Log.e(StaticData.TAG, e.toString());
        }

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
                Intent intent = new Intent(this, SearchActivity.class);
                startActivityForResult(intent, RESULT_SEARCH);
                return true;
            }
            case R.id.action_settings: {
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
                if (jsonArr != null) {
                    JSONObject airInfoObj = jsonArr.getJSONObject(0);

                    String dateTime     = airInfoObj.getString(StaticData.AIR_DATATIME_KEY);

                    String KHAI_Value   = airInfoObj.getString(StaticData.AIR_KHAI_VALUE_KEY);
                    String KHAI_Grade   = getAirGrade(airInfoObj.getString(StaticData.AIR_KHAI_GRADE_KEY));

                    String pm10_Value   = airInfoObj.getString(StaticData.AIR_PM10_VALUE_KEY);
                    String pm10_Value24 = airInfoObj.getString(StaticData.AIR_PM10_VALUE24_KEY);
                    String pm10_Grade   = getAirGrade(airInfoObj.getString(StaticData.AIR_PM10_GRADE_KEY));

                    String pm25_Value   = airInfoObj.getString(StaticData.AIR_PM25_VALUE_KEY);
                    String pm25_Value24 = airInfoObj.getString(StaticData.AIR_PM25_VALUE24_KEY);
                    String pm25_Grade   = getAirGrade(airInfoObj.getString(StaticData.AIR_PM25_GRADE_KEY));

                    String O3_Value     = airInfoObj.getString(StaticData.AIR_O3_VALUE_KEY);
                    String O3_Grade     = getAirGrade(airInfoObj.getString(StaticData.AIR_O3_GRADE_KEY));

                    String NO2_Value    = airInfoObj.getString(StaticData.AIR_NO2_VALUE_KEY);
                    String NO2_Grade    = getAirGrade(airInfoObj.getString(StaticData.AIR_NO2_GRADE_KEY));

                    String SO2_Value    = airInfoObj.getString(StaticData.AIR_SO2_VALUE_KEY);
                    String SO2_Grade    = getAirGrade(airInfoObj.getString(StaticData.AIR_SO2_GRADE_KEY));

                    String airInfo = "";
                    airInfo += "측정시간 : " + dateTime + "\n";
                    airInfo += "통합대기환경 : " + KHAI_Value + " - " + KHAI_Grade + "\n";
                    airInfo += "미세먼지 : "   + pm10_Value + " ㎍/㎥(1H)," + pm10_Value24 + " ㎍/㎥(24H) - " + pm10_Grade + "\n";
                    airInfo += "초미세먼지 : " + pm25_Value + " ㎍/㎥(1H)," + pm25_Value24 + " ㎍/㎥(24H) - " + pm25_Grade + "\n";
                    airInfo += "오존 : "       + O3_Value + " ppm - " + O3_Grade + "\n";
                    airInfo += "이산화질소 : " + NO2_Value + " ppm - " + NO2_Grade + "\n";
                    airInfo += "아황산가스 : " + SO2_Value + " ppm - " + SO2_Grade + "\n";
                    mTVAirInfo.setText(airInfo);
                }
            }catch(JSONException e){
                Log.e(StaticData.TAG, "Exception : " + e.toString());
            }
        }

        private String getAirGrade(String grade){
            final String[] gradeString = {"-", "좋음", "보통", "나쁨", "매우 나쁨"};
            int parsedGrade = 0;
            try {
                parsedGrade = Integer.parseInt(grade);
            } catch (NumberFormatException e){
                Log.e(StaticData.TAG, e.toString());
            }
            if(0 < parsedGrade && parsedGrade < 5)
                return gradeString[parsedGrade];
            else
                return "-";
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
