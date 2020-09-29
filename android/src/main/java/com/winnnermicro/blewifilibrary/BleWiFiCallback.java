package com.winnnermicro.blewifilibrary;

import com.winnnermicro.blewifilibrary.model.BleWiFiBaseResult;
import com.winnnermicro.blewifilibrary.model.BleWiFiConfigStaResult;

public interface BleWiFiCallback {

    void onConnected(BleWiFiClient client);
    void onDisconnected(BleWiFiClient client);
    void onServicesDiscovered(BleWiFiClient client);
    void onConfigureStaResult(BleWiFiClient client, BleWiFiConfigStaResult result);
    void onError(BleWiFiClient client, int errCode);
    void onNegotiateSecretKeyResult(BleWiFiClient client, BleWiFiBaseResult result);
    void onDebugMessage(String msg);
}
