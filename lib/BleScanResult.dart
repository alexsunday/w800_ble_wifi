class BleScanResult {
  String mac;
  String name;
  String device;

  BleScanResult({this.mac, this.name, this.device});

  factory BleScanResult.fromJson(Map<String, dynamic> json) {
    return BleScanResult(
      mac: json['mac'], 
      name: json['name'],
      device: json['device'],
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['mac'] = this.mac;
    data['name'] = this.name;
    data['device'] = this.device;
    return data;
  }

  @override
  bool operator ==(Object other) {
    if(!(other is BleScanResult)) {
      return false;
    }

    // ignore: test_types_in_equals
    var o = other as BleScanResult;
    return mac == o.mac && name == o.name;
  }

  @override
  int get hashCode {
    return mac.hashCode ^ name.hashCode;
  }
}
