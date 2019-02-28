package com.administrator.wifisafe.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.bean.CheckBean;
import com.administrator.wifisafe.bean.WifiBean;
import com.administrator.wifisafe.ui.adapter.WifiCheckAdapter;
import com.administrator.wifisafe.util.SmartUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lesences  2018/5/26
 */
public class WifiCheckActivity extends AppCompatActivity {
    private final static int check_what = 666;
    private final static String kWifiBean = "wifi_bean";

    public static void startWifiCheckActivity(Context packageContext, WifiBean wifiBean) {
        Intent intent = new Intent(packageContext, WifiCheckActivity.class);
        intent.putExtra(kWifiBean, wifiBean);
        packageContext.startActivity(intent);
    }

    private WifiCheckAdapter checkAdapter = null;
    private List<CheckBean> checkList = new ArrayList<>();
    private String[] checkOptions;
    private int optionIndex = 0;
    private WifiBean wifiBean;

    private View viewTop;
    private AppCompatTextView actvCheck;
    private AppCompatImageView acivWifi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_check);
        viewTop = findViewById(R.id.view_top);
        acivWifi = findViewById(R.id.aciv_wifi);
        actvCheck = findViewById(R.id.actv_check);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ConstraintLayout.LayoutParams toolbarLp = (ConstraintLayout.LayoutParams) toolbar.getLayoutParams();
        toolbarLp.setMargins(0, SmartUtil.getStatusHeight(), 0, 0);

        wifiBean = getIntent().getParcelableExtra(kWifiBean);

        RecyclerView rvCheck = findViewById(R.id.rv_check);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCheck.setLayoutManager(layoutManager);

        checkAdapter = new WifiCheckAdapter(this, checkList);
        rvCheck.setAdapter(checkAdapter);

        checkOptions = getResources().getStringArray(R.array.check_option);

        mHandler.sendEmptyMessage(check_what);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(check_what);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case check_what:
                    if (checkList.size() > 0) {
                        CheckBean lastBean = checkList.remove(0);
                        lastBean.setCheckState(getRandomState(lastBean.getOptionIndex()));
                        checkList.add(0, lastBean);

                        if (null != checkAdapter) {
                            checkAdapter.notifyItemChanged(0);
                        }
                    }
                    if (optionIndex >= checkOptions.length) {
                        //检测结束
                        checkFinish();
                        break;
                    }

                    CheckBean checkBean = new CheckBean();
                    checkBean.setOptionIndex(optionIndex);
                    checkBean.setCheckState(CheckBean.CheckState.CHECKING);
                    checkBean.setOptionStr(checkOptions[optionIndex]);

                    checkList.add(0, checkBean);
                    if (null != checkAdapter) {
                        checkAdapter.notifyItemInserted(0);
                    }
                    optionIndex++;
                    long delayMillis = (long) (1500 * (0.3d + Math.random()));
                    mHandler.sendEmptyMessageDelayed(check_what, delayMillis);
                    break;
            }
            return true;
        }
    });

    private CheckBean.CheckState getRandomState(int optionIndex) {
        if (null == wifiBean) {
            return CheckBean.CheckState.UNSAFE;
        }
        CheckBean.CheckState checkState;
        String capbilities = wifiBean.getCapabilities();
        int baseValue;
        switch (optionIndex) {
            default:
            case 0:
                baseValue = 1000;
                break;
            case 1:
                baseValue = 200;
                break;
            case 2:
                baseValue = 500;
                break;
            case 3:
                if (TextUtils.isEmpty(capbilities)) {
                    return CheckBean.CheckState.WARN;
                } else {
                    baseValue = getBaseValue(capbilities);
                    if (baseValue < 0) {
                        return CheckBean.CheckState.SAFE;
                    } else if (baseValue == 0) {
                        return CheckBean.CheckState.WARN;
                    }
                }
                break;
        }
        int random = (int) (baseValue * Math.random());
        if (random < 3) {
            checkState = CheckBean.CheckState.UNSAFE;
        } else if (random < 15) {
            checkState = CheckBean.CheckState.WARN;
        } else {
            checkState = CheckBean.CheckState.SAFE;
        }
        return checkState;
    }

    private int getBaseValue(String capbilities) {
        if (capbilities.contains("PSK")) {
            return 100000;
        } else if (capbilities.contains("WPA")) {
            return 50000;
        } else if (capbilities.contains("WEP")) {
            return 1000;
        } else if (capbilities.contains("EAP")) {
            return -1;
        } else {
            return 0;
        }
    }

    private void checkFinish() {

        int bgColor;
        int textId;
        boolean selected;
        if (isUnsafe()) { //检测发现不安全
            selected = true;
            bgColor = R.color.unsafe_color;
            textId = R.string.wifi_unsafe_tip;
        } else if (isWarn()) { //检测发现异常
            selected = true;
            bgColor = R.color.default_color;
            textId = R.string.wifi_warn_tip;
        } else { //全部安全
            selected = false;
            bgColor = R.color.welcome_tip_color;
            textId = R.string.wifi_safe_tip;

        }
        acivWifi.setSelected(selected);
        acivWifi.setVisibility(View.VISIBLE);
        viewTop.setBackgroundResource(bgColor);
        actvCheck.setText(textId);
    }

    private boolean isUnsafe() {
        for (CheckBean checkBean : checkList) {
            if (checkBean.getCheckState() == CheckBean.CheckState.UNSAFE) {
                return true;
            }
        }
        return false;
    }

    private boolean isWarn() {
        for (CheckBean checkBean : checkList) {
            if (checkBean.getCheckState() == CheckBean.CheckState.WARN) {
                return true;
            }
        }
        return false;
    }
}
