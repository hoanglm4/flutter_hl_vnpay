
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterHlVnpay {
  final MethodChannel _channel = const MethodChannel('flutter_hl_vnpay');
  final StreamController<MethodCall> _methodStreamController = new StreamController.broadcast();
  Stream<MethodCall> get _methodStream => _methodStreamController.stream;

  FlutterHlVnpay._() {
    _channel.setMethodCallHandler((MethodCall call) async {
      print('[setMethodCallHandler] call = $call');
      _methodStreamController.add(call);
    });
  }

  static FlutterHlVnpay _instance = new FlutterHlVnpay._();
  static FlutterHlVnpay get instance => _instance;

  Future<int?> show({
    required String paymentUrl,
    required String tmnCode,
    String scheme = '',
    bool isSandbox = true,
    String backAlert = 'Bạn có chắc chắn trở lại ko?',
    String title = 'Nạp tiền qua VNPay',
    String iconBackName = 'AppIcon',
    String beginColor = '#00B14F',
    String endColor = '#00B14F',
    String titleColor = '#FFFFFF',
  }) async {

    final params = <String, dynamic>{
      "scheme": scheme,
      "isSandbox": isSandbox,
      "paymentUrl": paymentUrl,
      "tmn_code": tmnCode,
      "backAlert": backAlert,
      "title": title,
      "iconBackName": iconBackName,
      "beginColor": beginColor,
      "endColor": endColor,
      "titleColor": titleColor,
    };
    await _channel.invokeMethod('show', params);
    await for (MethodCall m in _methodStream) {
      if (m.method == "PaymentBack") {
        return m.arguments['resultCode'] as int?;
      }
    }
  }

  Map<String, dynamic> sortParams(Map<String, dynamic> params) {
    final sortedParams = <String, dynamic>{};
    final keys = params.keys.toList()..sort();
    for (String key in keys) {
      sortedParams[key] = params[key];
    }
    return sortedParams;
  }
}
