package com.administrator.wifisafe.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.bean.WifiBean;
import com.administrator.wifisafe.ui.dialog.SpeedDialog;
import com.administrator.wifisafe.util.OkHttpUtil;
import com.administrator.wifisafe.util.SmartUtil;
import com.administrator.wifisafe.widget.ProgressDrawable;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author lesences  2018/5/26
 */
public class WifiSpeedActivity extends AppCompatActivity {
    public static DecimalFormat decimalFormat = new DecimalFormat(".00");
    private final static String kWifiBean = "wifi_bean";
    private final static int speed_what = 777;


    public static void startWifiSpeedActivity(Context packageContext, WifiBean wifiBean) {
        Intent intent = new Intent(packageContext, WifiSpeedActivity.class);
        intent.putExtra(kWifiBean, wifiBean);
        packageContext.startActivity(intent);
    }

    private AppCompatTextView actvTest;
    private AppCompatTextView actvSpeed;
    private ProgressDrawable speedDrawable;
    private Call httpCall;

    private List<Double> speedList = new ArrayList<>(4);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_speed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FrameLayout flTop = findViewById(R.id.fl_top);
        flTop.setPadding(0, SmartUtil.getStatusHeight(), 0, 0);

        speedDrawable = getProgressDrawable();
        AppCompatImageView acivSpeed = findViewById(R.id.aciv_speed);
        acivSpeed.setImageDrawable(speedDrawable);

        WifiBean wifiBean = getIntent().getParcelableExtra(kWifiBean);
        if (null != wifiBean) {
            AppCompatTextView actvSsid = findViewById(R.id.actv_ssid);
            actvSsid.setText(wifiBean.getSSID());
        }

        actvSpeed = findViewById(R.id.actv_speed);
        actvTest = findViewById(R.id.actv_retest);
        actvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actvTest.setText(R.string.wifi_speed_retest);
                startTest();
            }
        });
    }

    private ProgressDrawable getProgressDrawable() {
        int ringWidth = SmartUtil.dp2px(0.5f);
        int ringColor = ContextCompat.getColor(this, R.color.default_unenable_color);
        int centerColor = ContextCompat.getColor(this, R.color.speed_center_color);
        return new ProgressDrawable(ringWidth, ringColor, centerColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);

        if (null != speedAnimator && speedAnimator.isRunning()) {
            speedAnimator.cancel();
        }

        if (null != httpCall && !httpCall.isCanceled()) {
            httpCall.cancel();
        }
    }

    private ValueAnimator speedAnimator = null;

    private void startTest() {
        if (null == speedAnimator) {
            speedAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(10 * 1000L);
            speedAnimator.setInterpolator(new DecelerateInterpolator());
            speedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue();
                    speedDrawable.setProgress(progress);
                }
            });

            speedAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isDestroyed() || isFinishing()) {
                        return;
                    }

                    actvTest.setEnabled(true);
                    mHandler.removeCallbacksAndMessages(null);
                    if (null != httpCall && !httpCall.isCanceled()) {
                        httpCall.cancel();
                    }

                    double allByte = 0d;
                    int size = speedList.size();
                    for (int i = 0; i < size; i++) {
                        allByte += speedList.get(i);
                    }
                    double speed = allByte / size;

                    Bundle bundle = new Bundle();
                    bundle.putDouble(SpeedDialog.kSpeed, speed);
                    SpeedDialog speedDialog = new SpeedDialog();
                    speedDialog.setArguments(bundle);
                    speedDialog.show(getSupportFragmentManager(), "");
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    actvTest.setEnabled(false);
                    actvSpeed.setText("");
                    mHandler.removeCallbacksAndMessages(null);
                    speedList.clear();
                    startNetWork();
                }
            });
        }
        speedAnimator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionClassManager.getInstance().register(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectionClassManager.getInstance().remove(listener);
    }

    private ConnectionChangedListener listener = new ConnectionChangedListener();

    private class ConnectionChangedListener implements ConnectionClassManager.ConnectionClassStateChangeListener {
        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == speed_what) {
                if (null != speedAnimator && speedAnimator.isRunning()) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startNetWork();
                        }
                    }, 1000L);
                }
                double speed = (double) msg.obj;
                if (speedList.size() >= 4) {//只取最后四次
                    speedList.remove(0);
                }
                speedList.add(speed);
                actvSpeed.setText(getString(R.string.wifi_speed, decimalFormat.format(speed)));
            }
            return true;
        }
    });

    private void startNetWork() {
        Request.Builder builder = new Request.Builder();
        Request request = builder.url("http://www.baidu.com").build();

        DeviceBandwidthSampler.getInstance().startSampling();
        httpCall = OkHttpUtil.getOkHttpClient().newCall(request);
        httpCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DeviceBandwidthSampler.getInstance().stopSampling();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                DeviceBandwidthSampler.getInstance().stopSampling();
                final double downloadKBitsPerSecond = ConnectionClassManager.getInstance().getDownloadKBitsPerSecond();
                Message message = mHandler.obtainMessage();
                message.what = speed_what;
                message.obj = downloadKBitsPerSecond;
                mHandler.sendMessage(message);
            }
        });
    }

}
