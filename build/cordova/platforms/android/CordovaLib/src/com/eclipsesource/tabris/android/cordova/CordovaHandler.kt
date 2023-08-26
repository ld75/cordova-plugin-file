/*
 * Copyright(c) 2018 EclipseSource. All Rights Reserved.
 */

package com.eclipsesource.tabris.android.cordova

import android.content.Intent
import android.os.Looper
import android.view.View
import com.eclipsesource.tabris.android.ActivityScope
import com.eclipsesource.tabris.android.Events.ActivityStateListener
import com.eclipsesource.tabris.android.internal.ktx.getStringOrNull
import com.eclipsesource.tabris.android.ObjectHandler
import com.eclipsesource.tabris.android.ActivityState
import com.eclipsesource.v8.V8Object
import org.apache.cordova.ConfigXmlParser
import org.apache.cordova.CordovaPreferences

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class CordovaHandler(private val scope: ActivityScope) : ObjectHandler<String> {

  override val type = "cordova.plugin"

  private var webView = createWebView()

  override fun create(id: String, properties: V8Object): String = properties.getString("service")

  init {
    scope.events.addActivityStateListener(CordovaActivityStateListener())
  }

  private fun createWebView(): TabrisWebView? {
    return scope.tabrisView.let {
      val webView = TabrisWebView(scope)
      val cordovaInterface = CordovaInterfaceImpl(scope, webView.executorService)
      val configXmlParser = ConfigXmlParser()
      configXmlParser.parse(scope.context)
      webView.init(cordovaInterface, configXmlParser.pluginEntries, CordovaPreferences())
      it.addView(webView.view, 0)
      webView.view.visibility = View.GONE
      webView
    }
  }

  override fun call(service: String, method: String, properties: V8Object): Any? {
    val action = properties.getString("action")
    val callbackId = properties.getString(TabrisWebView.CALLBACK_ID)
    val arguments = properties.getStringOrNull("arguments")
    scope.remoteObject(service as Any)?.let {
      webView?.mapCallbackIdToTarget(callbackId, it.id)
      webView?.executorService?.execute {
        if (Looper.myLooper() == null) Looper.prepare()
        webView?.pluginManager?.exec(service, action, callbackId, arguments)
      }
    }
    return null
  }

  override fun destroy(service: String) {
    destroyCordovaWebView()
  }

  private fun destroyCordovaWebView() {
    webView?.handleDestroy()
    webView = null
  }

  private inner class CordovaActivityStateListener : ActivityStateListener {

    override fun activityStateChanged(state: ActivityState, intent: Intent?) {
      when (state) {
        ActivityState.NEW_INTENT -> webView?.onNewIntent(scope.activity.intent)
        ActivityState.START -> webView?.handleStart()
        ActivityState.RESUME -> webView?.handleResume(true)
        ActivityState.PAUSE -> webView?.handlePause(true)
        ActivityState.STOP -> webView?.handleStop()
        ActivityState.CONFIGURATION -> webView?.pluginManager?.onConfigurationChanged(null)
        ActivityState.DESTROY -> destroyCordovaWebView()
        ActivityState.LOW_MEMORY -> Unit
      }
    }
  }

}
