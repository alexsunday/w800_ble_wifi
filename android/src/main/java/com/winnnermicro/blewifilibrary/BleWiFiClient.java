package com.winnnermicro.blewifilibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.winnnermicro.blewifilibrary.constants.ConfigInfo;
import com.winnnermicro.blewifilibrary.data.MessageEncode;
import com.winnnermicro.blewifilibrary.data.MessagePacket;
import com.winnnermicro.blewifilibrary.model.BleWiFiConfigStaResult;
import com.winnnermicro.blewifilibrary.model.BleWiFiNegotiateSecretKeyResult;
import com.winnnermicro.blewifilibrary.model.BleWiFiStaParams;
import com.winnnermicro.blewifilibrary.model.BleWiFiBaseResult;
import com.winnnermicro.blewifilibrary.utils.HexUtils;
import com.winnnermicro.blewifilibrary.utils.RSAUtils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.core.location.LocationManagerCompat;

public class BleWiFiClient {
    public static final String VERSION = "1.0.1";
    private static final String TAG = "BleWiFiClient";
    private Context mContext;
    private BluetoothDevice mDevice;
    private ExecutorService mThreadPool;
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mWriteChar;
    private final Object mWriteLock;
    private BluetoothGattCharacteristic mNotifyChar;

    private BluetoothGattCallback mInnerGattCallback;
    private volatile BleWiFiCallback mBleWifiCallback;
    private MessageEncode messageEncode;
    private Handler mUIHandler;

    private LinkedBlockingQueue<Integer> mAck;
    private AtomicInteger mCharacterChanged;
    private AtomicInteger mSendSequence;
    private AtomicInteger mRecvSequence;
    private boolean mEncrypt = false;
    private boolean mNeedAck = false;
    private MessageEncode mRecvMessages = null;

    private Map<String, Object> mRsaKeyMap;
    private byte[] mAesKey;
    private Future mCurrentFuture;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private AtomicInteger mTimerCnt;
    private long time_last = 0;
    private int retry = 5;

