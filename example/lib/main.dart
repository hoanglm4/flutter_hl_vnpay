import 'package:flutter/material.dart';
import 'dart:convert';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_hl_vnpay/flutter_hl_vnpay.dart';
import 'package:crypto/crypto.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _paymentResultCodeCode = 'Unknown';

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
   _onBuyCoinPressed() async {
    String paymentResultCodeCode;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      String url = 'http://sandbox.vnpayment.vn/paymentv2/vpcpay.html';
      String tmnCode = 'XXX'; // Get from VNPay
      String hashKey = 'XXX'; // Get from VNPay

      final params = <String, dynamic>{
        'vnp_Command': 'pay',
        'vnp_Amount': '3000000',
        'vnp_CreateDate': '20210315151908',
        'vnp_CurrCode': 'VND',
        'vnp_IpAddr': '192.168.15.102',
        'vnp_Locale': 'vn',
        'vnp_OrderInfo': '5fa66b8b5f376a000417e501 pay coin 30000 VND',
        'vnp_ReturnUrl': 'XXX', // Your Server https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/#code-returnurl
        'vnp_TmnCode': tmnCode,
        'vnp_TxnRef': DateTime
            .now()
            .millisecondsSinceEpoch.toString(),
        'vnp_Version': '2.0.0'
      };

      final sortedParams = FlutterHlVnpay.instance.sortParams(params);
      final hashDataBuffer = new StringBuffer();
      sortedParams.forEach((key, value) {
        hashDataBuffer.write(key);
        hashDataBuffer.write('=');
        hashDataBuffer.write(value);
        hashDataBuffer.write('&');
      });
      final hashData = hashDataBuffer.toString().substring(0, hashDataBuffer.length - 1);
      final query = Uri(queryParameters: sortedParams).query;
      print('hashData = $hashData');
      print('query = $query');

      var bytes = utf8.encode(hashKey + hashData.toString());
      final vnpSecureHash = sha256.convert(bytes);
      final paymentUrl = "$url?$query&vnp_SecureHashType=SHA256&vnp_SecureHash=$vnpSecureHash";
      print('paymentUrl = $paymentUrl');
      paymentResultCodeCode = (await FlutterHlVnpay.instance.show(paymentUrl: paymentUrl, tmnCode: tmnCode, scheme: 'hlsolutions')).toString();
    } on PlatformException {
      paymentResultCodeCode = 'Failed to get payment result code';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _paymentResultCodeCode = paymentResultCodeCode;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Result Code: $_paymentResultCodeCode\n'),
              RaisedButton(
                onPressed: this._onBuyCoinPressed,
                child: Text('10.000 VND'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
