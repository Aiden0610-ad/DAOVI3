package vn.daovi.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.text.NumberFormat
import java.util.Locale

object Notifier {
    private const val CHANNEL = "ghi_so"

    fun ensureChannel(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL) == null) {
            val ch = NotificationChannel(CHANNEL, "Ghi sổ tự động", NotificationManager.IMPORTANCE_LOW)
            ch.description = "Báo khi Đảo Ví vừa ghi một giao dịch vào sổ"
            nm.createNotificationChannel(ch)
        }
    }

    @SuppressLint("MissingPermission")
    fun recorded(c: Context, q: Parser.Quick) {
        if (Build.VERSION.SDK_INT >= 33 &&
            c.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(c)
        val nm = c.getSystemService(NotificationManager::class.java) ?: return
        val amount = NumberFormat.getInstance(Locale("vi", "VN")).format(q.amount)
        val title = (if (q.income) "+" else "−") + amount + " ₫ đã vào sổ"
        val open = Intent(c, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pi = PendingIntent.getActivity(
            c, 0, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = Notification.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title)
            .setContentText(q.note.ifBlank { "Mở đảo để xem khoản này rơi vào vùng nào" })
            .setColor(0xFF8E2F25.toInt())
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setGroup("daovi")
            .build()
        nm.notify((System.currentTimeMillis() % 1_000_000L).toInt(), n)
    }
}
