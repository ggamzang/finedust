package ggamzang.airinfo;

import android.util.Log;

/**
 * Created by user on 2016-01-05.
 */
public class StaticData {
    final static String TAG = "ggamzang";

    final static String PREF_STATION_KEY    = "stationName";

    // 측정시간 (연-월-일 시간:분)
    final static String AIR_DATATIME_KEY    = "dataTime";

    // 통합대기환경
    final static String AIR_KHAI_VALUE_KEY  = "khaiValue";
    final static String AIR_KHAI_GRADE_KEY  = "khaiGrade";

    // 미세먼지
    final static String AIR_PM10_VALUE_KEY  = "pm10Value";  // 1시간 측정 정보
    final static String AIR_PM10_VALUE24_KEY= "pm10Value24";// 24시간 측정 정보
    final static String AIR_PM10_GRADE_KEY  = "pm10Grade";  // 24시간 기준으로 등급 표시

    // 초미세먼지
    final static String AIR_PM25_VALUE_KEY  = "pm25Value";
    final static String AIR_PM25_VALUE24_KEY= "pm25Value24";
    final static String AIR_PM25_GRADE_KEY  = "pm25Grade";

    // 오존 - ppm
    final static String AIR_O3_VALUE_KEY    = "o3Value";
    final static String AIR_O3_GRADE_KEY    = "o3Grade";

    // 이산화질소 - ppm
    final static String AIR_NO2_VALUE_KEY   = "no2Value";
    final static String AIR_NO2_GRADE_KEY   = "no2Grade";

    // 아황산가스 - ppm
    final static String AIR_SO2_VALUE_KEY   = "so2Value";
    final static String AIR_SO2_GRADE_KEY   = "so2Grade";

    final static String DEFAULT_UPDATEHOUR  = "1";

    final static String GRADE_TYPE_CAI      = "CAI";
    final static String GRADE_TYPE_PM10     = "PM10";
    final static String GRADE_TYPE_PM25     = "PM25";

    // Naver Open API
    final static String CLIENT_ID           = "ziEvZm7GRAvbW4njDkcP";
    final static String CLIENT_SECRET       = "UaVDU99au7";

    // Google
    final static String GOOGLE_API_KEY      = "AIzaSyBpuAeVe2dWrFrSq8rOME1_CDH26LFTfw4";
    final static String GOOGLE_REVERSE_GEOCODING_API_URL      = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s&language=ko";

    final static int NOTI_ID                = 720;

    static String GetGradeString(int value, String gradeType){
        String grade = "";
        switch(gradeType)
        {
            case GRADE_TYPE_CAI:
            {
                final String[] gradeString = {"", "좋음", "보통", "나쁨", "매우 나쁨"};
                grade = gradeString[value];
                break;
            }
            case GRADE_TYPE_PM10:
            {
                if(0 <= value && value <= 30)
                {
                    grade = "좋음";
                }
                else if(31 <= value && value <= 80)
                {
                    grade = "보통";
                }
                else if(81 <= value && value <= 150)
                {
                    grade = "나쁨";
                }
                else if(151 <= value)
                {
                    grade = "매우나쁨";
                }
                else
                {
                    grade = "-";
                }
                break;
            }
            case GRADE_TYPE_PM25:
            {
                if(0 <= value && value <= 15)
                {
                    grade = "좋음";
                }
                else if(16 <= value && value <= 50)
                {
                    grade = "보통";
                }
                else if(51 <= value && value <= 100)
                {
                    grade = "나쁨";
                }
                else if(101 <= value)
                {
                    grade = "매우나쁨";
                }
                else
                {
                    grade = "-";
                }
                break;
            }
            default:
            {
                Log.d(TAG, "unexpected gradeType:"+gradeType);
                break;
            }
        }
        return grade;
    }

    static int getGradeImage(int value, String gradeType){
        int imageID = 0;
        switch(gradeType)
        {
            case GRADE_TYPE_CAI:
            {
                break;
            }
            case GRADE_TYPE_PM10:
            {
                if(0 <= value && value <= 30)
                {
                    imageID = R.drawable.ic_cloud_good_36dp;
                }
                else if(31 <= value && value <= 80)
                {
                    imageID = R.drawable.ic_cloud_normal_36dp;
                }
                else if(81 <= value && value <= 150)
                {
                    imageID = R.drawable.ic_cloud_bad_36dp;
                }
                else if(151 <= value)
                {
                    imageID = R.drawable.ic_cloud_verybad_36dp;
                }
                else
                {
                    imageID = R.drawable.ic_cloud_off_white_36dp;
                }
                break;
            }
            default:
            {
                Log.d(TAG, "unexpected gradeType:"+gradeType);
                break;
            }
        }
        return imageID;
    }
}
