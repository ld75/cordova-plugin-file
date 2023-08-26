package com.eclipsesource.tabris.android.cordova

import android.annotation.SuppressLint
import com.eclipsesource.tabris.android.ActivityScope
import com.eclipsesource.tabris.android.post
import org.apache.cordova.*
import org.apache.cordova.engine.SystemWebView
import org.apache.cordova.engine.SystemWebViewEngine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("ViewConstructor")
internal class TabrisWebView(private val scope: ActivityScope)
  : CordovaWebViewImpl(SystemWebViewEngine(SystemWebView(scope.activity))) {

  private val idToTargetMapping = mutableMapOf<String, String>()
  private var nativeToJsMessageQueue: NativeToJsMessageQueue? = null
  val executorService: ExecutorService = Executors.newFixedThreadPool(10)

  override fun init(cordova: CordovaInterface, pluginEntries: List<PluginEntry>, preferences: CordovaPreferences) {
    nativeToJsMessageQueue = NativeToJsMessageQueue().apply {
      addBridgeMode(NativeToJsMessageQueue.NoOpBridgeMode())
      setBridgeMode(0)
    }
    super.init(cordova, pluginEntries, preferences)
  }

  override fun sendPluginResult(result: PluginResult, callbackId: String) {
    val encodedResult = encodeResult(result, callbackId) ?: return
    idToTargetMapping[callbackId]?.let { id ->
      if (!result.keepCallback) {
        idToTargetMapping.remove(callbackId)
      }
      scope.post {
        remoteObject(id)?.notify(FINISH, mapOf(
            CALLBACK_ID to callbackId,
            STATUS to result.status,
            KEEP_CALLBACK to result.keepCallback,
            MESSAGE to encodedResult))
      }
    }
  }

  private fun encodeResult(result: PluginResult, callbackId: String): String? {
    nativeToJsMessageQueue?.addPluginResult(result, callbackId)
    return nativeToJsMessageQueue?.popAndEncode(false)
  }

  fun mapCallbackIdToTarget(callbackId: String, target: String) {
    idToTargetMapping[callbackId] = target
  }

  override fun loadUrl(url: String?) {
    url?.trim()?.let {
      if (it.startsWith("javascript:")) {
        scope.bridge.v8.executeScript(it.substringAfter("javascript:"))
      }
    }
  }

  companion object {

    const val CALLBACK_ID = "callbackId"

    private const val STATUS = "status"
    private const val MESSAGE = "message"
    private const val FINISH = "finish"
    private const val KEEP_CALLBACK = "keepCallback"
  }
}