package com.example.service

import org.junit.Assert.assertEquals
import org.junit.Test

class OpusPacketQueueTest {
    @Test
    fun buffersUntilConnected() {
        val sent = mutableListOf<Pair<Int, ByteArray>>()
        val queue = OpusPacketQueue { seq, data -> sent += seq to data }

        queue.queue(byteArrayOf(1))
        queue.queue(byteArrayOf(2))
        assertEquals(0, sent.size)
        assertEquals(2, queue.size())

        queue.setConnected(true)
        assertEquals(2, sent.size)
        // Packets remain until acknowledged
        assertEquals(2, queue.size())
    }

    @Test
    fun resendsAfterReconnectAndAckClears() {
        val sent = mutableListOf<Int>()
        val queue = OpusPacketQueue { seq, _ -> sent += seq }

        queue.setConnected(true)
        queue.queue(byteArrayOf(1))
        queue.queue(byteArrayOf(2))
        assertEquals(listOf(0, 1), sent)

        sent.clear()
        queue.setConnected(false)
        queue.setConnected(true)
        assertEquals(listOf(0, 1), sent)

        queue.ack(0)
        sent.clear()
        queue.setConnected(false)
        queue.setConnected(true)
        assertEquals(listOf(1), sent)
    }
}
