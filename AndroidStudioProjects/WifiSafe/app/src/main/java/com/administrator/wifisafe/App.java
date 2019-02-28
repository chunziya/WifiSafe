package com.administrator.wifisafe;

import android.app.Application;

import com.administrator.wifisafe.util.WifiUtil;

/**
 * @author lesences  2018/5/26
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WifiUtil.init(this);


    }
}
