import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:w80x_ble_wifi/w80x_ble_wifi.dart';

void main() {
  const MethodChannel channel = MethodChannel('w80x_ble_wifi');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });
}