    public BleWiFiClient(Context context, BluetoothDevice device){
        mContext = context;
        mDevice = device;

        mInnerGattCallback = new InnerGattCallback();
        mThreadPool = Executors.newSingleThreadExecutor();
        mUIHandler = new Handler(Looper.getMainLooper());
        mAck = new LinkedBlockingQueue<>();
        mWriteLock = new Object();
        mCharacterChanged = new AtomicInteger(0);

        mTimerCnt = new AtomicInteger(0);
    }
    private void doTimerTask()
    {
        int cnt = mTimerCnt.getAndDecrement();
        onDebugMessage("timeout " + cnt);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (!adapter.isEnabled() || scanner == null) {
            //Toast.makeText(this, R.string.main_bt_disable_msg, Toast.LENGTH_SHORT).show();
            onError(BleWiFiBaseResult.STATUS_BT_POWER_OFF);
            if (mGatt != null) {
                mGatt.close();
                mGatt = null;
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BleWiFiClient.this.mContext != null) {
            // Check location enable
            LocationManager locationManager = (LocationManager) BleWiFiClient.this.mContext.getSystemService(BleWiFiClient.this.mContext.LOCATION_SERVICE);
            boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
            if (!locationEnable) {
                //Toast.makeText(this, R.string.main_location_disable_msg, Toast.LENGTH_SHORT).show();
                onError(BleWiFiBaseResult.STATUS_LOCATION_DISABLE);
                if (mGatt != null) {
                    mGatt.close();
                    mGatt = null;
                }
                return;
            }
        }
        if(cnt <= 1)
        {
            onError(BleWiFiBaseResult.STATUS_TIMEOUT);
            if (mGatt != null) {
                mGatt.close();
                mGatt = null;
            }
        }
    }
    private void startTimer(int cnt)
    {
        mTimerTask = new TimerTask(){
            @Override
            public void run() {
                doTimerTask();
            }
        };
        mTimer = new Timer();
        mTimerCnt.set(cnt);
        mTimer.schedule(mTimerTask,1000, 1000);
    }
    private void cancelTimer(){
        if(mTimerTask != null) {
            mTimer.cancel();
            mTimerTask = null;
        }
    }
    private void innerConnect() {
        if(retry <= 0) {
            return;
        }
        time_last = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mGatt = mDevice.connectGatt(mContext, false, mInnerGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mGatt = mDevice.connectGatt(mContext, false, mInnerGattCallback);
        }
    }
    public synchronized void connect() {
        if (mThreadPool == null) {
            throw new IllegalStateException("The Client has closed");
        }
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        retry = 5;
        innerConnect();
    }
    public void configureSta(final BleWiFiStaParams params) {
        mCurrentFuture = mThreadPool.submit(new ThrowableRunnable() {
            @Override
            void execute() {
                if(!__configureSta(params))
                {
                     BleWiFiClient.this.onError(BleWiFiBaseResult.STATUS_GATT_WRITE);
                }
                else
                {
                    startTimer(50);
                }
            }
        });
    }
    public void negotiateSecretKey() {
        mCurrentFuture = mThreadPool.submit(new ThrowableRunnable() {
            @Override
            void execute() {
                if(!__negotiateSecretKey())
                {
                    BleWiFiClient.this.onError(BleWiFiBaseResult.STATUS_GATT_WRITE);
                }
                else
                {
                    startTimer(5);
                }
            }
        });
    }
    private boolean __configureSta(BleWiFiStaParams params) {
        try {
            if(!dataPrepare(params))
            {
                return false;
            }
            for (MessagePacket messagePacket : messageEncode.getMessagePacketList()) {
                int seq = mSendSequence.getAndIncrement();
                messagePacket.buildPacket(seq, mNeedAck);
                if(!gattWrite(messagePacket.packageInfo)){
                    return false;
                }

                if(mNeedAck)
                {
                    if(!receiveAck(seq)){
                        return false;
                    }
                }
            }
            return true;
        }
        catch(Exception ex){
            Log.e(TAG, "__configureSta: excpiton " + ex.getMessage());
            return false;
        }
    }
    private boolean __negotiateSecretKey(){
        messageEncode = new MessageEncode();
        try{
            mRsaKeyMap = RSAUtils.initKey();
            messageEncode.putBytesValue(MessagePacket.PACKET_TLV_TYPE_PUB_KEY, RSAUtils.getPublicKey(mRsaKeyMap));
            messageEncode.encode((byte)MessagePacket.PACKET_CMD_KEY_EXCHANGE, false, mAesKey);
            for (MessagePacket messagePacket : messageEncode.getMessagePacketList()) {
                int seq = mSendSequence.getAndIncrement();
                messagePacket.buildPacket(seq, mNeedAck);
                if(!gattWrite(messagePacket.packageInfo)){
                    return false;
                }

                if(mNeedAck)
                {
                    if(!receiveAck(seq)){
                        return false;
                    }
                }
            }
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean receiveAck(int sequence) {
        try {
//            int ack = mAck.take();
            Integer ack = mAck.poll(ConfigInfo.MAX_ACK_TIMEOUT, TimeUnit.SECONDS);
            if(ack == null)
            {
                return false;
            }
            return ack.intValue() == sequence;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }
    /**
     * 传输内容准备
     */
    private boolean dataPrepare(BleWiFiStaParams params) throws UnsupportedEncodingException {
        byte apSSID = 1;
        byte apPASSWD = 2;
        byte apBSSID = 3;
        String bssid = params.getBssid();
        String ssid = params.getSsid();
        String psw = params.getPassword();

        if((bssid == null || bssid.isEmpty()) && (ssid == null || ssid.isEmpty())){
            return false;
        }

//        Collator collator = Collator.getInstance(Locale.CHINA);
        messageEncode = new MessageEncode();
        if (!(ssid == null || ssid.isEmpty())){
            if(ssid.getBytes("UTF-8").length > ConfigInfo.MAX_LENGHT_SSID){
                return false;
            }
            messageEncode.putStringValue(apSSID,ssid);
        }
        if (!(psw == null || psw.isEmpty())){
            if(psw.getBytes().length> ConfigInfo.MAX_LENGHT_PWD){
                return false;
            }
            messageEncode.putStringValue(apPASSWD, psw);
        }
        if (!(bssid == null || bssid.isEmpty())){
            byte[] bssidBytes = new byte[6];
            if(macString2byte(bssid, bssidBytes))
            {
                messageEncode.putBytesValue(apBSSID,bssidBytes);
            }
        }

        //message编码
        messageEncode.encode((byte)MessagePacket.PACKET_CMD_CONFIG_STA, mEncrypt, mAesKey);
        return true;
    }

    private synchronized boolean gattWrite(byte[] data) throws InterruptedException {
        synchronized (mWriteLock) {
            onDebugMessage(">>>: "+HexUtils.formatHexString(data, ','));
            mWriteChar.setValue(data);
            if(!mGatt.writeCharacteristic(mWriteChar)) {
                return false;
            }
            mWriteLock.wait(1000);
            int n = mCharacterChanged.getAndDecrement();
            if(n == 0)
            {
                mCharacterChanged.getAndIncrement();
                return false;
            }
        }
        return true;
    }

    public synchronized void close() {
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
            mThreadPool = null;
        }
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        if (mAck != null) {
            mAck.clear();
            mAck = null;
        }
        mInnerGattCallback = null;
        mBleWifiCallback = null;
        mContext = null;
        mDevice = null;
        mNotifyChar = null;
        mWriteChar = null;
    }

    public void setBleWiFiCallback(BleWiFiCallback callback)
    {
        mBleWifiCallback = callback;
    }

    private void onConnected() {
        startTimer(10);
        if (mBleWifiCallback != null) {
            mBleWifiCallback.onConnected(this);
        }
    }
    private void onDisconnected(){
        cancelTimer();
        if(System.currentTimeMillis() - time_last < 3000) {
            retry--;
            innerConnect();
            return;
        }
        if(mCurrentFuture != null)
        {
            if(!mCurrentFuture.isDone()){
                mCurrentFuture.cancel(true);
            }
        }
        mUIHandler.post(() -> {
            if (mBleWifiCallback != null) {
                mBleWifiCallback.onDisconnected(this);
            }
        });
    }
    private void onServicesCharsDiscovered(){
        cancelTimer();
        if (mBleWifiCallback != null) {
            mBleWifiCallback.onServicesDiscovered(BleWiFiClient.this);
        }
    }
    private void onError(final int errCode) {
        cancelTimer();
        mUIHandler.post(() -> {
            if (mBleWifiCallback != null) {
                mBleWifiCallback.onError(this, errCode);
            }
            if (mGatt != null) {
                mGatt.close();
                mGatt = null;
            }
        });
    }
    private void onConfigStaResult(final BleWiFiConfigStaResult result) {
        cancelTimer();
        mUIHandler.post(() -> {
            if (mBleWifiCallback != null) {
                mBleWifiCallback.onConfigureStaResult(this, result);
            }
            if (mGatt != null) {
                mGatt.close();
                mGatt = null;
            }
        });
    }
    private void onNegotiateSecretKeyResult(final BleWiFiBaseResult result) {
        cancelTimer();
        mUIHandler.post(() -> {
            if (mBleWifiCallback != null) {
                mBleWifiCallback.onNegotiateSecretKeyResult(this, result);
            }
        });
    }
    private void  onDebugMessage(String msg){
        mUIHandler.post(() -> {
            if (mBleWifiCallback != null) {
                mBleWifiCallback.onDebugMessage(msg);
            }
        });
    }
    private boolean macString2byte(String macString, byte[] bytes){
        String[] macStrings = macString.split(":");
        if (macStrings.length != 6){
            return false;
        }

        for (int i = 0; i < 6; i++) {
            try {
                Integer subMacIntegar =  Integer.valueOf(macStrings[i],16);
                bytes[i] = MessagePacket.intToByte(subMacIntegar.intValue());
            }
            catch (NumberFormatException e){
                Log.d("%s",e.getMessage());
                return false;
            }
        }
        return true;
    }
    private void handleRespAck(MessagePacket packet){
        if(packet.payload != null && packet.payload.length == 1){
            mAck.add(packet.payload[0] & 0xFF);
        }
    }

    private void handleCharacteristicData(final byte[] data){
        MessagePacket packet = MessagePacket.parsePacket(data);
        if(packet == null)
        {
            onError(BleWiFiBaseResult.STATUS_INVALID_PARAMS);
            return;
        }
        if(mRecvSequence.getAndIncrement() != packet.seq){
            Log.d(TAG, "Receive invalid sequence!");
            return;
        }
        if(packet.cmd == MessagePacket.PACKET_RESP_CMD_ACK){
            handleRespAck(packet);
            return;
        }
        if(mRecvMessages == null){
            mRecvMessages = new MessageEncode();
        }
        mRecvMessages.addMessagePacket(packet);
        if((packet.flag & MessagePacket.PACKET_FLAG_LAST_PACKET_MASK)  == 0){
            BleWiFiBaseResult result = mRecvMessages.decode(mAesKey);
            mRecvMessages = null;
            switch (packet.cmd & 0xFF){
                case MessagePacket.PACKET_RESP_CMD_CONFIG_STA:
                    onConfigStaResult((BleWiFiConfigStaResult) result);
                    break;
                case MessagePacket.PACKET_RESP_CMD_KEY_EXCHANGE:
                    mAesKey = RSAUtils.decryptByRsaPrikey(mRsaKeyMap, ((BleWiFiNegotiateSecretKeyResult)result).getEncryptedKey());
                    onDebugMessage(String.format("aeskey len %d", mAesKey.length));
                    if(mAesKey == null){
                        onError(BleWiFiBaseResult.STATUS_NEGOTIATE_SECRET_KEY);
                        break;
                    }
                    onDebugMessage("AesKey: "+HexUtils.formatHexString(mAesKey, ','));
                    mEncrypt = true;
                    onNegotiateSecretKeyResult(result);
                    break;
            }
        }
    }

//    private class GattCallback extends BluetoothGattCallback {
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//
//        }
//
//    }
    private class InnerGattCallback extends BluetoothGattCallback {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            Log.d(TAG, String.format(Locale.ENGLISH, "onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED)
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.d(TAG, String.format("Connected %s", devAddr));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        }
                        gatt.discoverServices();
                        onConnected();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.d(TAG, String.format("Disconnected %s", devAddr));
                        gatt.close();
                        onDisconnected();
                        break;
                }
            }
            else {
                Log.d(TAG, String.format("Disconnected %s", devAddr));
                gatt.close();
                onDisconnected();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService service = null;
            BluetoothGattCharacteristic writeChar = null;
            BluetoothGattCharacteristic notifyChar = null;
            Log.d(TAG, String.format("onServicesDiscovered status=%d", status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                service = gatt.getService(ConfigInfo.UUID_SERVICE_WIFT);
                if (service != null) {
                    writeChar = service.getCharacteristic(ConfigInfo.UUID_CHARARCTERISTIC_WRITE);
                    notifyChar = writeChar; //service.getCharacteristic(ConfigInfo.UUID_CHARARCTERISTIC_READ);
                    if (notifyChar != null) {
                        Log.d(TAG, "notifyChar is discovered.");
                        gatt.setCharacteristicNotification(notifyChar, true);

                        mWriteChar = writeChar;
                        mNotifyChar = notifyChar;
                        mSendSequence = new AtomicInteger(0);
                        mRecvSequence = new AtomicInteger(0);
                        mCharacterChanged = new AtomicInteger(0);
                        onServicesCharsDiscovered();
                    }
                }
            }
            else {
                gatt.disconnect();
            }
        }
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic != mWriteChar) {
                return;
            }
            onDebugMessage("recv onCharacteristicWrite");
            synchronized (mWriteLock) {
                mCharacterChanged.getAndIncrement();
                mWriteLock.notify();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic != mNotifyChar) {
                return;
            }
            byte[] data = characteristic.getValue();
            Log.d(TAG,"Receive characteristic change: " + HexUtils.formatHexString(data, ' '));
            onDebugMessage("<<<: "+HexUtils.formatHexString(data, ','));
            handleCharacteristicData(data);
        }
    }

    private abstract class ThrowableRunnable implements Runnable {
        @Override
        public void run() {
            try {
                execute();
            } catch (Exception e) {
                e.printStackTrace();
                onError(e);
            }
        }

        abstract void execute();

        void onError(Exception e) {
        }
    }
}
