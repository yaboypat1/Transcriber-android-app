package com.example.transcriber

import com.example.ingest.JitterBuffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JitterBufferTest {
    @Test
    fun outOfOrderFramesAreEmittedInTimeOrder() {
        val jb = JitterBuffer(delayMs = 20)
        val now = System.currentTimeMillis()

        val pkt3 = jb.AudioPacket(shortArrayOf(3, 3), voiceActivity = true, timestamp = now - 10)
        val pkt1 = jb.AudioPacket(shortArrayOf(1, 1), voiceActivity = true, timestamp = now - 30)
        val pkt2 = jb.AudioPacket(shortArrayOf(2, 2), voiceActivity = true, timestamp = now - 20)

        // Add out of order
        jb.add(pkt3)
        jb.add(pkt1)
        jb.add(pkt2)

        // First available should be pkt1, then pkt2, then pkt3
        val first = jb.nextFrame()
        assertArrayEquals(shortArrayOf(1, 1), first)

        val second = jb.nextFrame()
        assertArrayEquals(shortArrayOf(2, 2), second)

        val third = jb.nextFrame()
        assertArrayEquals(shortArrayOf(3, 3), third)

        // No extra frames
        assertNull(jb.nextFrame())
    }

    @Test
    fun voiceActivityEndIsDetected() {
        val jb = JitterBuffer(delayMs = 0)
        val now = System.currentTimeMillis()

        jb.add(jb.AudioPacket(shortArrayOf(1), voiceActivity = true, timestamp = now))
        jb.nextFrame()
        assertTrue(!jb.voiceActivityEnded())

        jb.add(jb.AudioPacket(shortArrayOf(2), voiceActivity = false, timestamp = now + 5))
        jb.nextFrame()
        assertTrue(jb.voiceActivityEnded())
    }
}

