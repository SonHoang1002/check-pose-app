package com.hts.checkposeapp.light

import android.graphics.Bitmap

/**
 * Kết quả đo ánh sáng của MỘT ảnh.
 *
 * ⚠️ Chỉ để HIỂN THỊ cho người dùng biết vì sao ảnh tối hay cháy. KHÔNG đưa vào
 * điểm giống mẫu: PO đã chốt bỏ qua ánh sáng khỏi phần chấm điểm (xem CLAUDE.md,
 * mục "Đối chiếu với bản iOS").
 */
data class LightReading(
    /** Độ sáng trung bình, 0 (đen) .. 255 (trắng). */
    val meanLuma: Int,
    /** Tỉ lệ điểm ảnh gần như trắng hoàn toàn (cháy sáng), 0..1. */
    val clippedHighFraction: Float,
    /** Tỉ lệ điểm ảnh gần như đen hoàn toàn (mất chi tiết bóng tối), 0..1. */
    val clippedLowFraction: Float,
) {
    val verdict: LightVerdict
        get() = when {
            clippedHighFraction > LightConfig.CHAY_SANG_TI_LE -> LightVerdict.CHAY_SANG
            meanLuma < LightConfig.THIEU_SANG_BELOW -> LightVerdict.THIEU_SANG
            meanLuma > LightConfig.QUA_SANG_ABOVE -> LightVerdict.QUA_SANG
            else -> LightVerdict.DU_SANG
        }
}

/** Nhãn và lời khuyên hiển thị cho từng kết luận ánh sáng. */
enum class LightVerdict(val nhan: String, val goiY: String) {
    DU_SANG(
        "Đủ sáng",
        "Ánh sáng ổn, có thể so sánh với ảnh mẫu.",
    ),
    THIEU_SANG(
        "Thiếu sáng",
        "Ảnh tối, chi tiết dễ bị mờ. Chụp chỗ sáng hơn hoặc tăng độ sáng bằng thanh chỉnh.",
    ),
    QUA_SANG(
        "Quá sáng",
        "Ảnh sáng quá, mất sắc thái. Giảm độ sáng bằng thanh chỉnh.",
    ),
    CHAY_SANG(
        "Cháy sáng",
        "Có vùng quá sáng mất chi tiết (áo trắng, nền trời). Giảm độ sáng hoặc tránh nguồn sáng chiếu thẳng.",
    ),
}

/**
 * Ngưỡng phân loại ánh sáng.
 *
 * ⚠️ TẠM — chưa đo trên máy thật. Chỉnh TẠI ĐÂY khi có buổi đo, giống `GuidanceConfig`.
 */
internal object LightConfig {
    /** Điểm ảnh có độ sáng từ mức này trở lên được tính là "cháy trắng". */
    const val CAO_SANG_TU = 250

    /** Điểm ảnh có độ sáng từ mức này trở xuống được tính là "mất chi tiết đen". */
    const val THAP_SANG_DEN = 5

    /** Cháy sáng khi vùng trắng chiếm quá tỉ lệ này. */
    const val CHAY_SANG_TI_LE = 0.05f

    /** Trung bình dưới mức này là thiếu sáng. */
    const val THIEU_SANG_BELOW = 70

    /** Trung bình trên mức này là quá sáng. */
    const val QUA_SANG_ABOVE = 200

    /** Cạnh dài tối đa khi thu nhỏ để đếm — đủ chính xác, không đếm cả triệu điểm ảnh. */
    const val MAU_CANH_NGANG = 160
}

/** Đo ánh sáng của một ảnh. Hàm thuần trên pixel, tách khỏi Android để kiểm được bằng test. */
object LightAnalysis {

    fun of(bitmap: Bitmap): LightReading {
        val scale = minOf(1f, LightConfig.MAU_CANH_NGANG.toFloat() / bitmap.width)
        val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val small = if (w == bitmap.width && h == bitmap.height) bitmap
        else Bitmap.createScaledBitmap(bitmap, w, h, true)

        val pixels = IntArray(small.width * small.height)
        small.getPixels(pixels, 0, small.width, 0, 0, small.width, small.height)
        if (small !== bitmap) small.recycle()
        return fromArgb(pixels)
    }

    /** Đếm từ mảng ARGB. Không đụng Android nên kiểm được bằng unit test. */
    fun fromArgb(pixels: IntArray): LightReading {
        if (pixels.isEmpty()) return LightReading(0, 0f, 0f)
        var sum = 0L
        var high = 0
        var low = 0
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            // Công thức độ sáng (luma) chuẩn BT.601, nhân nguyên để không dùng số thực.
            val y = (299 * r + 587 * g + 114 * b) / 1000
            sum += y
            if (y >= LightConfig.CAO_SANG_TU) high++
            if (y <= LightConfig.THAP_SANG_DEN) low++
        }
        val n = pixels.size.toFloat()
        return LightReading(
            meanLuma = (sum / pixels.size).toInt(),
            clippedHighFraction = high / n,
            clippedLowFraction = low / n,
        )
    }
}
