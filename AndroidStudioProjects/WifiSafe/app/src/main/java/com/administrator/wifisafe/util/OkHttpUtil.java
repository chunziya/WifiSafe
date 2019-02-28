package com.administrator.wifisafe.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author lesences  2018/5/27 12:40.
 */
public final class OkHttpUtil {
    private static OkHttpClient okHttpClient = null;

    public static OkHttpClient getOkHttpClient() {
        if (null == okHttpClient) {
            synchronized (OkHttpUtil.class) {
                if (null == okHttpClient) {
                    okHttpClient = new OkHttpClient
                            .Builder()
                            .connectTimeout(2, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false)
                            .build();
                }
            }
        }
        return okHttpClient;
    }
}
