package vn.daovi.app

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Trạm nghe: hệ thống Android gửi mọi thông báo tới đây sau khi bạn cấp quyền.
 * Trạm chỉ giữ lại thông báo từ những ứng dụng bạn đã chọn và trông giống biến động số dư.
 * Không gì rời khỏi máy.
 */
class BankListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) return
            val pkg = sbn.packageName ?: return
            if (pkg == packageName) return
            if (pkg !in Prefs.watched(this)) return

            val n = sbn.notification ?: return
            if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
            val ex = n.extras ?: return

            val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val big = ex.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val txt = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val lines = ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.joinToString("\n") { it.toString() }
            val body = listOf(big, txt, lines).firstOrNull { !it.isNullOrBlank() } ?: return
            val text = if (title.isNotBlank() && !body.contains(title)) "$title\n$body" else body

            val q = Parser.quick(text) ?: return
            val hash = Prefs.sha1(pkg + "|" + body.replace(Regex("\\s+"), " ").trim())
            val added = InboxDb.get(this).add(pkg, appLabel(pkg), text, sbn.postTime, hash)
            if (!added) return

            if (!q.income) Prefs.addPending(this, q.amount)
            IslandWidget.refresh(this)
            if (Prefs.confirm(this)) Notifier.recorded(this, q)
            MainActivity.ping()
        } catch (_: Throwable) {
            // không bao giờ để trạm nghe sập vì một thông báo lạ
        }
    }

    private fun appLabel(pkg: String): String = try {
        val pm = packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        pkg
    }
}
