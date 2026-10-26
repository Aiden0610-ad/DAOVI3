package vn.daovi.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.ref.WeakReference

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var loaded = false

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.ensureChannel(this)

        web = WebView(this)
        setContentView(web)
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            textZoom = 100
        }
        web.setBackgroundColor(0)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val u = request.url
                if (u.scheme == "file") return false
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, u)); true
                } catch (_: Exception) {
                    true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                loaded = true
                pushInbox()
            }
        }
        web.addJavascriptInterface(Bridge(this), "DaoVi")
        web.loadUrl("file:///android_asset/www/index.html")

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 7)
        }
    }

    override fun onResume() {
        super.onResume()
        current = WeakReference(this)
        pushInbox()
    }

    override fun onPause() {
        super.onPause()
        if (current?.get() === this) current = null
    }

    /** Gọi vào giao diện để đọc hộp thư điện tín và cập nhật trạm nghe. */
    fun pushInbox() {
        if (!loaded) return
        runOnUiThread {
            web.evaluateJavascript("window.DaoViNative && window.DaoViNative.onInbox()", null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else @Suppress("DEPRECATION") super.onBackPressed()
    }

    companion object {
        @Volatile
        private var current: WeakReference<MainActivity>? = null

        fun ping() {
            current?.get()?.pushInbox()
        }
    }
}
