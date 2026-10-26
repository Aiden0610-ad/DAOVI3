package vn.daovi.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Hộp thư điện tín: mọi thông báo ngân hàng bắt được nằm ở đây cho tới khi
 * giao diện đảo đọc và ghi vào sổ. Mã băm UNIQUE chặn ghi trùng.
 */
class InboxDb private constructor(ctx: Context) : SQLiteOpenHelper(ctx, "inbox.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE inbox(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pkg TEXT NOT NULL," +
                "app TEXT," +
                "text TEXT NOT NULL," +
                "ts INTEGER NOT NULL," +
                "hash TEXT NOT NULL UNIQUE," +
                "done INTEGER NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    /** Trả về true nếu là điện tín mới (chưa từng thấy). */
    fun add(pkg: String, app: String, text: String, ts: Long, hash: String): Boolean {
        val cv = ContentValues().apply {
            put("pkg", pkg)
            put("app", app)
            put("text", text)
            put("ts", ts)
            put("hash", hash)
        }
        return writableDatabase.insertWithOnConflict("inbox", null, cv, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    fun pendingJson(): String {
        val arr = JSONArray()
        readableDatabase.rawQuery(
            "SELECT id, pkg, app, text, ts FROM inbox WHERE done = 0 ORDER BY ts ASC LIMIT 300", null
        ).use { c ->
            while (c.moveToNext()) {
                arr.put(
                    JSONObject()
                        .put("id", c.getLong(0))
                        .put("pkg", c.getString(1))
                        .put("app", c.getString(2) ?: "")
                        .put("text", c.getString(3))
                        .put("ts", c.getLong(4))
                )
            }
        }
        return arr.toString()
    }

    fun pendingCount(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM inbox WHERE done = 0", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    fun ack(ids: List<Long>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (id in ids) db.execSQL("UPDATE inbox SET done = 1 WHERE id = ?", arrayOf<Any>(id))
            // giữ dấu vết 120 ngày để không ghi trùng, sau đó dọn bớt
            val cutoff = System.currentTimeMillis() - 120L * 86_400_000L
            db.execSQL("DELETE FROM inbox WHERE done = 1 AND ts < ?", arrayOf<Any>(cutoff))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        @Volatile
        private var inst: InboxDb? = null

        fun get(c: Context): InboxDb =
            inst ?: synchronized(this) { inst ?: InboxDb(c.applicationContext).also { inst = it } }
    }
}
