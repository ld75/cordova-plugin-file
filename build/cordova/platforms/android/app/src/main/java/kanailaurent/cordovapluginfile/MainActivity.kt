/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package kanailaurent.cordovapluginfile;

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.eclipsesource.tabris.android.ActivityState
import com.eclipsesource.tabris.android.ActivityState.NEW_INTENT
import com.eclipsesource.tabris.android.SubsequentAction
import com.eclipsesource.tabris.android.SubsequentAction.Launch
import com.eclipsesource.tabris.android.TabrisFragment
import com.eclipsesource.tabris.android.boot.AssetBootJsSource
import com.eclipsesource.tabris.android.boot.BootJsLoader
import com.eclipsesource.tabris.android.boot.BootJsResponse
import com.eclipsesource.tabris.android.boot.Resource
import com.eclipsesource.tabris.android.boot.Resource.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import java.io.IOException
import org.apache.cordova.*;

class MainActivity : CordovaActivity() {

  private val bundledAppUri = "file:///android_asset/www/app"
  private val rootId = View.generateViewId()

  override fun onCreate(savedInstanceState: Bundle?) {
    loadConfig()
    setTheme(getAppTheme())
    super.onCreate(savedInstanceState)
    super.init()
    val root = android.widget.FrameLayout(this)
    root.isFocusableInTouchMode = true
    root.id = rootId
    setContentView(root)
    getPackageJsonFromBuildConfig()?.let { launchBlocking(it) }
  }

  private fun getAppTheme() =
    preferences.getString("Theme", null)?.substringAfterLast("@")?.toInt()
      ?: R.style.Theme_Tabris_Light_DarkAppBar

  private fun getPackageJsonFromBuildConfig(): String? {
    return try {
      assets.open(BuildConfig.JS_APP.removePrefix("file:///android_asset/")).close()
      BuildConfig.JS_APP
    } catch (e: IOException) {
      null
    }
  }

  private fun launchBlocking(uri: String) {
    runBlocking {
      launch(Dispatchers.Default) {
        BootJsLoader(application)
          .load(uri)
          .catch { showError(it.message ?: it.toString()) }
          .collect { handleBoothJsResource(it) }
      }
    }
  }

  fun launch(uri: String) {
    GlobalScope.launch {
      BootJsLoader(application)
        .load(uri)
        .catch { showError(it.message ?: it.toString()) }
        .collect { withContext(Dispatchers.Main) { handleBoothJsResource(it) } }
    }
  }

  private fun handleBoothJsResource(resource: Resource<BootJsResponse>) {
    when (resource) {
      is Success -> showTabrisFragment(resource.data)
      is Failure -> showError("${resource.code} - ${resource.message}")
      else -> Unit
    }
  }

  private fun showTabrisFragment(response: BootJsResponse) {
    val tabrisFragment = newTabrisFragment(response)
    supportFragmentManager.beginTransaction()
      .replace(rootId, tabrisFragment, tabrisFragment::class.java.name)
      .addToBackStack(null)
      .commit()
  }

  private fun showError(message: String) {
    Snackbar.make(findViewById(rootId), message, Snackbar.LENGTH_LONG).show()
  }

  private fun newTabrisFragment(response: BootJsResponse): TabrisFragment {
    val tabrisFragment = TabrisFragment.newInstance(
      baseUri = response.baseUri,
      devMode = preferences.getBoolean("EnableDeveloperConsole", false),
      strictSsl = preferences.getBoolean("UseStrictSSL", true)
    )
    tabrisFragment.onScopeAvailable { scope ->
      scope.onClose { action ->
        if (action is Launch) {
          launch(action.uri)
        } else {
          val backStackSize = supportFragmentManager.backStackEntryCount
          if (response.baseUri == bundledAppUri || backStackSize <= 1) {
            finish()
          } else {
            repeat(backStackSize - 1) { supportFragmentManager.popBackStack() }
          }
        }
      }
      scope.boot(response.bootScripts)
      appView.pluginManager.postMessage("onPageFinished", getPackageJsonFromBuildConfig());
    }
    return tabrisFragment
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    getEvents()?.notifyActivityStateChanged(NEW_INTENT, intent)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    getEvents()?.notifyActivityStateChanged(ActivityState.CONFIGURATION)
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent) =
    getEvents()?.notifyKeyDownEvent(event)?.takeIf { it } ?: super.onKeyDown(keyCode, event)

  override fun dispatchKeyEvent(event: KeyEvent) =
    getEvents()?.notifyDispatchKeyEvent(event)?.takeIf { it } ?: super.dispatchKeyEvent(event)

  override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    getEvents()?.notifyTouchEvent(event)
    return super.dispatchTouchEvent(event)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    getEvents()?.notifyActivityResultReceived(requestCode, resultCode, data)
  }

  override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
    super.startActivityForResult(intent, requestCode, options)
    getEvents()?.notifyStartActivityForResult(intent, requestCode, options)
  }

  override fun onLowMemory() {
    super.onLowMemory()
    getEvents()?.notifyActivityStateChanged(ActivityState.LOW_MEMORY)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    getEvents()?.notifyRequestPermissionResult(requestCode, permissions, grantResults)
  }

  private fun getEvents() =
    (supportFragmentManager.findFragmentByTag(TabrisFragment::class.java.name) as? TabrisFragment)
      ?.scope?.events

}
