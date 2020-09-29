import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:w80x_ble_wifi/WifiScanResult.dart';
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
  List<WifiScanResult> mWifiList = [];

  Widget buildInputForm() {
//    _ssid.text = "Chinanet-96c";
//    _pwd.text = "2zhlmcl1hblsqt";

    _ssid.text = "2208-WiFi";
    _pwd.text = "12345678";

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(40, 0, 40, 0),
          child: TextField(
            controller: _ssid,
            decoration: InputDecoration(
              labelText: "SSID",
              suffixIcon: PopupMenuButton<String>(
                icon: const Icon(Icons.arrow_drop_down),
                onSelected: (String v) {
                  _ssid.text = v;
                },
                itemBuilder: (BuildContext context) {
                  return mWifiList.map<PopupMenuItem<String>>((e) {
                    return PopupMenuItem<String>(
                      child: Text(e.ssid),
                    );
                  }).toList();
                },
              )
            ),
          )
        ),
        Padding(
          padding: const EdgeInsets.fromLTRB(40, 0, 40, 0),
          child: TextField(
            controller: _pwd,
            obscureText: true,
            decoration: InputDecoration(
              labelText: "密码"
            ),
          ),
        ),
      ],
    );
  }

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
            buildInputForm(),
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
    mWifiList = rs;
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

  void ssidSelected(String value) {
  }
}
