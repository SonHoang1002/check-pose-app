package com.hts.checkposeapp.home

import android.content.Context
import com.hts.checkposeapp.face.FaceAnalyzer
import com.hts.checkposeapp.face.FaceInfo
import com.hts.checkposeapp.guidance.PoseDescriber
import com.hts.checkposeapp.media.UprightBitmap
import com.hts.checkposeapp.pose.FramingClass
import com.hts.checkposeapp.pose.PoseFrame
import com.hts.checkposeapp.pose.StillPoseAnalyzer
import com.hts.checkposeapp.template.TemplateGate
import com.hts.checkposeapp.template.TemplateProfile
import com.hts.checkposeapp.template.TemplateVerdict
import java.io.File

/**
 * Kết quả phân tích MỘT LẦN một ảnh mẫu — dùng chung cho cổng kiểm, hồ sơ tiêu chí
 * và bảng gắn nhãn ảnh tự nhập, để không phân tích lại và chắc chắn các bên nói về
 * cùng một kết quả.
 */
internal class PhanTichAnhMau(
    val verdict: TemplateVerdict,
    private val frame: PoseFrame?,
    private val face: FaceInfo?,
    val poseGuide: List<String>,
    val difficulty: PoseDescriber.Difficulty?,
) {
    val framing: FramingClass? get() = (verdict as? TemplateVerdict.Accepted)?.framing

    /** Hồ sơ tiêu chí; [gocMayNhan] là nhãn góc máy nếu có. `null` khi ảnh bị từ chối. */
    fun hoSo(
        gocMayNhan: Double?,
        kieuChupTren: com.hts.checkposeapp.template.KieuChupTren? = null,
    ): TemplateProfile? {
        val f = frame ?: return null
        val k = framing ?: return null
        return TemplateProfile.from(
            f, k, TemplateGate.CORE_VIS, face,
            gocMayNhan = gocMayNhan, kieuChupTren = kieuChupTren,
        )
    }
}

/** Chạy chặn luồng — gọi trong `Dispatchers.Default`. */
internal fun phanTichAnhMau(context: Context, file: File): PhanTichAnhMau {
    val bmp = UprightBitmap.decode(file)
    val frame = if (bmp == null) null else StillPoseAnalyzer.analyze(context, bmp)
    val v = TemplateGate.check(frame)
    val accepted = v as? TemplateVerdict.Accepted
    // Nhận diện mặt CHỈ chạy khi ảnh đã qua cổng kiểm — ảnh bị từ chối thì chẳng
    // dùng tới, chạy chỉ tốn thời gian chờ.
    val face = if (frame != null && bmp != null && accepted != null) {
        val fa = FaceAnalyzer()
        try { fa.analyze(bmp) } finally { fa.close() }
    } else null
    return PhanTichAnhMau(
        verdict = v,
        frame = frame,
        face = face,
        poseGuide = if (frame != null && accepted != null) PoseDescriber.describe(frame, TemplateGate.CORE_VIS) else emptyList(),
        difficulty = if (frame != null && accepted != null) {
            PoseDescriber.difficulty(frame, accepted.framing, TemplateGate.CORE_VIS)
        } else null,
    )
}
