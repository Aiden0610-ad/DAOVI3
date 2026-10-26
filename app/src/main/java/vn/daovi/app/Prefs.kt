package vn.daovi.app

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.security.MessageDigest

/** Cài đặt nhỏ lưu trong SharedPreferences, dùng chung cho dịch vụ nghe, giao diện và widget. */
object Prefs {
    private const val NAME = "daovi"

    fun sp(c: Context): SharedPreferences = c.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun watched(c: Context): Set<String> = sp(c).getStringSet("watched", emptySet()) ?: emptySet()

    fun setWatched(c: Context, pkgs: Set<String>) {
        sp(c).edit().putStringSet("watched", HashSet(pkgs)).apply()
    }

    fun confirm(c: Context): Boolean = sp(c).getBoolean("confirm", true)

    fun setConfirm(c: Context, on: Boolean) {
        sp(c).edit().putBoolean("confirm", on).apply()
    }

    /** Tiền chi đã bắt được nhưng giao diện chưa xử lý, để widget hiển thị ngay. */
    @Synchronized
    fun addPending(c: Context, amount: Long) {
        if (amount <= 0) return
        val p = sp(c)
        p.edit().putLong("pendingOut", p.getLong("pendingOut", 0L) + amount).apply()
    }

    fun hasListenerAccess(c: Context): Boolean {
        val flat = Settings.Secure.getString(c.contentResolver, "enabled_notification_listeners") ?: return false
        val me = ComponentName(c, BankListenerService::class.java)
        return flat.split(":").any { entry ->
            val cn = ComponentName.unflattenFromString(entry)
            cn != null && cn.packageName == me.packageName
        }
    }

    fun sha1(s: String): String {
        val d = MessageDigest.getInstance("SHA-1").digest(s.toByteArray(Charsets.UTF_8))
        return d.joinToString("") { "%02x".format(it) }
    }
}
