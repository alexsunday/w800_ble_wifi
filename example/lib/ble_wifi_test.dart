import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:w80x_ble_wifi/w80x_ble_wifi.dart';

void showToast(String msg) {
  Fluttertoast.showToast(msg: msg);
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePage createState() => _MyHomePage();
}

class _MyHomePage extends State<MyHomePage> {
  var _ssid = TextEditingController();
  var _pwd = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("BleWifi"),
        elevation: 0.0,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(),
            Padding(
              padding: const EdgeInsets.fromLTRB(10, 0, 10, 0),
              child: TextField(),
            ),
            RaisedButton(
              child: Text('WiFi搜索'),
              onPressed: wifiScan,
            ),
            RaisedButton(
              child: Text('权限申请'),
              onPressed: permRequest,
            ),
            RaisedButton(
              child: Text('蓝牙搜索'),
              onPressed: bleScan,
            ),
            RaisedButton(
              child: Text('停止搜索'),
              onPressed: stopBleScan,
            ),
            RaisedButton(
              child: Text('开始配网'),
              onPressed: bleWiFi,
            ),
            RaisedButton(
              child: Text('一发入魂'),
              onPressed: goOneShot,
            )
          ],
        ),
      ),
    );
  }

  Future<void> permRequest() async {
    var status = await Permission.location.status;
    if(!status.isGranted) {
      await [Permission.location, Permission.storage].request();
    }

    status = await Permission.location.status;
    if(status.isGranted){
      showToast('已授权!');
    }

    if(status.isPermanentlyDenied) {
      showToast('用户已永久拒绝!');
    }
    if(status.isDenied) {
      showToast('用户已拒绝!');
    }
  }

  Future<void> wifiScan() async {
    var rs = await W80xBleWifi.wifiScan();
    showToast("扫描到wifi热点共 ${rs.length} 个");
  }

  void onBleItemScan(Object key) {
    //
  }

  void bleScan() {
    var sink = W80xBleWifi.bleScan();
    sink.listen(onBleItemScan, onError: (error) {});
  }

  void stopBleScan() {
  }

  void bleWiFi() {
  }

  void goOneShot() {
  }
}
