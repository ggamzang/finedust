package ggamzang.airinfo;

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
}
