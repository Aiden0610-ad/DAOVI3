package vn.daovi.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

/** Cầu nối giữa giao diện đảo (WebView) và phần máy. Tên trong JS: window.DaoVi */
class Bridge(private val act: MainActivity) {
    private val ctx = act.applicationContext

    @JavascriptInterface
    fun pullInbox(): String = InboxDb.get(ctx).pendingJson()

    @JavascriptInterface
    fun ack(json: String) {
        val a = JSONArray(json)
        InboxDb.get(ctx).ack((0 until a.length()).map { a.getLong(it) })
    }

    @JavascriptInterface
    fun hasAccess(): Boolean = Prefs.hasListenerAccess(ctx)

    @JavascriptInterface
    fun openAccess() {
        act.runOnUiThread {
            act.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    /** Mở trang thông tin ứng dụng: nơi bật "Cho phép cài đặt bị hạn chế" trên Android 13 trở lên. */
    @JavascriptInterface
    fun openAppInfo() {
        act.runOnUiThread {
            act.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.packageName))
            )
        }
    }

    @JavascriptInterface
    fun openBattery() {
        act.runOnUiThread {
            try {
                act.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                openAppInfo()
            }
        }
    }

    @JavascriptInterface
    fun listApps(): String {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val watched = Prefs.watched(ctx)
        val seen = HashSet<String>()
        val rows = ArrayList<Pair<String, String>>()
        @Suppress("DEPRECATION")
        val infos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        for (ri in infos) {
            val p = ri.activityInfo?.packageName ?: continue
            if (p == ctx.packageName || !seen.add(p)) continue
            rows.add(p to ri.loadLabel(pm).toString())
        }
        rows.sortBy { it.second.lowercase() }
        val arr = JSONArray()
        for ((p, label) in rows) arr.put(JSONObject().put("pkg", p).put("label", label).put("on", p in watched))
        return arr.toString()
    }

    @JavascriptInterface
    fun setWatched(json: String) {
        val a = JSONArray(json)
        Prefs.setWatched(ctx, (0 until a.length()).map { a.getString(it) }.toSet())
    }

    @JavascriptInterface
    fun confirmOn(): Boolean = Prefs.confirm(ctx)

    @JavascriptInterface
    fun setConfirm(on: Boolean) = Prefs.setConfirm(ctx, on)

    /** Giao diện báo số liệu tháng hiện tại cho widget; tiền chờ xử lý được xóa vì đã vào sổ. */
    @JavascriptInterface
    fun summary(json: String) {
        Prefs.sp(ctx).edit().putString("summary", json).putLong("pendingOut", 0L).apply()
        IslandWidget.refresh(ctx)
    }

    @JavascriptInterface
    fun share(text: String) {
        act.runOnUiThread {
            val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
            act.startActivity(Intent.createChooser(send, "Sao lưu Đảo Ví"))
        }
    }

    /** Thả một điện tín thử vào hộp thư, như thể ngân hàng vừa gửi. */
    @JavascriptInterface
    fun simulate(text: String): Boolean {
        val now = System.currentTimeMillis()
        val ok = InboxDb.get(ctx).add("demo", "Điện tín thử", text, now, Prefs.sha1("demo|$now|$text"))
        val q = Parser.quick(text)
        if (ok && q != null && Prefs.confirm(ctx)) Notifier.recorded(ctx, q)
        return ok
    }

    @JavascriptInterface
    fun platform(): String = "android"
}
