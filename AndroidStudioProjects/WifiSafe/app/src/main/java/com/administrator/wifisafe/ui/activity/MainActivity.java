package com.administrator.wifisafe.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.WifiAsyncTask;
import com.administrator.wifisafe.bean.WifiBean;
import com.administrator.wifisafe.ui.adapter.WifiListAdapter;
import com.administrator.wifisafe.ui.dialog.BottomDialog;
import com.administrator.wifisafe.util.SmartUtil;
import com.administrator.wifisafe.util.WifiUtil;
import com.administrator.wifisafe.widget.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static int fab_what = 888;
    private final static int scan_what = 999;
    private static WifiAsyncTask wifiAsyncTask;

    private Snackbar snackbar = null;
    private FloatingActionButton fab;

    private WifiBroadcastReceiver wifiReceiver;

    private List<WifiBean> wifiBeanList = null;
    private WifiListAdapter listAdapter = null;
    private RecyclerView rvWifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FrameLayout.LayoutParams toolbarLp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        toolbarLp.setMargins(0, SmartUtil.getStatusHeight(), 0, 0);
        AppCompatTextView appNameText = findViewById(R.id.actv_app_name);
        FrameLayout.LayoutParams textLp = (FrameLayout.LayoutParams) appNameText.getLayoutParams();
        textLp.setMargins(0, SmartUtil.getStatusHeight(), 0, 0);

        rvWifiList = findViewById(R.id.rv_wifi_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvWifiList.setLayoutManager(layoutManager);

        rvWifiList.addOnItemTouchListener(new OnRecyclerItemClickListener(rvWifiList) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh, View child) {
                if (isDestroyed() || isFinishing()) {
                    return;
                }
                int position = vh.getAdapterPosition();
                WifiBean wifiBean = wifiBeanList.get(position);
                if (wifiBean.isConnect()) {
                    BottomDialog bottomDialog = new BottomDialog();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(BottomDialog.kWifiBean, wifiBean);
                    bottomDialog.setArguments(bundle);
                    bottomDialog.show(getSupportFragmentManager(), "");
                }
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setClickable(false);
                //两秒后，可再次点击
                mHandler.sendEmptyMessageDelayed(fab_what, 2 * 1000L);
                stopScan();
                scanWifi();
            }
        });

        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        registerReceiver(wifiReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WifiUtil.getInstance().isWifiEnable()) {
            hideSnackbar();
            scanWifi();
        } else {
            showSnackbar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }

    public void setWifiBeanList(List<WifiBean> wifiBeanList) {
        if (null == wifiBeanList) {
            wifiBeanList = new ArrayList<>();
        }
        if (null == this.wifiBeanList) {
            this.wifiBeanList = wifiBeanList;
        } else {
            this.wifiBeanList.clear();
            this.wifiBeanList.addAll(wifiBeanList);
        }

        if (null == listAdapter) {
            listAdapter = new WifiListAdapter(this, this.wifiBeanList);
            rvWifiList.setAdapter(listAdapter);
        } else {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLING);
                    if (state == WifiManager.WIFI_STATE_DISABLED) {
                        // WLAN已经关闭
                        stopScan();
                        showSnackbar();
                        if (null != wifiBeanList) {
                            wifiBeanList.clear();
                            if (null != listAdapter) {
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    } else if (state == WifiManager.WIFI_STATE_ENABLED) {
                        //WLAN已经打开
                        stopScan();
                        scanWifi();
                    }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (null != info) {
                        if (NetworkInfo.State.DISCONNECTED == info.getState()) {//WiFi未连接
                            disconnectAll();
                        } else if (NetworkInfo.State.CONNECTED == info.getState()) {//WiFi连接上
                            connected();
                        }
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    stopScan();
                    scanWifi();
                    break;

            }

        }
    }


    private void showSnackbar() {
        if (null == snackbar) {
            snackbar = Snackbar.make(fab, R.string.wifi_unconnect, Snackbar.LENGTH_SHORT);
        }
        if (!snackbar.isShown()) {
            snackbar.show();
        }
    }

    private void hideSnackbar() {
        if (null != snackbar && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case fab_what:
                    fab.setClickable(true);
                    break;
                case scan_what:
                    scanWifi();
                    break;
            }

            return true;
        }
    });

    /**
     * 取消扫描WiFi
     */
    private void stopScan() {
        if (null != wifiAsyncTask && !wifiAsyncTask.isCancelled()) {
            wifiAsyncTask.cancel(true);
            wifiAsyncTask = null;
        }
        if (mHandler.hasMessages(scan_what)) {
            mHandler.removeMessages(scan_what);
        }
    }

    /**
     * 开启扫描WiFi
     */
    private void scanWifi() {
        wifiAsyncTask = new WifiAsyncTask(this);
        wifiAsyncTask.execute();
        mHandler.sendEmptyMessageDelayed(scan_what, 2 * 60 * 1000L);
    }

    /**
     * 未连接到WiFi
     */
    private void disconnectAll() {
        if (!SmartUtil.isNullOrEmpty(wifiBeanList)) {
            for (WifiBean wifiBean : wifiBeanList) {
                wifiBean.setConnect(false);
                int iconId = WifiManager.calculateSignalLevel(wifiBean.getSignalLevel(), 5);
                wifiBean.setIconId(WifiAsyncTask.wifiLevel[iconId]);
            }
            Collections.sort(wifiBeanList, new Comparator<WifiBean>() {
                @Override
                public int compare(WifiBean o1, WifiBean o2) {
                    int tempLevel = o1.getSignalLevel() - o2.getSignalLevel();
                    return Integer.compare(0, tempLevel);
                }
            });

            if (null != listAdapter) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 连接上WiFi
     */
    private void connected() {
        if (!SmartUtil.isNullOrEmpty(wifiBeanList)) {
            WifiInfo wifiInfo = WifiUtil.getInstance().getConnectWifiInfo();
            if (wifiInfo == null) {
                return;
            }
            String bssidStr = wifiInfo.getBSSID();
            WifiBean connectBean = null;
            Iterator<WifiBean> iterableBean = wifiBeanList.iterator();
            while (iterableBean.hasNext()) {
                WifiBean wifiBean = iterableBean.next();
                if (wifiBean.getBSSID().equals(bssidStr)) {
                    wifiBean.setConnect(true);
                    wifiBean.setIconId(R.drawable.ic_connect);
                    connectBean = wifiBean;
                    iterableBean.remove();
                } else {
                    wifiBean.setConnect(false);
                    int iconId = WifiManager.calculateSignalLevel(wifiBean.getSignalLevel(), 5);
                    wifiBean.setIconId(WifiAsyncTask.wifiLevel[iconId]);
                }
            }
            Collections.sort(wifiBeanList, new Comparator<WifiBean>() {
                @Override
                public int compare(WifiBean o1, WifiBean o2) {
                    int tempLevel = o1.getSignalLevel() - o2.getSignalLevel();
                    return Integer.compare(0, tempLevel);
                }
            });
            if (null != connectBean) {
                wifiBeanList.add(0, connectBean);
            }

            if (null != listAdapter) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }
}
