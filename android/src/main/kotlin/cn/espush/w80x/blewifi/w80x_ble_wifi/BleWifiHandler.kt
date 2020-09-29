package cn.espush.w80x.blewifi.w80x_ble_wifi

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.google.gson.Gson
import com.winnnermicro.blewifilibrary.BleWiFiCallback
import com.winnnermicro.blewifilibrary.BleWiFiClient
import com.winnnermicro.blewifilibrary.model.BleWiFiBaseResult
import com.winnnermicro.blewifilibrary.model.BleWiFiConfigStaResult
import com.winnnermicro.blewifilibrary.model.BleWiFiStaParams
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

private class BleWifiParams {
    lateinit var device: String
    lateinit var ssid: String
    var bssid: String? = null
    lateinit var password: String
}

private class BleWifiResult {
    companion object {
        const val Connected = 1
        const val Disconnected = 2
        const val ServiceDiscovered = 3
        const val ConfigureStaResult = 4
        const val NegotiateSecretKeyResult = 5
        const val DebugMessage = 6
        const val Error = 7

        fun viaCode(code: Int): BleWifiResult {
            val rsp = BleWifiResult()
            rsp.code = code
            return rsp
        }

        fun viaCodeStatus(code: Int, status: Int):BleWifiResult {
            val rsp = BleWifiResult()
            rsp.code = code
            rsp.status = status
            return rsp
        }

        fun viaCodeMsg(code: Int, msg: String):BleWifiResult {
            val rsp = BleWifiResult()
            rsp.code = code
            rsp.msg = msg
            return rsp
        }
    }

    var code = 0
    var status = 0
    var msg = ""
    var wifiMac = ""
    var ipAddress = ""
}

class BleWifiHandler(private val context: Context) :EventChannel.StreamHandler, MethodChannel.MethodCallHandler {
    private lateinit var ssid: String
    private var bssid: String? = null
    private lateinit var password: String
    private lateinit var event: EventChannel.EventSink
    private var mWifiClient: BleWiFiClient? = null
    private val gson = Gson()

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        event = events!!
        val args = arguments as String
        val req = gson.fromJson(args, BleWifiParams::class.java)
        connect(req)
    }

    private fun String.fromHex()=this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    // params.device => BluetoothDevice
    private fun connect(params: BleWifiParams) {
        val req = params.device.fromHex()
        ssid = params.ssid
        password = params.password
        if(params.bssid != null && params.bssid!!.isNotEmpty()) {
            bssid = params.bssid
        }

        val device = ParcelableUtil.unMarshall(req, BluetoothDevice.CREATOR)
        val wifiCallback = MyBleWifiCallback()
        print("Device mac: [${device.address}, ssid: ${params.ssid}, pwd: ${params.password}")
        mWifiClient = BleWiFiClient(context, device)
        mWifiClient!!.setBleWiFiCallback(wifiCallback)
        mWifiClient!!.connect()
    }

    private fun stopBleWifi() {
        if(mWifiClient != null) {
            mWifiClient!!.close()
            mWifiClient = null
        }
    }

    override fun onCancel(arguments: Any?) {
        stopBleWifi()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if(call.method == "closeBleWifi") {
            stopBleWifi()
        }
    }

    private inner class MyBleWifiCallback: BleWiFiCallback {
        private fun sendEventChannelMsg(msg: BleWifiResult) {
            val data = gson.toJson(msg)
            context.run {
                event.success(data)
            }
            /*
            val m = context as MainActivity
            m.runOnUiThread {
                event.success(data)
            }
             */
        }

        override fun onConfigureStaResult(client: BleWiFiClient?, result: BleWiFiConfigStaResult?) {
            val msg = BleWifiResult.viaCode(BleWifiResult.ConfigureStaResult)
            if(result != null) {
                msg.status = result.status
                if(result.status == BleWiFiConfigStaResult.STATUS_SUCCESS) {
                    msg.wifiMac = result.mac
                    msg.ipAddress = result.ipAddress
                }
            } else {
                msg.status = -1
            }

            sendEventChannelMsg(msg)
        }

        override fun onNegotiateSecretKeyResult(client: BleWiFiClient?, result: BleWiFiBaseResult?) {
            val msg = BleWifiResult.viaCode(BleWifiResult.NegotiateSecretKeyResult)
            if(result != null) {
                msg.status = result.status
                if(result.status == BleWiFiBaseResult.STATUS_SUCCESS) {
                    // configure sta
                    val staParams = BleWiFiStaParams()
                    staParams.ssid = ssid
                    staParams.password = password
                    if(bssid != null && bssid!!.isNotEmpty()) {
                        staParams.bssid = bssid
                    }
                    mWifiClient!!.configureSta(staParams)
                }
            } else {
                msg.status = -1
            }

            sendEventChannelMsg(msg)
        }

        override fun onDebugMessage(msg: String?) {
            if(msg != null) {
                sendEventChannelMsg(BleWifiResult.viaCodeMsg(BleWifiResult.DebugMessage, msg))
            } else {
                sendEventChannelMsg(BleWifiResult.viaCodeMsg(BleWifiResult.DebugMessage, ""))
            }
        }

        override fun onConnected(client: BleWiFiClient?) {
            sendEventChannelMsg(BleWifiResult.viaCode(BleWifiResult.Connected))
        }

        override fun onServicesDiscovered(client: BleWiFiClient?) {
            sendEventChannelMsg(BleWifiResult.viaCode(BleWifiResult.ServiceDiscovered))
            mWifiClient!!.negotiateSecretKey()
        }

        override fun onDisconnected(client: BleWiFiClient?) {
            sendEventChannelMsg(BleWifiResult.viaCode(BleWifiResult.Disconnected))
        }

        override fun onError(client: BleWiFiClient?, errCode: Int) {
            sendEventChannelMsg(BleWifiResult.viaCodeStatus(BleWifiResult.Error, errCode))
        }
    }
}
