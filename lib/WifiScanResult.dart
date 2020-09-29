class WifiScanResult {
  String bssid;
  String capabilities;
  int freq;
  int level;
  String ssid;

  WifiScanResult({this.bssid, this.capabilities, this.freq, this.level, this.ssid});

  factory WifiScanResult.fromJson(Map<String, dynamic> json) {
    return WifiScanResult(
      bssid: json['bssid'], 
      capabilities: json['capabilities'], 
      freq: json['freq'], 
      level: json['level'], 
      ssid: json['ssid'], 
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['bssid'] = this.bssid;
    data['capabilities'] = this.capabilities;
    data['freq'] = this.freq;
    data['level'] = this.level;
    data['ssid'] = this.ssid;
    return data;
  }
}