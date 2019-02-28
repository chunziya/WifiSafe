package com.administrator.wifisafe.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author lesences  2018/5/26
 */
public class WifiBean implements Parcelable {
    private String BSSID;//WiFiMac地址(用于区分WiFi)
    private String SSID;//WiFi名称
    private String capabilities;//WiFi加密方式
    private boolean isConnect;
    private int signalLevel;
    private int iconId;

    public WifiBean() {
    }

    protected WifiBean(Parcel in) {
        BSSID = in.readString();
        SSID = in.readString();
        capabilities = in.readString();
        isConnect = in.readByte() != 0;
        signalLevel = in.readInt();
        iconId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BSSID);
        dest.writeString(SSID);
        dest.writeString(capabilities);
        dest.writeByte((byte) (isConnect ? 1 : 0));
        dest.writeInt(signalLevel);
        dest.writeInt(iconId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WifiBean> CREATOR = new Creator<WifiBean>() {
        @Override
        public WifiBean createFromParcel(Parcel in) {
            return new WifiBean(in);
        }

        @Override
        public WifiBean[] newArray(int size) {
            return new WifiBean[size];
        }
    };

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
}
