package cn.espush.w80x.blewifi.w80x_ble_wifi


import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

/** W80xBleWifiPlugin */
class W80xBleWifiPlugin: FlutterPlugin {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private val bleScanChannel = "cn.espush/bleScan"
  private val stopBleScanChannel = "cn.espush.command/bleScan"
  private val bleWifiChannel = "cn.espush/bleWifi"
  private val stopBleWifiChannel = "cn.espush.command/bleWifi"
  private val wifiScanChannel = "cn.espush.command/wifiScan"

  private lateinit var bleScanEvt: EventChannel
  private lateinit var bleWifiEvt: EventChannel
  private lateinit var stopBleScan: MethodChannel
  private lateinit var stopBleWifi: MethodChannel
  private lateinit var wifiScan: MethodChannel

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    val ctx = binding.applicationContext
    val messagener = binding.binaryMessenger
    // 蓝牙扫描
    val bleScanHandler = BleScanHandler(ctx)
    bleScanEvt = EventChannel(messagener, bleScanChannel)
    bleScanEvt.setStreamHandler(bleScanHandler)
    // 停止扫描命令
    stopBleScan = MethodChannel(messagener, stopBleScanChannel)
    stopBleScan.setMethodCallHandler(bleScanHandler)

    // wifi配网
    val bleWifiHandler = BleWifiHandler(ctx)
    bleWifiEvt = EventChannel(messagener, bleWifiChannel)
    bleWifiEvt.setStreamHandler(bleWifiHandler)
    // 停止配网
    stopBleWifi = MethodChannel(messagener, stopBleWifiChannel)
    stopBleWifi.setMethodCallHandler(bleWifiHandler)

    // wifi 热点扫描
    val wifiScanHandler = WifiScanHandler(ctx)
    wifiScan = MethodChannel(messagener, wifiScanChannel)
    wifiScan.setMethodCallHandler(wifiScanHandler)
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
//  companion object {
//    @JvmStatic
//    fun registerWith(registrar: Registrar) {
//      val channel = MethodChannel(registrar.messenger(), "w80x_ble_wifi")
//      channel.setMethodCallHandler(W80xBleWifiPlugin())
//    }
//  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    bleScanEvt.setStreamHandler(null)
    bleWifiEvt.setStreamHandler(null)
    stopBleScan.setMethodCallHandler(null)
    stopBleWifi.setMethodCallHandler(null)
    wifiScan.setMethodCallHandler(null)
  }
}
