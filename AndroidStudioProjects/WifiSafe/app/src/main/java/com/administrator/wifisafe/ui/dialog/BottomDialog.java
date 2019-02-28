package com.administrator.wifisafe.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.bean.WifiBean;
import com.administrator.wifisafe.ui.activity.WifiCheckActivity;
import com.administrator.wifisafe.ui.activity.WifiSpeedActivity;

/**
 * @author lesences  2018/5/27
 */
public class BottomDialog extends DialogFragment implements View.OnClickListener {
    public static final String kWifiBean = "wifi_bean";
    private WifiBean wifiBean;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return inflater.inflate(R.layout.dialog_bottom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle argument = getArguments();
        if (null == argument) {
            dismiss();
            return;
        }
        wifiBean = argument.getParcelable(kWifiBean);
        if (null == wifiBean) {
            dismiss();
            return;
        }
        AppCompatTextView actvSsid = view.findViewById(R.id.actv_ssid);
        actvSsid.setText(wifiBean.getSSID());
        view.findViewById(R.id.actv_security_test).setOnClickListener(this);
        view.findViewById(R.id.actv_speed_test).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (null == window) {
            return;
        }
        WindowManager.LayoutParams params = window.getAttributes();
        window.setWindowAnimations(R.style.dialog_down_up);
        DisplayMetrics displayMetrics = requireContext().getResources().getDisplayMetrics();
        params.gravity = Gravity.BOTTOM;
        params.width = displayMetrics.widthPixels;
        window.setAttributes(params);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actv_security_test:
                WifiCheckActivity.startWifiCheckActivity(requireContext(), wifiBean);
                dismiss();
                break;
            case R.id.actv_speed_test:
                WifiSpeedActivity.startWifiSpeedActivity(requireContext(), wifiBean);
                dismiss();
                break;
        }
    }
}
