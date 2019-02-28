package com.administrator.wifisafe.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author lesences  2018/5/26
 */
public final class WifiUtil {
    private static WifiUtil instance = null;
    private WifiManager mWifiManager;

    private WifiUtil(Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (null == mWifiManager) {
            throw new NullPointerException("mWifiManager is null");
        }
    }

    public static void init(@NonNull Context context) {
        if (null == instance) {
            synchronized (WifiUtil.class) {
                if (null == instance) {
                    instance = new WifiUtil(context);
                }
            }
        }
    }

    public static WifiUtil getInstance() {
        if (null == instance) {
            throw new NullPointerException("instance can not be null ,  please init(context) first");
        }
        return instance;
    }

    public boolean isWifiEnable() {
        return mWifiManager.isWifiEnabled();
    }

    public List<ScanResult> getScanResults() {
        return mWifiManager.getScanResults();
    }

    public WifiInfo getConnectWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }
}
