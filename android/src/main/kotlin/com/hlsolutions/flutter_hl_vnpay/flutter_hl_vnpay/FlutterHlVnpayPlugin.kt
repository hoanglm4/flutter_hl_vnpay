package com.hlsolutions.flutter_hl_vnpay.flutter_hl_vnpay

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.annotation.NonNull
import com.vnpay.authentication.VNP_AuthenticationActivity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** FlutterHlVnpayPlugin */
class FlutterHlVnpayPlugin : FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private var activityBinding: ActivityPluginBinding? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_hl_vnpay")
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        binding.addActivityResultListener(this)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activityBinding?.removeActivityResultListener(this)
        activityBinding = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = binding
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "show") {
            this.handleShow(call)
            result.success(null)
        } else {
            result.notImplemented()
        }
    }

    private fun handleShow(@NonNull call: MethodCall) {
        val params = call.arguments as HashMap<*, *>
        val paymentUrl = params["paymentUrl"] as String
        val scheme = params["scheme"] as String
        val tmnCode = params["tmn_code"] as String
        val intent = Intent(flutterPluginBinding!!.applicationContext, VNP_AuthenticationActivity::class.java).apply {
            putExtra("url", paymentUrl)
            putExtra("scheme", scheme)
            putExtra("tmn_code", tmnCode)
        }
        VNP_AuthenticationActivity.setSdkCompletedCallback { action ->
            Log.wtf("VNP_AuthenticationActivity", "action: $action")
            if (action == "AppBackAction") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to -1))
            }
            if (action == "CallMobileBankingApp") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to 10))
            }
            if (action == "WebBackAction") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to 24))
            }
            if (action == "FaildBackAction") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to 99))
            }
            if (action == "FailBackAction") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to 99))
            }
            if (action == "SuccessBackAction") {
                channel.invokeMethod("PaymentBack", hashMapOf("resultCode" to 0))
            }

            //action == AppBackAction
            //Người dùng nhấn back từ sdk để quay lại

            //action == CallMobileBankingApp
            //Người dùng nhấn chọn thanh toán qua app thanh toán (Mobile Banking, Ví...)
            //lúc này app tích hợp sẽ cần lưu lại mã giao dịch thanh toán (vnp_TxnRef). Khi người dùng mở lại app tích hợp với cheme thì sẽ gọi kiểm tra trạng thái thanh toán của mã TxnRef đó kiểm tra xem đã thanh toán hay chưa để thực hiện nghiệp vụ kết thúc thanh toán / thông báo kết quả cho khách hàng..

            //action == WebBackAction
            //Tạo nút sự kiện cho user click từ return url của merchant chuyển hướng về URL: http://cancel.sdk.merchantbackapp
            // vnp_ResponseCode == 24 / Khách hàng hủy thanh toán.

            //action == FaildBackAction
            //Tạo nút sự kiện cho user click từ return url của merchant chuyển hướng về URL: http://fail.sdk.merchantbackapp
            // vnp_ResponseCode != 00 / Giao dịch thanh toán không thành công

            //action == SuccessBackAction
            //Tạo nút sự kiện cho user click từ return url của merchant chuyển hướng về URL: http://success.sdk.merchantbackapp
            //vnp_ResponseCode == 00) / Giao dịch thành công
        }
        activityBinding?.activity?.startActivity(intent)
//        activityBinding?.activity?.startActivityForResult(intent, 99)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
      if (requestCode == 99) {
        channel.invokeMethod("resultCode", resultCode)
      }
      return false
    }
}
