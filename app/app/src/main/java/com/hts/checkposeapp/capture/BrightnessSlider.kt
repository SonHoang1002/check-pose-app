package com.hts.checkposeapp.capture

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Thanh chỉnh độ sáng của camera (bù sáng, EV).
 *
 * Nhận và trả về SỐ NẤC (index), không phải EV: máy quyết định một nấc bằng bao
 * nhiêu EV. [stepEv] chỉ dùng để HIỂN THỊ con số cho người dùng đọc.
 *
 * @param index nấc hiện tại, nằm trong [range]
 * @param range các nấc máy cho chỉnh — `0..0` nghĩa là máy không hỗ trợ
 * @param stepEv độ lớn một nấc, đơn vị EV
 */
@Composable
fun BrightnessSlider(
    index: Int,
    range: IntRange,
    stepEv: Float,
    onChange: (Int) -> Unit,
) {
    val ev = index * stepEv
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("☀", color = Color.White, fontSize = 16.sp)
        Slider(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            value = index.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.35f),
            ),
        )
        Text(
            "%+.1f".format(ev),
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(40.dp),
        )
    }
}
