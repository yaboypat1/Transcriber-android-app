package com.example.transcriber

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.language.LanguageIdWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageIdWrapperTest {
    @Test
    fun returnsNullWhenModelNotReady() = kotlinx.coroutines.runBlocking {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val lid = LanguageIdWrapper(ctx) { throw IllegalStateException("not available in test") }
        lid.markModelNotReady()
        val result = lid.identify("hello world")
        assertNull(result)
    }

    @Test
    fun allowsMarkReadyAndDeferOnBlank() = kotlinx.coroutines.runBlocking {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        val lid = LanguageIdWrapper(ctx) { throw IllegalStateException("not available in test") }
        lid.markModelReady()
        val blank = lid.identify("")
        assertNull(blank)
    }
}

