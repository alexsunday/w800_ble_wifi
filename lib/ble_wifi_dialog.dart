import 'dart:async';

import 'package:flutter/material.dart';

import 'BleWifiParams.dart';
import 'w80x_ble_wifi.dart';

class BleWifiDialog extends StatefulWidget {
  final BleWifiParams params;
  BleWifiDialog({Key key, @required this.params}): super(key: key);

  @override
  State<StatefulWidget> createState() {
    return _BleWifiDialog();
  }
}

enum BleWifiState {
  prepare,
  bleScanning,
  bleScanFinished,
  bleScanFailed,
  bleConnecting,
  bleConnected,
  bleDisconnected,
  devServiceDiscovered,
  devExchangeFailed,
  devExchangeFinished,
  staConfigureFinished,
  staConfFailed,
}
/*
1. 开启蓝牙搜索，打开定时器；蓝牙搜索到结果，关闭定时器；定时器开启，关闭蓝牙搜索；
2. 组装参数，解包蓝牙设备信息，开启配网；
3. 网络成功或失败，都应 Navigator.pop
*/
class _BleWifiDialog extends State<BleWifiDialog> {
  var _curState = BleWifiState.prepare;
  var _elapsed = 0.0;
  var _timer;

  static const Connected = 1;
  static const Disconnected = 2;
  static const ServiceDiscovered = 3;
  static const ConfigureStaResult = 4;
  static const NegotiateSecretKeyResult = 5;
  static const DebugMessage = 6;
  static const Error = 7;

  @override
  void initState() {
    super.initState();
    Future.delayed(Duration.zero, beginBleScan);
    _timer = Timer.periodic(Duration(milliseconds: 100), (_) {
      setState(() {
        _elapsed += 0.1;
      });
    });
  }

  @override
  void dispose() {
    W80xBleWifi.stopBleScan();
    if(_timer != null) {
      _timer.cancel();
    }
    super.dispose();
  }

  /// 1~10 秒内，扫描蓝牙，若扫描到，执行配网
  /// 10 秒后，若未扫描到蓝牙，应界面提示 蓝牙扫描超时 整个流程可以重启
  /// 配网 20 秒超时
  Future<void> beginBleScan() async {
    print('ble scan begin.');
    var bleSink = W80xBleWifi.bleScan();
    setState(() {
      _curState = BleWifiState.bleScanning;
    });
    bleSink.listen((event) {
      // 收到后，关闭蓝牙扫描
      setState(() {
        _curState = BleWifiState.bleScanFinished;
      });
      print('scan get result ${event.name}: ${event.mac}');
      W80xBleWifi.stopBleScan();
      // 组装数据 发送至配网 连接
      var params = BleWifiParams();
      params.device = event.device;
      params.ssid = widget.params.ssid;
      params.password = widget.params.password;

      var wifiSink = W80xBleWifi.bleWiFi(params);
      wifiSink.listen((event) {
        var code = event.code;
        print('recv code: $code');
        if(code == Connected) {
          setState(() {
            _curState = BleWifiState.bleConnected;
          });
        } else if(code == Disconnected) {
          setState(() {
            _curState = BleWifiState.bleDisconnected;
          });
        } else if(code == ServiceDiscovered) {
          setState(() {
            _curState = BleWifiState.devServiceDiscovered;
          });
        } else if(code == ConfigureStaResult) {
          print('sta result status: ${event.status}');
          setState(() {
            if(event.status == 0) {
              _curState = BleWifiState.staConfigureFinished;
            } else {
              _curState = BleWifiState.staConfFailed;
            }
          });
        } else if(code == NegotiateSecretKeyResult) {
          print('status: ${event.status}');
          setState(() {
            if(event.status == 0) {
              _curState = BleWifiState.devExchangeFinished;
            } else {
              _curState = BleWifiState.devExchangeFailed;
            }
          });
        } else if(code == DebugMessage) {
          print('recv message: ${event.msg}');
        } else if(code == Error) {
          setState(() {
            _curState = BleWifiState.staConfFailed;
          });
        } else {
          print('unknown message!!!!! $code');
        }
      }, onError: (e) {
        setState(() {
          _curState = BleWifiState.staConfFailed;
        });
        print('wifi sink error!');
      });
    }, onError: (e) {
      print('ble scan on error!');
      print(e);
      setState(() {
        _curState = BleWifiState.bleScanFailed;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    var elapsed = ((_elapsed * 10).floor())/10.0;
    var curState = getStateMessage(_curState);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Image.asset(
          'res/refresh.png',
          package: 'w80x_ble_wifi',
          width: 64,
          height: 64,
        ),
        Text(
          curState,
          style: TextStyle(
            fontSize: 20
          ),
        ),
        Text(
          "$elapsed"
        ),
      ],
    );
  }

  String getStateMessage(BleWifiState s) {
    const m = <BleWifiState, String>{
      BleWifiState.prepare: "准备中...",

      BleWifiState.bleScanning: "蓝牙扫描进行中...",
      BleWifiState.bleScanFinished: "蓝牙扫描已完成",
      BleWifiState.bleScanFailed: "蓝牙扫描失败",

      BleWifiState.bleConnecting: "设备连接进行中...",
      BleWifiState.bleConnected: "蓝牙已连接",
      BleWifiState.bleDisconnected: "蓝牙连接已断开",

      BleWifiState.devServiceDiscovered: "发现设备服务已完成",
      BleWifiState.devExchangeFailed: "设备密钥交换失败",
      BleWifiState.devExchangeFinished: "设备密钥交换已完成",
      BleWifiState.staConfigureFinished: "设备网络配置已完成",
      BleWifiState.staConfFailed: "网络配置失败",
    };

    return m[s];
  }
}
