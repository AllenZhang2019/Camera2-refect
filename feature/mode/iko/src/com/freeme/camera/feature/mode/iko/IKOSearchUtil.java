package com.freeme.camera.feature.mode.iko;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by azmohan on 18-3-13.
 */

public class IKOSearchUtil {
    public static final String TOKEN_HTTP = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials";
    public static final String CLIENT_ID = "zP0b7bjP1oXxwQGeZRrYtkxPRq11T3d0";
    public static final String CLIENT_SECRET = "3BGv0l41KP5og0BW4Sj6GjFZGac90kmv";
    public static final String CLIENT_APP_ID = "1585928312337326";
    public static final String PARSE_URL = "https://openapi.baidu.com/rest/2.0/mms/general/url";

    public static boolean getNetWorkStatus(final Activity activity) {
        boolean netSataus = false;
        ConnectivityManager cwjManager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cwjManager.getActiveNetworkInfo() != null) {
            netSataus = cwjManager.getActiveNetworkInfo().isConnectedOrConnecting();
        }

        if (!netSataus) {
            NetworkInfo.State gprs = cwjManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            cwjManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING) {
                netSataus = true;
            }
        }
        return netSataus;
    }

    public static String accessNetworkByPost(String urlString) throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        boolean isPress = false;
        HttpURLConnection connection = null;

        try {

            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod("POST");
//            connection.setInstanceFollowRedirects(true);
//            connection.setRequestProperty("contentType", "utf-8");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            connection.setRequestProperty("Content-Length", "" + encrypted.length);

//            out = new DataOutputStream(connection.getOutputStream());
//            out.write(encrypted);
//            out.flush();
//            out.close();

            bis = new BufferedInputStream(connection.getInputStream());
            baf = new ByteArrayBuffer(1024);

            isPress = Boolean.valueOf(connection.getHeaderField("isPress"));

            int current = 0;
            Log.i("keke", "bis:" + bis);

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            if (baf.length() > 0) {
                line = new String(baf.toByteArray());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "NetworkUtil accessNetworkByPost exception:" + e.toString());
        } finally {
            if (connection != null) connection.disconnect();
            if (bis != null) bis.close();
            if (baf != null) baf.clear();
        }
        return line.trim();

    }

    public static String buildTokenUrlStr() {
        StringBuilder sb = new StringBuilder(TOKEN_HTTP);
        sb.append("&client_id=").append(CLIENT_ID).append("&client_secret=").append(CLIENT_SECRET);
        return sb.toString();
    }

    public static String buildParseUrlStr(String token, byte[] jpeg) {//2018.09.14
        StringBuilder sb = new StringBuilder();
        try {
            String imageStr = URLEncoder.encode(Base64.encodeToString(jpeg, Base64.DEFAULT), "UTF-8");
            sb.append("image=").append(imageStr)
                    .append("&client_app_id=").append(CLIENT_APP_ID);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
