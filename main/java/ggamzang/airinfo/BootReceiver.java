package ggamzang.airinfo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/**
 * Created by chansub.shin on 2016-01-09.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean autoUpdate = mPref.getBoolean(SettingsActivity.KEY_PREF_IS_AUTOUPDATE, false);
            if(autoUpdate == true) {
                AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                PendingIntent alarmIntent = PendingIntent.getService(context, 0, new Intent(context, DustService.class), 0);

                long firstTime = SystemClock.elapsedRealtime();
                long updateHour = (long)Integer.parseInt(mPref.getString(SettingsActivity.KEY_PREF_UPDATE_HOUR, StaticData.DEFAULT_UPDATEHOUR));

                am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, updateHour * AlarmManager.INTERVAL_HOUR, alarmIntent);
            }
        }
    }
}
