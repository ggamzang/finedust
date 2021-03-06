package ggamzang.airinfo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chansub.shin on 2016-01-08.
 */

public class DustService extends Service {
    private SharedPreferences mPref = null;
    private Thread mThread          = null;
    private String stationName      = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(StaticData.TAG, "DustService Created");
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        stationName = mPref.getString(StaticData.PREF_STATION_KEY, "");
        Log.d(StaticData.TAG, stationName);

        Intent intent = new Intent(DustService.this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(DustService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification noti = new NotificationCompat.Builder(DustService.this)
                .setContentTitle(stationName + "미세먼지 정보")
                .setContentText("정보 업데이트 중...")
                .setSmallIcon(R.drawable.cloud)
                .setContentIntent(pIntent)
                .build();

        startForeground(StaticData.NOTI_ID, noti);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(StaticData.TAG, "DustService onStartCommand");
        stationName = mPref.getString(StaticData.PREF_STATION_KEY, "");
        Log.d(StaticData.TAG, stationName);

        if(stationName.length() > 0){
            // INFO : not able to http communication in main thread.
            mThread = new Thread("DustService Thread"){
                @Override
                public void run() {
                    super.run();
                    AirInfoAPIClient mAirClient = new AirInfoAPIClient();
                    String response = mAirClient.getStationInfo(stationName);
                    Log.d(StaticData.TAG, "airInfo : " + response);

                    String pm10Value = "-";
                    String pm10Grade = "-";
                    int imageID = 0;
                    try{
                        JSONObject json = new JSONObject(response);
                        JSONArray jsonArr = json.getJSONArray("list");
                        if(jsonArr != null) {
                            JSONObject airInfoObj = jsonArr.getJSONObject(0);
                            pm10Value = airInfoObj.getString(StaticData.AIR_PM10_VALUE_KEY);
                            int pm10Value_int = Integer.parseInt(pm10Value);
                            pm10Grade = StaticData.GetGradeString(pm10Value_int, StaticData.GRADE_TYPE_PM10);
                            imageID = StaticData.getGradeImage(pm10Value_int, StaticData.GRADE_TYPE_PM10);
                            /*int grade = Integer.parseInt(airInfoObj.getString(StaticData.AIR_PM10_GRADE_KEY));
                            if(0 < grade && grade < 5)
                                pm10Grade = gradeString[grade];
                            else
                                Log.e(StaticData.TAG, "unexpected grade:" + grade);*/
                        }
                    }catch(JSONException e){
//                        imageID = StaticData.getGradeImage(-1, StaticData.GRADE_TYPE_PM10);
                        Log.e(StaticData.TAG, e.toString());
                        return;
                    }catch(NumberFormatException e){
//                        imageID = StaticData.getGradeImage(-1, StaticData.GRADE_TYPE_PM10);
                        Log.e(StaticData.TAG, e.toString());
                        return;
                    }

                    Intent intent = new Intent(DustService.this, MainActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(DustService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification updatedNoti = new NotificationCompat.Builder(DustService.this)
                            .setContentTitle(stationName + " 미세먼지 정보")
                            .setContentText("미세먼지 : " + pm10Value + " ㎍/㎥ ( " + pm10Grade + " )")
                            .setSmallIcon(imageID)
                            .setContentIntent(pIntent)
                            .build();

                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(StaticData.NOTI_ID, updatedNoti);
                }
            };

            mThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(StaticData.TAG, "DustService Destoryed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
