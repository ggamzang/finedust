package ggamzang.airinfo;
/* TODO
 * : GPS 구현
 * : EditText 두줄 안되고, 엔터하면 검색
 */
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {// implements LocationListener {

    private EditText mETStationName = null;
    private Button mBtnSearch       = null;
    private Button mBtnGPS          = null;
    private ListView mLVStationList = null;
    private StationListAdapter mAdapter = null;
    private GPSSettingsDialog mSettingsDialog = null;

    private ArrayList<StationInfo> mStationList = null;

    private SharedPreferences mPref = null;
    private SharedPreferences.Editor mPrefEdit= null;

    public static final String EXTRA_SEARCH_STATIONNAME = "staionName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mStationList = new ArrayList<StationInfo>();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPref != null)
            mPrefEdit = mPref.edit();

        mETStationName = (EditText)findViewById(R.id.etStationName);
        mBtnSearch = (Button)findViewById(R.id.btnSearch);
        mLVStationList = (ListView)findViewById(R.id.lvStationList);
        mBtnGPS         = (Button)findViewById(R.id.btnGPS);
        mSettingsDialog = new GPSSettingsDialog(this);

        if(mBtnSearch != null){
            mBtnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mETStationName != null && mETStationName.length() > 0)
                    {
                        StationInfoTask stationTask = new StationInfoTask();
                        if(stationTask != null)
                            stationTask.execute(mETStationName.getText().toString());
                    }
                }
            });
        }

        if(mLVStationList != null) {
            mLVStationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mStationList != null) {
                        mPrefEdit.putString(StaticData.PREF_STATION_KEY, mStationList.get(position).getStationName());
                        mPrefEdit.apply();

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_SEARCH_STATIONNAME, mStationList.get(position).getStationName());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        }

        if(mBtnGPS != null){
            mBtnGPS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    boolean isGPSEnalbed = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                    if(isGPSEnalbed == false){
                        mSettingsDialog.show();
                        return;
                    }

                    double latitude = 0;
                    double longitude = 0;

                    Location networkLocation = null;
                    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    if(isNetworkEnabled) {
                        networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(networkLocation != null){
                            Log.e(StaticData.TAG, "[NETWORK] 위도" + networkLocation.getLatitude() + ", 경도:" + networkLocation.getLongitude());
                            latitude = networkLocation.getLatitude();
                            longitude = networkLocation.getLongitude();
                        }
                        else{
                            Log.e(StaticData.TAG, "[NETWORK] not able to get location object");
                        }
                    }

                    Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(gpsLocation != null) {
                        Log.d(StaticData.TAG, "[GPS] 위도:" + gpsLocation.getLatitude() + ", 경도:" + gpsLocation.getLongitude());
                        latitude = gpsLocation.getLatitude();
                        longitude = gpsLocation.getLongitude();
                    }
                    else{
                        Log.e(StaticData.TAG, "[GPS] not able to get location object");
                    }

                    ReverseGeocodingTask geoTask = new ReverseGeocodingTask();
                    if(geoTask != null){
                        geoTask.execute(Double.toString(latitude), Double.toString(longitude));
                    }
                }
            });
        }

        if(mSettingsDialog != null){
            mSettingsDialog.setTitle("위치 서비스 사용");
            mSettingsDialog.setSettingsClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    mSettingsDialog.cancel();
                }
            });

            mSettingsDialog.setCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSettingsDialog.cancel();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
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
                if(jsonArr.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "해당 지역에 측정소 정보가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.d(StaticData.TAG, "totalCount:" + totalCount + ", len:" + jsonArr.length());
                for(int i = 0 ; i < jsonArr.length(); i++){
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
//                    strBdstationName.append(jsonObj.getString("stationName") + ",");
                    mStationList.add(new StationInfo(jsonObj.getString("stationName"), jsonObj.getString("addr")));
                }
            }catch (JSONException e)
            {
                Log.e(StaticData.TAG, "Exception : " + e.toString());
            }

            mAdapter = new StationListAdapter( getApplicationContext(), mStationList);
            mLVStationList = (ListView)findViewById(R.id.lvStationList);
            mLVStationList.setAdapter(mAdapter);
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

    /*
    * TODO : GPS 로 근처 측정소 찾기
    * 동네로 TM 좌표 가져옴( 근처 측정소 TM을 못가져옴 ;; )
    * http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt?umdName=%EB%8F%99%ED%83%84&pageNo=1&numOfRows=10&ServiceKey=uAmy02OcSWzBk2mjIbujDRJsMg4piiLiWNHooYln31zMqlisoxDwuJO9z3MsyvKHJnu3fXPZnRgm3nEDLND38A%3D%3D
    *
    * TM좌표로 근처 측정소 찾기
    * 동탄 ( 209794.232219, 409509.684878 )
    * http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?tmX=209794.232219&tmY=409509.684878&pageNo=1&numOfRows=10&ServiceKey=uAmy02OcSWzBk2mjIbujDRJsMg4piiLiWNHooYln31zMqlisoxDwuJO9z3MsyvKHJnu3fXPZnRgm3nEDLND38A%3D%3D
    */
    public class ReverseGeocodingTask extends AsyncTask<String, Integer, Boolean>{
        private String response;

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(getApplicationContext(), "주소 : " + response, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(StaticData.TAG, "위도:"+params[0]+", 경도:"+params[1]);
            ReverseGeocodingAPIClient mGeocodingClient = new ReverseGeocodingAPIClient();
            response = mGeocodingClient.getAddress(params[0], params[1]);
            return null;
        }
    }
}
