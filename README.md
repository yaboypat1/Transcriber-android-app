# Transcriber Android App

A "phone-in-pocket, watch-on-wrist" live translator. The Wear OS watch captures audio continuously and streams it to the paired phone where speech recognition, language identification, and translation run on-device. Final transcripts are stored for later review and can be reprocessed with a larger model when the phone is charging.

## Architecture

```
[Wear OS Watch]
  Mic ForegroundService → VAD → Opus → Data Layer stream
                            │
                            ▼
[Android Phone]
  Receiver → jitter buffer → ASR → LID → translation → Room storage
                                      │
                                      └─ post-process worker (heavy model)
```

### Watch (`app-wear`)

- Foreground service with microphone service type.
- Uses WebRTC VAD to skip silence and encodes 16 kHz mono audio with Opus.
- Sends small packets through the Wear OS Data Layer to the phone.

### Phone (`app-mobile`)

- Data Layer listener decodes packets and feeds a streaming ASR engine.
- Partial transcripts can drive live captions; final segments are saved along with translation.
- A WorkManager job can re-run segments with a larger model for better accuracy.

## Status

The repository currently contains scaffolding for the wearable service, the phone listener, and a translation helper. Buffering, full ASR integration, Room storage, and the post-processing worker still need implementation.

## Development

This repository uses Gradle. Run tests with:

```
./gradlew test
```