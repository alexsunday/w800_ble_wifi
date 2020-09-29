class BleWifiParams {
  String device;
  String password;
  String ssid;

  BleWifiParams({this.device, this.password, this.ssid});

  factory BleWifiParams.fromJson(Map<String, dynamic> json) {
    return BleWifiParams(
      device: json['device'], 
      password: json['password'], 
      ssid: json['ssid'], 
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['device'] = this.device;
    data['password'] = this.password;
    data['ssid'] = this.ssid;
    return data;
  }
}