## Introduction

flutter_hl_vnpay is a VNPay plugin for [Flutter](https://flutter.dev), a new app SDK to help developers build modern multi-platform apps.

## Getting Started
#### **iOS**
In the **ios/Runner/Info.plist** let’s add:

```dart
	<dict>
	     <key>NSAppTransportSecurity</key>
          <dict>
                <key>NSAllowsArbitraryLoads</key>
                <true/>
                <key>NSAllowsArbitraryLoadsInWebContent</key>
                <true/>
          </dict>
```

```dart
   	<key>CFBundleURLTypes</key>
   	<array>
   		<dict>
   			<key>CFBundleTypeRole</key>
   			<string>Editor</string>
   			<key>CFBundleURLName</key>
   			<string>com.hlsolutions.hoishipper</string>
   			<key>CFBundleURLSchemes</key>
   			<array>
   				<string>hlsolutions</string>
   			</array>
   		</dict>
   	</array>

```

#### **Android**

## Usage
#### 1\. Depend
Add this to you package's `pubspec.yaml` file:

```yaml
dependencies:
  flutter_hl_vnpay: ^2.0.0
  crypto: 2.1.5
```
#### 2\. Install
Run command:

```bash
$ flutter pub get
```

#### 3\. Import

Import in Dart code:

```dart
import 'package:flutter/services.dart';
import 'package:flutter_hl_vnpay/flutter_hl_vnpay.dart';
import 'package:crypto/crypto.dart';
```

#### 4\. Using VNPAY
https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/#t%E1%BA%A1o-url-thanh-to%C3%A1n

```dart
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
        'vnp_TxnRef': '604f187c862cd70004478e',
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
      paymentResultCodeCode = (await FlutterHlVnpay.instance.show(paymentUrl: paymentUrl, tmnCode: tmnCode)).toString();
    } on PlatformException {
      paymentResultCodeCode = 'Failed to get payment result code';
    }
```