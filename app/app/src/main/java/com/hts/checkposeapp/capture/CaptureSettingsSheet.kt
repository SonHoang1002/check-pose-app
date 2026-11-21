package com.hts.checkposeapp.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hts.checkposeapp.ui.theme.Ds

/**
 * CÀI ĐẶT MÀN CHỤP — mở bằng nút ⚙, kiểu bảng dưới của iPhone.
 *
 * Gom những thứ KHÔNG cần thấy khi đang canh khung: chế độ bấm, kiểu chụp, tỉ lệ
 * khung, lưới, khung xương, số đo gỡ lỗi. Mục nào đổi được giữa lúc quay thì khoá
 * khi đang quay hoặc đang chụp liên tục — đúng quy tắc cũ, chỉ đổi nơi đặt.
 *
 * Trạng thái nằm ở màn chụp; sheet này chỉ hiển thị và báo lại thay đổi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSettingsSheet(
    autoMode: Boolean,
    onAutoMode: (Boolean) -> Unit,
    burst: Boolean,
    onBurst: (Boolean) -> Unit,
    tiLeNhan: String,
    onTiLeTiep: () -> Unit,
    khoaKhiDangQuay: Boolean,
    showGrid: Boolean,
    onGrid: (Boolean) -> Unit,
    showSkeleton: Boolean,
    onSkeleton: (Boolean) -> Unit,
    showDebug: Boolean,
    onDebug: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ds.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Ds.pageH)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Cài đặt", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ds.text)
            Spacer(Modifier.height(4.dp))

            SectionLabel("Bấm chụp")
            Segmented(
                labelOff = "Bấm tay",
                labelOn = "Tự đếm 5s",
                on = autoMode,
                enabled = !khoaKhiDangQuay,
                onChange = onAutoMode,
            )
            Hint(
                if (autoMode) "Tự động sẽ bỏ qua mục bị kẹt sau khoảng 6 giây."
                else "Bạn có thể bấm chụp bất cứ lúc nào."
            )

            SectionLabel("Kiểu chụp")
            Segmented(
                labelOff = "Quay video",
                labelOn = "Chụp liên tục",
                on = burst,
                enabled = !khoaKhiDangQuay,
                onChange = onBurst,
            )

            SectionLabel("Khung hình")
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Ds.rCard))
                    .background(Ds.surfaceMuted)
                    .clickable(enabled = !khoaKhiDangQuay, onClick = onTiLeTiep)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Tỉ lệ khung",
                    color = if (khoaKhiDangQuay) Ds.textMuted else Ds.text,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(tiLeNhan, color = Ds.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            if (khoaKhiDangQuay) Hint("Không đổi được khi đang quay hoặc chụp liên tục.")

            SectionLabel("Hiển thị")
            SwitchRow("Lưới 3×3", showGrid, onGrid)
            SwitchRow("Khung xương", showSkeleton, onSkeleton)
            SwitchRow("Số đo gỡ lỗi", showDebug, onDebug)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = Ds.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun Hint(text: String) {
    Text(text, color = Ds.textMuted, fontSize = 12.sp)
}

/** Hai lựa chọn kiểu nút gạt: trái = tắt (`on = false`), phải = bật (`on = true`). */
@Composable
private fun Segmented(
    labelOff: String,
    labelOn: String,
    on: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Ds.rCard))
            .background(Ds.surfaceMuted)
            .padding(4.dp),
    ) {
        SegmentOption(labelOff, selected = !on, enabled = enabled, Modifier.weight(1f)) { onChange(false) }
        SegmentOption(labelOn, selected = on, enabled = enabled, Modifier.weight(1f)) { onChange(true) }
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(Ds.rPill))
            .background(if (selected) Ds.surface else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = when {
                !enabled -> Ds.textMuted
                selected -> Ds.text
                else -> Ds.textMuted
            },
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Ds.rCard))
            .background(Ds.surfaceMuted)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Ds.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
