package com.example.transcriber

import com.example.speech.DefaultSpeechInputController
import com.example.speech.FakeRecognizerBackend
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechInputControllerTest {
    @Test
    fun controllerPropagatesEventsAndRestartsOnError() {
        val backend = FakeRecognizerBackend()
        val controller = DefaultSpeechInputController(backend)

        var partial = ""
        var final = ""
        var error = ""
        controller.setOnPartial { partial = it }
        controller.setOnFinal { final = it }
        controller.setOnError { error = it }

        controller.start()
        backend.emitPartial("he")
        backend.emitFinal("hello")
        backend.emitError("boom")

        assertEquals("he", partial)
        assertEquals("hello", final)
        assertEquals("boom", error)
        // Restart on error should have stopped and started once
        assertEquals(1, backend.startedCount)
        assertEquals(1, backend.stoppedCount)
    }
}

