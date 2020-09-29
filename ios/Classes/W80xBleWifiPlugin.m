#import "W80xBleWifiPlugin.h"
#if __has_include(<w80x_ble_wifi/w80x_ble_wifi-Swift.h>)
#import <w80x_ble_wifi/w80x_ble_wifi-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "w80x_ble_wifi-Swift.h"
#endif

@implementation W80xBleWifiPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftW80xBleWifiPlugin registerWithRegistrar:registrar];
}
@end
