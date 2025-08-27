package com.example.service

/**
 * Maintains a buffer of Opus packets and coordinates resend logic.
 *
 * Packets are enqueued with [queue] and will be sent via the provided
 * [sender] function whenever a connection to the phone is available.
 * Packets remain pending until [ack] is invoked with their sequence id.
 */
class OpusPacketQueue(private val sender: (Int, ByteArray) -> Unit) {
    private val pending = linkedMapOf<Int, ByteArray>()
    private var nextSeq = 0
    private var connected = false

    /** Queue a packet for transmission. */
    fun queue(packet: ByteArray) {
        val seq = nextSeq++
        pending[seq] = packet
        if (connected) {
            sender(seq, packet)
        }
    }

    /** Mark a packet as acknowledged by the phone. */
    fun ack(seq: Int) {
        pending.remove(seq)
    }

    /** Update connection status; flushing any pending packets when connected. */
    fun setConnected(value: Boolean) {
        connected = value
        if (connected) {
            flush()
        }
    }

    /** Re-sends all packets that have not been acknowledged. */
    fun flush() {
        if (!connected) return
        for ((seq, data) in pending) {
            sender(seq, data)
        }
    }

    /** Number of packets still waiting for acknowledgment. */
    fun size(): Int = pending.size

    /** Snapshot of all pending packet payloads. */
    fun pendingPackets(): List<ByteArray> = pending.values.toList()
}
