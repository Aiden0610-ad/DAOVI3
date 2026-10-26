package vn.daovi.app

/**
 * Bộ đọc nhanh phía máy: chỉ cần biết thông báo có phải biến động số dư không,
 * số tiền và chiều tiền. Việc xếp vùng chi tiết do giao diện đảo làm.
 */
object Parser {
    data class Quick(val amount: Long, val income: Boolean, val note: String)

    private val AMOUNT = Regex(
        """([+\-−–]?)\s*(\d[\d.,]*)\s*(?:vnd|vnđ|VNĐ|₫|đ|Đ)(?![a-zA-Zà-ỹÀ-Ỹ])""",
        RegexOption.IGNORE_CASE
    )
    private val BALANCE_TAIL = Regex(
        """(số dư|so du|sodu|sd|balance|bal|sdc)(\s+(tk|tài khoản|tai khoan|acc)\s*[\dxX*.]*)?[\s:=|.\-]*$""",
        RegexOption.IGNORE_CASE
    )
    private val FALLBACK = Regex(
        """(?:số tiền|so tien|gd|giao dịch|giao dich|amount)\s*[:=]?\s*([+\-]?)\s*(\d[\d.,]*)""",
        RegexOption.IGNORE_CASE
    )
    private val NOTE = Regex("""(?:\bND|n[oộ]i\s*dung|content)\s*[:\-]\s*([^|\n]+)""", RegexOption.IGNORE_CASE)
    private val NOTE_CUT = Regex("""\s*(?:S[ốo]\s*d[ưu]|\bSD)\s*[:=]""", RegexOption.IGNORE_CASE)
    private val SPACES = Regex("\\s+")

    fun parseVnd(raw: String): Long {
        var s = raw.replace(SPACES, "")
        val hasDot = s.contains('.')
        val hasComma = s.contains(',')
        if (hasDot && hasComma) {
            val last = maxOf(s.lastIndexOf('.'), s.lastIndexOf(','))
            if (s.length - last - 1 <= 2) s = s.substring(0, last)
        } else if (hasDot || hasComma) {
            val parts = s.split(if (hasDot) '.' else ',')
            if (parts.size == 2 && parts[1].length <= 2) s = parts[0]
        }
        return s.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
    }

    fun quick(text: String): Quick? {
        val t = text.replace(SPACES, " ").trim()
        var sign = ""
        var num: String? = null
        for (m in AMOUNT.findAll(t)) {
            val pre = t.substring(maxOf(0, m.range.first - 22), m.range.first)
            if (BALANCE_TAIL.containsMatchIn(pre)) continue
            sign = m.groupValues[1]
            num = m.groupValues[2]
            break
        }
        if (num == null) {
            val f = FALLBACK.find(t) ?: return null
            sign = f.groupValues[1]
            num = f.groupValues[2]
        }
        val amount = parseVnd(num)
        if (amount <= 0L) return null
        val low = t.lowercase()
        val income = when {
            sign == "+" -> true
            sign.isNotEmpty() -> false
            else -> {
                val inWords = listOf("tăng", "ghi có", "nhận tiền", "nhận được", "hoàn tiền", "credit")
                val outWords = listOf("giảm", "ghi nợ", "thanh toán", "trừ", "rút", "debit")
                val i = inWords.map { low.indexOf(it) }.filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE
                val o = outWords.map { low.indexOf(it) }.filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE
                i < o
            }
        }
        var note = NOTE.find(text)?.groupValues?.get(1) ?: ""
        note = note.split(NOTE_CUT)[0].replace(SPACES, " ").trim().trimEnd('.', ' ')
        return Quick(amount, income, note.take(60))
    }
}
