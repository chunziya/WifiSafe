package com.administrator.wifisafe;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.administrator.wifisafe.bean.WifiBean;
import com.administrator.wifisafe.ui.activity.MainActivity;
import com.administrator.wifisafe.util.SmartUtil;
import com.administrator.wifisafe.util.WifiUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author lesences  2018/5/26
 */
public class WifiAsyncTask extends AsyncTask<Void, Void, List<WifiBean>> {
    private WeakReference<MainActivity> weakAty;
    public final static int[] wifiLevel = new int[]{
            R.drawable.ic_wifi_none,
            R.drawable.ic_wifi_one,
            R.drawable.ic_wifi_two,
            R.drawable.ic_wifi_three,
            R.drawable.ic_wifi_four
    };

    public WifiAsyncTask(MainActivity weakAty) {
        this.weakAty = new WeakReference<>(weakAty);
    }

    @Override
    protected List<WifiBean> doInBackground(Void... voids) {
        List<WifiBean> wifiBeanList;
        List<ScanResult> resultList = WifiUtil.getInstance().getScanResults();
        if (SmartUtil.isNullOrEmpty(resultList)) {
            wifiBeanList = null;
        } else {
            //BSSID 去重
            Set<String> baseSet = new HashSet<>(resultList.size());
            Iterator<ScanResult> baseIterator = resultList.iterator();
            while (baseIterator.hasNext()) {
                ScanResult scanResult = baseIterator.next();
                if (!baseSet.add(scanResult.BSSID)) {
                    baseIterator.remove();
                }
            }
            //SSID 去重
            Set<String> resultSet = new HashSet<>(resultList.size());
            Iterator<ScanResult> scanIterator = resultList.iterator();
            while (scanIterator.hasNext()) {
                ScanResult scanResult = scanIterator.next();
                if (!resultSet.add(scanResult.SSID)) {
                    scanIterator.remove();
                }
            }


            int size = resultList.size();
            wifiBeanList = new ArrayList<>(size);
            WifiInfo wifiInfo = WifiUtil.getInstance().getConnectWifiInfo();
            List<WifiBean> tempList = new ArrayList<>(size - 1);
            for (int i = 0; i < size; i++) {
                ScanResult scanResult = resultList.get(i);
                WifiBean wifiBean = new WifiBean();
                wifiBean.setBSSID(scanResult.BSSID);
                wifiBean.setSSID(scanResult.SSID);
                wifiBean.setSignalLevel(scanResult.level);
                wifiBean.setCapabilities(getCapabilities(scanResult.capabilities));
                boolean connect = null != wifiInfo && scanResult.BSSID.equals(wifiInfo.getBSSID());
                wifiBean.setConnect(connect);
                if (connect) {
                    wifiBean.setIconId(R.drawable.ic_connect);
                    wifiBeanList.add(wifiBean);
                } else {
                    int iconId = WifiManager.calculateSignalLevel(scanResult.level, 5);
                    wifiBean.setIconId(wifiLevel[iconId]);
                    tempList.add(wifiBean);
                }
            }

            Collections.sort(tempList, new Comparator<WifiBean>() {
                @Override
                public int compare(WifiBean o1, WifiBean o2) {
                    int tempLevel = o1.getSignalLevel() - o2.getSignalLevel();
                    return Integer.compare(0, tempLevel);
                }
            });
            wifiBeanList.addAll(tempList);
        }
        return wifiBeanList;
    }


    @Override
    protected void onPostExecute(List<WifiBean> wifiBeanList) {
        MainActivity mainActivity = weakAty.get();
        if (null != mainActivity) {
            mainActivity.setWifiBeanList(wifiBeanList);
        }
    }

    private String getCapabilities(String capabilities) {
        if (TextUtils.isEmpty(capabilities)) {
            capabilities = "";
        } else if (capabilities.contains("PSK")) {
            capabilities = "WPA-PSK/WPA2-PSK";
        } else if (capabilities.contains("WPA")) {
            capabilities = "WPA/WPA2";
        } else if (capabilities.contains("WEP")) {
            capabilities = "WEP";
        } else if (capabilities.contains("EAP")) {
            capabilities = "EAP";
        } else {
            capabilities = "";
        }
        return capabilities;
    }
}
