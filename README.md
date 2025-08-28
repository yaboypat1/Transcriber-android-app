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

## Components

- **Jitter buffer** smooths incoming audio before recognition.
- **Room entities** like `TranscriptSegment` persist transcripts.
- **Post-process worker** revisits audio with a heavier ASR model.
- **ML Kit Language ID** detects the spoken language prior to translation.

## Development

This repository uses Gradle with a centralized version catalog at `gradle/libs.versions.toml`. Dependencies and plugin versions are referenced through the `libs` catalog in the build scripts.

Install the phone and watch apps on paired devices:

```
./gradlew :app-mobile:installDebug :app-wear:installDebug
```

To pair a Wear OS emulator, use:

```
adb pair <emulator-ip>:<port>
adb connect <emulator-ip>:<port>
```

Run tests with:

```
./gradlew test
```

### Versions

Key versions from `gradle/libs.versions.toml`:

- Android Gradle Plugin 8.12.1
- Kotlin 2.0.21
- Compose BOM 2024.02.00 (compiler 1.5.10)
- Room 2.6.1 and WorkManager 2.9.0
- ML Kit language-id 17.0.4

