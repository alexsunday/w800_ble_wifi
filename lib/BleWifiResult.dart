class BleWifiResult {
  int code;
  String ipAddress;
  String msg;
  int status;
  String wifiMac;

  BleWifiResult({this.code, this.ipAddress, this.msg, this.status, this.wifiMac});

  factory BleWifiResult.fromJson(Map<String, dynamic> json) {
    return BleWifiResult(
      code: json['code'], 
      ipAddress: json['ipAddress'], 
      msg: json['msg'], 
      status: json['status'], 
      wifiMac: json['wifiMac'], 
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['code'] = this.code;
    data['ipAddress'] = this.ipAddress;
    data['msg'] = this.msg;
    data['status'] = this.status;
    data['wifiMac'] = this.wifiMac;
    return data;
  }
}