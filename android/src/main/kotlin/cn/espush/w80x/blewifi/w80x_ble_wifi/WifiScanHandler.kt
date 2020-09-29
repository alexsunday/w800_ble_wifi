package cn.espush.w80x.blewifi.w80x_ble_wifi

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import com.google.gson.Gson
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.*

private class WifiScanResult {
    var ssid = ""
    var bssid = ""
    var capabilities = ""
    var level = 0
    var freq = 0
}

class WifiScanHandler(private val context: Context) : MethodChannel.MethodCallHandler {
    private val gson = Gson()

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if(call.method == "wifiScan") {
            onWifiScan(call, result)
        } else {
            result.error("1", "unknown method", null)
        }
    }

    private fun onWifiScan(call: MethodCall, result: MethodChannel.Result) {
        assert(call.method == "wifiScan")

        val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled) {
            result.error("2", "wifi disabled", null)
            return;
        }
        
        val results = wifiManager.scanResults
        val rs = LinkedList<WifiScanResult>()
        for(item in results) {
            val cur = WifiScanResult()
            cur.ssid = item.SSID
            cur.bssid = item.BSSID
            cur.capabilities = item.capabilities
            cur.level = item.level
            cur.freq = item.frequency
            rs.add(cur)
        }

        result.success(gson.toJson(rs))
    }
}
