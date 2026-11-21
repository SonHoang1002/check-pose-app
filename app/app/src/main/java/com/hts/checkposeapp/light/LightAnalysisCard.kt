package com.hts.checkposeapp.light

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.hts.checkposeapp.ui.theme.Ds

/**
 * Mục PHÂN TÍCH ÁNH SÁNG của ảnh đang chọn — chỉ hiển thị, không tính vào điểm.
 */
@Composable
fun LightAnalysisCard(reading: LightReading?) {
    if (reading == null) return
    val verdict = reading.verdict
    val color = if (verdict == LightVerdict.DU_SANG) Ds.success else Ds.warning

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Ds.rCard))
            .background(Ds.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            "Ánh sáng: ${verdict.nhan}",
            color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(verdict.goiY, color = Ds.textMuted, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Trung bình ${reading.meanLuma}/255 · cháy sáng ${pct(reading.clippedHighFraction)} · " +
                "tối mất chi tiết ${pct(reading.clippedLowFraction)}",
            color = Ds.textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
        )
        Text(
            "Không tính vào điểm giống mẫu.",
            color = Ds.textMuted, fontSize = 10.sp,
        )
    }
}

private fun pct(f: Float): String = "%.1f%%".format(f * 100f)
