package ggamzang.airinfo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class SearchActivity extends AppCompatActivity {

    private EditText mETStationName = null;
    private Button mBtnSearch = null;
    private ListView mLVStationList = null;
    private StationListAdapter mAdapter = null;

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

        mStationList = new ArrayList<StationInfo>();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPref != null)
            mPrefEdit = mPref.edit();

        mETStationName = (EditText)findViewById(R.id.etStationName);
        mBtnSearch = (Button)findViewById(R.id.btnSearch);
        mLVStationList = (ListView)findViewById(R.id.lvStationList);

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
                // TODO : 10개 이상 정보가 한번에 안옴.. 확인 필요
                JSONArray jsonArr = json.getJSONArray("list");
                if(jsonArr.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "해당 지역에 측정소 정보가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
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
}
