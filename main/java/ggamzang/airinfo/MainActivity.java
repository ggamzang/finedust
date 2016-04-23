package ggamzang.airinfo;
/* TODO :   help 메뉴
        :   움직이는 위치에 따라 측정장소 변경 - x
        :   정보 받아올때 마다 Noti update
*/
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import android.support.v7.widget.Toolbar;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements AirInfoSharedPreferenceChangeListener{
    private TextView mTVSelected            = null;
    private TextView mTVAirInfo             = null;
    private TextView mTVCIA                 = null;
    private TextView mTVPM10                = null;
    private TextView mTVO3                  = null;
    private TextView mTVNO2                 = null;
    private TextView mTVSO2                 = null;

    private LinearLayout mLLMain            = null;
    private TextView mTVGuide                = null;
    /*private Button mBtnColor    = null;
    private ImageView mIVColor  = null;*/
    private SharedPreferences mPref         = null;

    private static final int RESULT_SEARCH  =   1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.inflateMenu(R.menu.toolbar_item);

        mLLMain     = (LinearLayout)findViewById(R.id.llMain);
        mTVGuide    = (TextView)findViewById(R.id.tvGuide);
        mTVSelected = (TextView)findViewById(R.id.tvSelectedStationName);
        mTVAirInfo  = (TextView)findViewById(R.id.tvAirInfo);
        /*mBtnColor = (Button)findViewById(R.id.btnColor);
        mIVColor = (ImageView)findViewById(R.id.ivColor);*/

        mTVCIA = (TextView)findViewById(R.id.tvCIA);
        mTVPM10 = (TextView)findViewById(R.id.tvPM10);
        mTVO3 = (TextView)findViewById(R.id.tvO3);
        mTVNO2 = (TextView)findViewById(R.id.tvNO2);
        mTVSO2 = (TextView)findViewById(R.id.tvSO2);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(mTVSelected != null){
            mTVSelected.setText(mPref.getString(StaticData.PREF_STATION_KEY, ""));

            if (mTVSelected.getText().length() > 0) {
                mLLMain.setVisibility(View.VISIBLE);
                AirInfoTask airInfoTask = new AirInfoTask();
                if (airInfoTask != null)
                    airInfoTask.execute(mTVSelected.getText().toString());
            }
            else{
                mTVGuide.setVisibility(View.VISIBLE);
            }
        }

        /*mBtnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Drawable img = R.drawable.ic_wb_cloudy_black_48dp;
                Drawable drb = (Drawable)getResources().getDrawable(R.drawable.ic_wb_cloudy_white_48dp);
                drb.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                mIVColor.setImageDrawable(drb);
            }
        });*/
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
                        if (mTVSelected.getText().length() > 0) {
                            mLLMain.setVisibility(View.VISIBLE);
                            mTVGuide.setVisibility(View.GONE);
                        }
                        else{
                            mLLMain.setVisibility(View.GONE);
                            mTVGuide.setVisibility(View.VISIBLE);
                        }
                    }
                    if( mPref.getBoolean(SettingsActivity.KEY_PREF_IS_AUTOUPDATE, false) == true ) {
                        Intent serviceIntent = new Intent(MainActivity.this, DustService.class);
                        stopService(serviceIntent);

                        startService(serviceIntent);
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

        long firstTime = SystemClock.elapsedRealtime() + updateHour * AlarmManager.INTERVAL_HOUR;
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
                Intent serviceIntent = new Intent(MainActivity.this, DustService.class);
                startService(serviceIntent);

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
                    mTVAirInfo.setText(airInfo);

                    mTVCIA.setText(KHAI_Value + " - " + KHAI_Grade);
                    mTVPM10.setText(pm10_Value + "㎍/㎥(1H)" + pm10_Value24);
                    mTVO3.setText(O3_Value + " ppm - " + O3_Grade);
                    mTVNO2.setText(NO2_Value + " ppm - " + NO2_Grade);
                    mTVSO2.setText(SO2_Value + " ppm - " + SO2_Grade);
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
