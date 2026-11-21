package com.hts.checkposeapp

import com.hts.checkposeapp.guidance.Band
import com.hts.checkposeapp.guidance.CriterionStatus
import com.hts.checkposeapp.guidance.GateState
import com.hts.checkposeapp.guidance.GuidanceEngine
import com.hts.checkposeapp.guidance.guidanceStepOf
import com.hts.checkposeapp.template.Criterion
import com.hts.checkposeapp.ui.groupRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HuongDanTuanTuTest {

    @Test
    fun `xoay thang may roi lui va chinh goc truoc khi zoom`() {
        val dauVao = listOf(
            Criterion.POSE,
            Criterion.PITCH,
            Criterion.SCALE,
            Criterion.YAW,
            Criterion.PERSPECTIVE,
            Criterion.CENTER,
            Criterion.ELEVATION,
            Criterion.ROLL,
        )
        val ketQua = dauVao.sortedWith(
            compareBy<Criterion> { guidanceStepOf(it).ordinal }.thenBy { it.ordinal }
        )

        assertEquals(Criterion.ROLL, ketQua[0])
        assertEquals(Criterion.PERSPECTIVE, ketQua[1])
        assertTrue(ketQua.indexOf(Criterion.PITCH) < ketQua.indexOf(Criterion.SCALE))
        assertTrue(ketQua.indexOf(Criterion.PITCH) < ketQua.indexOf(Criterion.POSE))
        assertTrue(ketQua.indexOf(Criterion.ELEVATION) < ketQua.indexOf(Criterion.YAW))
    }

    @Test
    fun `dong gop hien dang cho zoom sau khi cho dung da dat`() {
        val band = Band(1.0, 1.5, 3.0, 0.1)
        val rows = groupRows(
            listOf(
                CriterionStatus(Criterion.PERSPECTIVE, GateState.PASSING, 0.0, 0.0, band),
                CriterionStatus(
                    Criterion.SCALE,
                    GateState.UNMEASURED,
                    null,
                    null,
                    band,
                    pending = true,
                ),
            )
        )

        assertEquals(1, rows.size)
        assertTrue(rows.single().second.pending)
    }

    @Test
    fun `sau khoang sau giay moi duoc bo qua tu dong`() {
        assertEquals(
            3 * 2_000L,
            GuidanceEngine.AUTO_SKIP_AFTER_MS,
        )
    }
}
