package com.hts.checkposeapp

import com.hts.checkposeapp.light.LightAnalysis
import com.hts.checkposeapp.light.LightVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kiểm HÀNH VI phân loại ánh sáng. Con số ngưỡng cụ thể phải đo trên máy thật,
 * các test này chỉ khẳng định đúng hướng: tối thì báo tối, cháy thì báo cháy.
 */
class LightAnalysisTest {

    private fun gray(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    @Test
    fun `anh den thi bao thieu sang va mat chi tiet bong toi`() {
        val r = LightAnalysis.fromArgb(IntArray(100) { gray(0) })
        assertEquals(0, r.meanLuma)
        assertEquals(1f, r.clippedLowFraction, 0.001f)
        assertEquals(LightVerdict.THIEU_SANG, r.verdict)
    }

    @Test
    fun `anh trang thi bao chay sang`() {
        val r = LightAnalysis.fromArgb(IntArray(100) { gray(255) })
        assertEquals(1f, r.clippedHighFraction, 0.001f)
        assertEquals(LightVerdict.CHAY_SANG, r.verdict)
    }

    @Test
    fun `anh xam trung binh thi du sang`() {
        val r = LightAnalysis.fromArgb(IntArray(100) { gray(128) })
        assertEquals(128, r.meanLuma)
        assertEquals(LightVerdict.DU_SANG, r.verdict)
    }

    @Test
    fun `vung chay nho duoi nguong thi van du sang`() {
        // 2% điểm trắng, còn lại xám vừa: dưới ngưỡng cháy 5%, không được báo cháy.
        val pixels = IntArray(100) { if (it < 2) gray(255) else gray(128) }
        assertEquals(LightVerdict.DU_SANG, LightAnalysis.fromArgb(pixels).verdict)
    }

    @Test
    fun `mang rong tra ve ket qua rong thay vi nem loi`() {
        val r = LightAnalysis.fromArgb(IntArray(0))
        assertEquals(0, r.meanLuma)
        assertEquals(0f, r.clippedHighFraction, 0f)
    }
}
