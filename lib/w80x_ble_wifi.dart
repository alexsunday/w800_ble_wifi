
import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

import 'BleWifiParams.dart';
import 'WifiScanResult.dart';

class W80xBleWifi {
  static const EventChannel _bleScanEvt = EventChannel("cn.espush/bleScan");
  static const MethodChannel _stopBleScanCh = MethodChannel("cn.espush.command/bleScan");
  static const EventChannel _bleWiFiEvt = EventChannel("cn.espush/bleWifi");
  static const MethodChannel _wifiScanCh = MethodChannel("cn.espush.command/wifiScan");

  static Stream<dynamic> bleScan() {
    var bleScanSink = _bleScanEvt.receiveBroadcastStream();
    return bleScanSink;
  }

  static Stream<dynamic> bleWiFi(BleWifiParams params) {
    var req = jsonEncode(params.toJson());

    var sink = _bleWiFiEvt.receiveBroadcastStream(req);
    print('submit succeed.');
    return sink;
  }

  static Future<void> stopBleScan() async {
    try {
      await _stopBleScanCh.invokeMethod("stopBleScan");
    } on PlatformException {
      print("stop ble scan, but recv exception.");
    }
  }

  static Future<List<WifiScanResult>> wifiScan() async {
    var rs = await _wifiScanCh.invokeMethod("wifiScan");
    var source = rs as String;
    List<dynamic> rsList = jsonDecode(source);
    var out = List<WifiScanResult>();
    for(var item in rsList) {
      out.add(WifiScanResult.fromJson(item));
    }

    return out;
  }
}
