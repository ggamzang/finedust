package ggamzang.airinfo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chansub.shin on 2016-04-21.
 */
public class ReverseGeocodingAPIClient {

    public String getAddress(String latitude, String longitude){  // 위도, 경도
        HttpURLConnection urlConn = null;
        StringBuffer response = new StringBuffer();
        try {
            String urlStr = String.format(StaticData.GOOGLE_REVERSE_GEOCODING_API_URL, latitude, longitude, StaticData.GOOGLE_API_KEY);
            URL url = new URL(urlStr);
            Log.d(StaticData.TAG, "url:" + url.toString());
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
