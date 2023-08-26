package com.eclipsesource.tabris.android.cordova

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.eclipsesource.tabris.android.ActivityScope
import com.eclipsesource.tabris.android.Events.*
import org.apache.cordova.CallbackMap
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import java.util.concurrent.ExecutorService
import androidx.appcompat.app.AppCompatActivity

internal class CordovaInterfaceImpl(private val scope: ActivityScope, private val executorService: ExecutorService) : CordovaInterface {

  private val permissionResultCallbacks = CallbackMap()
  private var activityResultCallback: CordovaPlugin? = null
  private var activityResultRequestCode: Int = 0

  init {
    scope.events.addRequestPermissionResultListener(object : RequestPermissionsResultListener {
      override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
          onRequestPermissionResult(requestCode, permissions, grantResults)
        } catch (e: Exception) {
          scope.log.error("Could not handle received permission", e)
        }
      }
    })
    scope.events.addStartActivityForResultListener(object : StartActivityForResultListener {
      override fun startActivityForResultInvoked(intent: Intent?, requestCode: Int, options: Bundle?): Boolean {
        activityResultRequestCode = requestCode
        return true
      }
    })
    scope.events.addActivityResultListener(object : ActivityResultListener {
      override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
          activityResultCallback?.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
          scope.log.error("Could not handle activity result", e)
        }
        activityResultCallback = null
      }
    })
  }

  override fun startActivityForResult(cordovaPlugin: CordovaPlugin, intent: Intent?, requestCode: Int) {
    setActivityResultCallback(cordovaPlugin)
    startActivityForResult(intent, requestCode, null)
  }

  private fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
    try {
      activity.startActivityForResult(intent, requestCode, options)
    } catch (e: Exception) {
      activityResultCallback = null
      throw e
    }

  }

  override fun setActivityResultCallback(plugin: CordovaPlugin) {
    activityResultCallback?.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null)
    activityResultCallback = plugin
  }

  override fun getContext() = scope.context

  override fun getActivity() = scope.activity as AppCompatActivity

  override fun onMessage(id: String?, data: Any): Any? {
    if (id == "exit") {
      scope.activity.finish()
    }
    return null
  }

  override fun getThreadPool() = executorService

  private fun onRequestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    val callback = permissionResultCallbacks.getAndRemoveCallback(requestCode)
    callback?.first?.onRequestPermissionResult(callback.second, permissions, grantResults)
  }

  override fun requestPermission(plugin: CordovaPlugin, requestCode: Int, permission: String) {
    requestPermissions(plugin, requestCode, arrayOf(permission))
  }

  override fun requestPermissions(plugin: CordovaPlugin, requestCode: Int, permissions: Array<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val mappedRequestCode = permissionResultCallbacks.registerCallback(plugin, requestCode)
      activity.requestPermissions(permissions, mappedRequestCode)
    }
  }

  override fun hasPermission(permission: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      scope.activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

}
