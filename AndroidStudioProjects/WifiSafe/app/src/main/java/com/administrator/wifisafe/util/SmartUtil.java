package com.administrator.wifisafe.util;

import android.content.res.Resources;

import java.util.Collection;

/**
 * @author lesences  2018/5/26
 */
public final class SmartUtil {

    /**
     * 获得状态栏的高度
     */
    public static int getStatusHeight() {
        int statusHeight = -1;
        int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusHeight = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return statusHeight;
    }

    /**
     * 判断集合是否为null或empty
     */
    public static boolean isNullOrEmpty(Collection c) {
        return null == c || c.isEmpty();
    }

    /**
     * dp 转换成px
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
