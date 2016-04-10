package ggamzang.airinfo;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by user on 2016-01-04.
 */
public class AirInfoAPIClient {
        // Todo : fix error only coming 10 only even it's over 10
        final static String stationInfoUrlPre = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=";
        final static String stationInfoUrlPost = "&dataTerm=daily&pageNo=1&numOfRows=1&ServiceKey=uAmy02OcSWzBk2mjIbujDRJsMg4piiLiWNHooYln31zMqlisoxDwuJO9z3MsyvKHJnu3fXPZnRgm3nEDLND38A%3D%3D&ver=1.0";
        final static String jsonUrl = "&_returnType=json";

        public String getStationInfo(String stationName){
            HttpURLConnection urlConn = null;
            StringBuffer response = new StringBuffer();
            try {
                URL url = new URL(stationInfoUrlPre + URLEncoder.encode(stationName, "UTF-8") + stationInfoUrlPost + jsonUrl);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                if(urlConn.getResponseCode() == 200)
                {
                    Log.d(StaticData.TAG, "response code is 200");
                }
                else
                {
                    Log.d(StaticData.TAG, "response code is " + urlConn.getResponseCode());
                    return "connection failure";
                }

                InputStream in = urlConn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                while(true){
                    String line = br.readLine();
                    if(line == null)
                        break;
                    response.append(line);
                }

                Log.d(StaticData.TAG, response.toString());
            }
            catch(Exception e){
                Log.e(StaticData.TAG, "Exception : " + e.toString());
            }
            finally{
                if(urlConn != null)
                {
                    urlConn.disconnect();
                }
                if(response != null)
                {
                    return response.toString();
                }
                else
                {
                    return "abnormal response";
                }
            }
        }
}
