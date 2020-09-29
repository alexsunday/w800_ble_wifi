package com.winnnermicro.blewifilibrary.model;

import android.util.SparseArray;

import com.winnnermicro.blewifilibrary.data.MessagePacket;

public class BleWiFiNegotiateSecretKeyResult extends BleWiFiBaseResult {

    private byte[] encryptedKey;
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public BleWiFiNegotiateSecretKeyResult(SparseArray<byte[]> objects) {
        super(objects);
        byte[] typePayload = objects.get(MessagePacket.PACKET_RESP_TLV_TYPE_PUB_ENC_KEY);
        if(typePayload != null){
            setEncryptedKey(typePayload);
        }
    }
}
