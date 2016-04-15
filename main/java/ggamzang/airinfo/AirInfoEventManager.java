package ggamzang.airinfo;

import java.util.ArrayList;

/**
 * Created by 미진앤찬섭 on 2016-04-15.
 */
public class AirInfoEventManager {
    private ArrayList<AirInfoSharedPreferenceChangeListener> mPreferenceObserver = null;
    private static AirInfoEventManager mInstance = null;

    private AirInfoEventManager(){
        mPreferenceObserver = new ArrayList<>();
    }

    public static AirInfoEventManager getInstance(){
        if(mInstance == null){
            mInstance = new AirInfoEventManager();
        }
        return mInstance;
    }

    public void addPreferenceListener(AirInfoSharedPreferenceChangeListener listener){
        if( mPreferenceObserver != null ){
            mPreferenceObserver.add(listener);
        }
    }

    public void notifyPreferenceChanged(String key, String value){
        if(mPreferenceObserver != null){
            for (AirInfoSharedPreferenceChangeListener listener: mPreferenceObserver) {
                listener.onAirInfoSharedPreferenceChanged(key, value);
            }
        }
    }

}
