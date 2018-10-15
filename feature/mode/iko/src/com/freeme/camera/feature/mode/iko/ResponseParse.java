package com.freeme.camera.feature.mode.iko;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.net.URLDecoder;

public class ResponseParse {

    public static final String TAG = "Client_Response_TAG";

    /**
     * 解析的对象为Null
     */
    public static final int CODE_JNI_RESPONSE_EMPTY = 101;
    /**
     * JSON格式解析异常
     */
    public static final int CODE_JSON_PARSE_EXCEPTION = 102;

    private static class ResponseParseHolder {
        private final static ResponseParse INSTANCE = new ResponseParse();
    }

    /**
     * 获取ResponseParse的单例
     *
     * @return
     */
    public static ResponseParse getInstance() {
        return ResponseParseHolder.INSTANCE;
    }

    public Object parseJsonData(String responseData, Class<?> clzz) {
        int i = 0;
        byte[] bytes = responseData.getBytes();
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == '{') {
                byte n[] = new byte[bytes.length - i];
                for (int j = i; j < bytes.length; j++) {
                    n[j - i] = bytes[j];
                }
                responseData = new String(n);
                break;
            }
        }

        // 跨端表情编码问题
        if (!TextUtils.isEmpty(responseData)) {

            try {
                responseData = URLDecoder.decode(responseData);
            } catch (Exception e) {
                Log.e(TAG, "编码异常>>>>>>");
                e.printStackTrace();
                return null;
            }
        }
        Object object = null;
        try {
            Gson gson = new Gson();
            object = gson.fromJson(responseData, clzz);
            if (object != null) {
            } else {
                Log.e(TAG, "###Json解析的结果为空###" + responseData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "###Json解析异常###" + responseData);
        }
        return object;
    }

}
