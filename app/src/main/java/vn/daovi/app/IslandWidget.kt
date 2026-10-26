package vn.daovi.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject

/** Widget hải đồ trên màn hình chính: cấp đảo, ngân sách tháng, kho vàng. */
class IslandWidget : AppWidgetProvider() {

    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) {
        for (id in ids) m.updateAppWidget(id, views(c))
    }

    companion object {
        fun refresh(c: Context) {
            val m = AppWidgetManager.getInstance(c) ?: return
            val ids = m.getAppWidgetIds(ComponentName(c, IslandWidget::class.java))
            for (id in ids) m.updateAppWidget(id, views(c))
        }

        private fun short(n: Long): String = when {
            n >= 1_000_000L -> {
                val v = n / 100_000L
                val s = "${v / 10},${v % 10}".removeSuffix(",0")
                "$s tr"
            }
            n >= 1_000L -> "${n / 1000}k"
            else -> n.toString()
        }

        fun views(c: Context): RemoteViews {
            val sp = Prefs.sp(c)
            val j = try {
                JSONObject(sp.getString("summary", null) ?: "{}")
            } catch (_: Exception) {
                JSONObject()
            }
            val out = j.optLong("out", 0L) + sp.getLong("pendingOut", 0L)
            val tb = j.optLong("tb", 0L)
            val v = RemoteViews(c.packageName, R.layout.widget_island)
            v.setTextViewText(R.id.w_city, j.optString("city", "Đảo Ví"))
            v.setTextViewText(R.id.w_title, j.optString("title", "Mở đảo để bắt đầu hải trình"))
            v.setTextViewText(R.id.w_level, j.optString("roman", "III"))
            v.setTextViewText(R.id.w_month, j.optString("month", ""))
            v.setTextViewText(R.id.w_spent, short(out) + " / " + short(tb))
            val p = if (tb > 0) minOf(1000L, out * 1000L / tb).toInt() else 0
            v.setProgressBar(R.id.w_bar, 1000, p, false)
            v.setTextViewText(R.id.w_gold, "✦ " + j.optLong("gold", 0L) + " vàng")
            val pi = PendingIntent.getActivity(
                c, 1, Intent(c, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            v.setOnClickPendingIntent(R.id.w_root, pi)
            return v
        }
    }
}
