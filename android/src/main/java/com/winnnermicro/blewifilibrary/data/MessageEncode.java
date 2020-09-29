package com.winnnermicro.blewifilibrary.data;

import android.util.Log;
import android.util.SparseArray;

import com.winnnermicro.blewifilibrary.model.BleWiFiConfigStaResult;
import com.winnnermicro.blewifilibrary.model.BleWiFiBaseResult;
import com.winnnermicro.blewifilibrary.model.BleWiFiNegotiateSecretKeyResult;
import com.winnnermicro.blewifilibrary.utils.AESUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MessageEncode {
    private static final String TAG = "BleWiFiClient";
    private ArrayList<MessagePacket> messagePacketList = new ArrayList<>();

    private static final int PACKETMAXLENGTH800 = 14;
    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private SparseArray<byte[]> mObjects;
    private int mTotalValues = 0;
    private int mTotalKeys = 0;
    private ArrayList<byte[]> packDataList = new ArrayList<byte[]>();


    public MessageEncode() {
        mObjects = new SparseArray<byte[]>();
    }

    public void serialize800(boolean encrypt, byte[] aesKey) {
        int i = 0;
        int offset = 0;
        if (mObjects.size() == 0){return;}
        int tlen = mTotalKeys * 2 + mTotalValues;
        byte[] tBytes = new byte[tlen];

        for(i = 0; i < mTotalKeys; i++) {
            int key = mObjects.keyAt(i);
            byte[] curObject = mObjects.get(key);
            byte type   = MessagePacket.intToByte(key);
            byte length = MessagePacket.intToByte(curObject.length);
            tBytes[offset] = type;
            offset += 1;
            tBytes[offset] = length;
            offset += 1;
            System.arraycopy(curObject, 0, tBytes, offset, curObject.length);
            offset += curObject.length;
        }
        if(encrypt)
        {
            tBytes = AESUtils.Encrypt(tBytes, aesKey);
            if(tBytes == null){
                return;
            }
            tlen = tBytes.length;
        }
        offset = 0;
        while(tlen > 0) {
            int l = tlen;
            if(l > PACKETMAXLENGTH800)
            {
                l = PACKETMAXLENGTH800;
            }
            byte[] pl = new byte[l];
            System.arraycopy(tBytes, offset, pl, 0, l);
            packDataList.add(pl);
            offset += l;
            tlen -= l;
        }
    }

    public void putStringValue(int type,String value) {
        putBytesValue(type,value.getBytes());
    }

    public void putBytesValue(int type,byte[] value) {
        mObjects.put(type, value);
        mTotalValues += value.length;
        mTotalKeys++;
    }

    public ArrayList<MessagePacket> getMessagePacketList(){
        return messagePacketList;
    }

    public void addMessagePacket(MessagePacket packet){
        if(packet == null){
            return;
        }
        messagePacketList.add(packet);
    }
    /**
     * 组包分片
     */
    public void encode(byte cmd, boolean encrypt, byte[] aesKey){
        // 获取tlv内容
        serialize800(encrypt, aesKey);
        int i = 0;
        for (byte[] curBytes: packDataList) {
            MessagePacket pMessagePacket = new MessagePacket();
            pMessagePacket.cmd = cmd; //Integer.valueOf("0A", 16).byteValue();
//            pMessagePacket.org_id = Integer.valueOf("CC", 16).byteValue();
//            pMessagePacket.length = MessagePacket.intToByte(1);
            pMessagePacket.seq = 0;
            pMessagePacket.flag = i + 1 < packDataList.size() ? (byte) MessagePacket.PACKET_FLAG_LAST_PACKET_MASK: 0;
            if(encrypt){
                pMessagePacket.flag |= MessagePacket.PACKET_FLAG_ENCRYPT_MASK;
            }
            pMessagePacket.no = MessagePacket.intToByte(i);
            pMessagePacket.payload = curBytes;
            messagePacketList.add(pMessagePacket);
//            Log.d(TAG,"current packet: " + HexUtils.formatHexString(pMessagePacket.packageInfo, true));
            i++;
        }
    }

    public BleWiFiBaseResult decode(byte[] aesKey){
        byte flag = 0;
        BleWiFiBaseResult result = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (MessagePacket packet : messagePacketList) {
                outputStream.write(packet.payload);
                flag = packet.flag;
            }
            byte[] allData = outputStream.toByteArray();
            if((flag & MessagePacket.PACKET_FLAG_ENCRYPT_MASK) != 0){
                allData = AESUtils.Decrypt(allData, aesKey);
                if(allData == null){
                    return null;
                }
            }
            int i = 0;
            mObjects.clear();
            while (i < allData.length){
                int type = allData[i++] & 0xFF;
                int length = allData[i++] & 0xFF;
                if(allData.length >= i + length)
                {
                    byte[] typePayload = new byte[length];
                    System.arraycopy(allData, i, typePayload, 0, length);
                    putBytesValue(type, typePayload);
                    i += length;
                }
                else
                {
                    return null;
                }
            }

            switch (messagePacketList.get(0).cmd & 0xFF){
                case MessagePacket.PACKET_RESP_CMD_CONFIG_STA:
                    result = new BleWiFiConfigStaResult(mObjects);
                    break;
                case MessagePacket.PACKET_RESP_CMD_KEY_EXCHANGE:
                    result = new BleWiFiNegotiateSecretKeyResult(mObjects);
                    break;
            }
        }
        catch (IOException ex){
            Log.e(TAG, ex.getMessage());
            return null;
        }
        finally {
            try {
                outputStream.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

}
