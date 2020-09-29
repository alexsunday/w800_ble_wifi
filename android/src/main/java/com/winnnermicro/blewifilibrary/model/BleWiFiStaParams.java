package com.winnnermicro.blewifilibrary.model;

import java.io.Serializable;

public class BleWiFiStaParams implements Serializable {
    private String ssid;
    private String password;
    private String bssid;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }
}
