package com.hlsolutions.flutter_hl_vnpay.flutter_hl_vnpay

import android.content.Intent
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
        activityBinding?.activity?.startActivityForResult(intent, 99)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
      if (requestCode == 99) {
        channel.invokeMethod("resultCode", resultCode)
      }
      return false
    }
}
