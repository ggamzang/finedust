package ggamzang.airinfo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

/**
 * Created by chansub.shin on 2016-01-09.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences mPref = context.getSharedPreferences("myPref", Activity.MODE_PRIVATE);
            Boolean autoUpdate = mPref.getBoolean(StaticData.PREF_AUTOUPDATE_KEY, false);
            if(autoUpdate == true) {
                AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                PendingIntent alarmIntent = PendingIntent.getService(context, 0, new Intent(context, DustService.class), 0);

                long firstTime = SystemClock.elapsedRealtime();

                Float updateCycleTime = Float.parseFloat(mPref.getString(StaticData.PREF_AUTOUPDATETIME_KEY, StaticData.DEFAULT_UPDATEHOUR));
                updateCycleTime *= (1000 * 60 * 60); // convert hour to mili sec

                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, updateCycleTime.longValue(), alarmIntent);
            }
        }
    }
}
