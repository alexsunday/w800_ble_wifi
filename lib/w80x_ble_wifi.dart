
import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:w80x_ble_wifi/BleWifiResult.dart';

import 'BleScanResult.dart';
import 'BleWifiParams.dart';
import 'WifiScanResult.dart';
import 'ble_wifi_dialog.dart';

class W80xBleWifi {
  static const EventChannel _bleScanEvt = EventChannel("cn.espush/bleScan");
  static const MethodChannel _stopBleScanCh = MethodChannel("cn.espush.command/bleScan");
  static const EventChannel _bleWiFiEvt = EventChannel("cn.espush/bleWifi");
  static const MethodChannel _wifiScanCh = MethodChannel("cn.espush.command/wifiScan");

  static Stream<BleScanResult> bleScan() async* {
    var bleScanSink = _bleScanEvt.receiveBroadcastStream();
    await for(var chunk in bleScanSink) {
      var item = BleScanResult.fromJson(jsonDecode(chunk as String));
      yield item;
    }
  }

  static Stream<BleWifiResult> bleWiFi(BleWifiParams params) async* {
    var req = jsonEncode(params.toJson());

    var sink = _bleWiFiEvt.receiveBroadcastStream(req);

    await for(var chunk in sink) {
      var item = BleWifiResult.fromJson(jsonDecode(chunk as String));
      yield item;
    }
  }

  static Future<void> stopBleScan() async {
    try {
      print('stop ble scan.');
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

  static Future<BleWifiResult> doBleWifi(BuildContext context, BleWifiParams params) {
    return showDialog<BleWifiResult>(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('网络配置'),
            content: BleWifiDialog(params: params),
          );
        }
    );
  }
}
