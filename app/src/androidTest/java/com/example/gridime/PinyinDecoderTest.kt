package com.example.gridime

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PinyinDecoderTest {
    @Test
    fun bundledDictionaryReturnsSimplifiedPhrasesOnFirstUse() {
        assertTrue(decode("nihao").contains(PinyinCandidate("你好", 5)))
        assertTrue(decode("youxi").contains(PinyinCandidate("游戏", 5)))
        assertTrue(decode("zhongwen").contains(PinyinCandidate("中文", 8)))
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
