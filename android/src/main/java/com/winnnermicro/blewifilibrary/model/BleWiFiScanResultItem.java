package com.winnnermicro.blewifilibrary.model;

import android.bluetooth.le.ScanResult;
import android.util.Log;

import androidx.annotation.Nullable;

public class BleWiFiScanResultItem {
    private static final String TAG = "BleWiFiClient";
    private static final int DATA_TYPE_ORGNANIZATION_CUSTOM_DATA_COMPLETE = 0xFF;
    private ScanResult scanResult;
    private int type;
    private String mac;
    private int version;
    private int orgId;
    private String name;

    public BleWiFiScanResultItem()
    {

    }
    public BleWiFiScanResultItem(ScanResult scanResult)
    {
        name = scanResult.getDevice().getName();
        mac = scanResult.getDevice().getAddress();
        setScanResult(scanResult);
        parseFromBytes(scanResult.getScanRecord().getBytes());
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    private int parseOrgCustomData(byte[] scanRecord, int currentPos, int dataLength)
    {
        if(dataLength < 4)
        {
            return currentPos;
        }
        int oid = scanRecord[currentPos++] & 0xFF;
        oid += (scanRecord[currentPos++] & 0xFF) << 8;
        setOrgId(oid);
        int t = (scanRecord[currentPos++] & 0xFF);
        setType(t);
        int v = (scanRecord[currentPos++] & 0xFF);
        setVersion(v);
        return currentPos;
    }
    private void parseFromBytes(byte[] scanRecord) {
        if (scanRecord == null) {
            return;
        }
//        InputStream inputStream = new ByteArrayInputStream(scanRecord);
//        DataInputStream dis = new DataInputStream(inputStream);
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = scanRecord[currentPos++] & 0xFF;
                switch (fieldType) {
                    case DATA_TYPE_ORGNANIZATION_CUSTOM_DATA_COMPLETE:
                        parseOrgCustomData(scanRecord, currentPos, dataLength);
                        break;
                    default:
                        break;
                }
                currentPos += dataLength;
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record." + e.getMessage());
        }
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        BleWiFiScanResultItem item = null;
        if(obj instanceof BleWiFiScanResultItem)
        {
            item = (BleWiFiScanResultItem)obj;
        }
        if(item == null)
        {
            return false;
        }
        return mac.equals(item.getMac());
    }
}
