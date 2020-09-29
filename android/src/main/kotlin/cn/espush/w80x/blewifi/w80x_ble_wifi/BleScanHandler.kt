package cn.espush.w80x.blewifi.w80x_ble_wifi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.location.LocationManagerCompat
import com.google.gson.Gson
import com.winnnermicro.blewifilibrary.model.BleWiFiScanResultItem
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

private class BleScanResult {
    lateinit var name: String
    lateinit var mac: String
    lateinit var device: String
}

/*
蓝牙扫描
 */
class BleScanHandler(private val context: Context): EventChannel.StreamHandler, MethodChannel.MethodCallHandler {
    private val mScanCallback = ScanCallback()
    private lateinit var event: EventChannel.EventSink

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = adapter.bluetoothLeScanner
        event = events!!

        if(!adapter.isEnabled) {
            //error
            event.error("0", "adapter enable failed", null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location enable
            val locationManager = getSystemService(context, LocationManager::class.java) as LocationManager
            val locationEnable = LocationManagerCompat.isLocationEnabled(locationManager)
            if (!locationEnable) {
                event.error("1", "location disabled", null)
                return
            }
        }

        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, scanSettings, mScanCallback)
    }

    private fun stopScan() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = adapter.bluetoothLeScanner
        scanner!!.stopScan(mScanCallback)
    }

    override fun onCancel(arguments: Any?) {
        stopScan()
    }

    private inner class ScanCallback: android.bluetooth.le.ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            event.error("2", "scan failed.", null)
            super.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            scanResultProc(result!!)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            for (item in results!!) {
                scanResultProc(item)
            }
        }

        private fun ByteArray.toHex()=this.joinToString(""){ String.format("%02X",(it.toInt() and 0xFF)) }

        private fun scanResultProc(item: ScanResult) {
            val result = BleWiFiScanResultItem(item)
            if("070C" == String.format("%04X", result.orgId)) {
                val obj = BleScanResult()
                obj.mac = result.mac
                obj.name = result.name
                obj.device = ParcelableUtil.marshall(item.device).toHex()
                val gson = Gson()
                event.success(gson.toJson(obj))
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if(call.method == "stopBleScan") {
            print("Stop scan!")
            stopScan()
//            event.endOfStream()
        }
    }
}
