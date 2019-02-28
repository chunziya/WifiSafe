package com.administrator.wifisafe.ui.dialog;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.administrator.wifisafe.R;
import com.administrator.wifisafe.ui.activity.WifiSpeedActivity;

/**
 * @author lesences  2018/5/27 06:29.
 */
public class SpeedDialog extends DialogFragment {
    public static final String kSpeed = "speed";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return inflater.inflate(R.layout.dialog_speed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle argument = getArguments();
        if (null == argument) {
            return;
        }

        String[] speedStrs = getResources().getStringArray(R.array.speed_option);
        double speed = argument.getDouble(kSpeed, 0d);
        String speedStr = WifiSpeedActivity.decimalFormat.format(speed);

        String speedTitleStr = getString(R.string.wifi_speed_average, speedStr);
        SpannableString spanStr = new SpannableString(speedTitleStr);
        int startIndex = speedTitleStr.lastIndexOf(speedStr);
        int endIndex = startIndex + speedStr.length();
        spanStr.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        AppCompatTextView speedTitle = view.findViewById(R.id.actv_speed);
        speedTitle.setText(spanStr);


        int index = getSpeedIndex(speed);

        AppCompatTextView speedDecr = view.findViewById(R.id.actv_descr);
        speedDecr.setText(speedStrs[index]);
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
        params.gravity = Gravity.CENTER;
        params.width = (int) (displayMetrics.widthPixels * 0.90f);
        window.setAttributes(params);
    }

    private int getSpeedIndex(double speed) {
        int index;
        if (speed > 2000) {
            index = 0;
        } else if (speed > 800) {
            index = 1;
        } else if (speed > 50) {
            index = 2;
        } else {
            index = 3;
        }
        return index;
    }
}
