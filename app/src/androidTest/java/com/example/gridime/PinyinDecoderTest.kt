package com.example.gridime

import android.content.ComponentName
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PinyinDecoderTest {
    @Test
    fun installedPreviewUsesForkPackageAndLabels() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pm = context.packageManager
        assertEquals("io.github.dakingeman.inlay.pinyin", context.packageName)
        assertEquals("Inlay Pinyin", pm.getApplicationLabel(context.applicationInfo).toString())
        val service = pm.getServiceInfo(ComponentName(context, GridKeyboardService::class.java), 0)
        assertEquals("Inlay Pinyin 键盘", service.loadLabel(pm).toString())
    }

    @Test
    fun bundledDictionaryReturnsSimplifiedPhrasesOnFirstUse() {
        for ((spelling, expected) in listOf("nihao" to "你好", "youxi" to "游戏", "zhongwen" to "中文")) {
            val candidates = decode(spelling)
            assertTrue("$spelling should include $expected, got $candidates", PinyinCandidate(expected, spelling.length) in candidates)
        }
    }

    private fun decode(spelling: String): List<PinyinCandidate> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val settled = CountDownLatch(1)
        var result: List<PinyinCandidate>? = null
        PinyinDecoder.request(context, spelling, { true }) {
            result = it
            settled.countDown()
        }
        assertTrue("Timed out decoding $spelling", settled.await(90, TimeUnit.SECONDS))
        assertNotNull(result)
        return result.orEmpty()
    }
}
