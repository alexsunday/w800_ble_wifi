import 'package:flutter/material.dart';

import 'BleWifiParams.dart';

class BleWifiDialog extends StatefulWidget {
  final BleWifiParams params;
  BleWifiDialog({Key key, @required this.params}): super(key: key);

  @override
  State<StatefulWidget> createState() {
    return _BleWifiDialog();
  }
}

/*
1. 开启蓝牙搜索，打开定时器；蓝牙搜索到结果，关闭定时器；定时器开启，关闭蓝牙搜索；
2. 组装参数，解包蓝牙设备信息，开启配网；
3. 网络成功或失败，都应 Navigator.pop
*/
class _BleWifiDialog extends State<BleWifiDialog> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: Container(
      ),
    );
  }
}
