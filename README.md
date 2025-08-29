# Transcriber Android App

A "phone-in-pocket, watch-on-wrist" live translator. The Wear OS watch captures audio continuously and streams it to the paired phone where speech recognition, language identification, and translation run on-device. Final transcripts are stored for later review and can be reprocessed with a larger model when the phone is charging.

## Status

- Features: JitterBuffer, Room (TranscriptSegment), WorkManager post-processing, on-device ML Kit Language ID, Wear ↔ Phone pairing via MessageClient with ChannelClient streaming for bursts.
- One-command build: `./gradlew :app-mobile:assembleDebug :app-wear:assembleDebug`
- Lint/analysis: ktlint + Detekt wired into `check`
- CI: `.github/workflows/android.yml` for self-hosted runner (Ubuntu, Android)

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
- Sends small packets via `MessageClient`, and uses `ChannelClient` for large bursts with backpressure.

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
./gradlew ktlintFormat detekt
./gradlew test
```

### Versions

Key versions from `gradle/libs.versions.toml`:

- Android Gradle Plugin: 8.12.1
- Gradle Wrapper: 8.13
- Kotlin: 2.0.21 (JDK 21 toolchain)
- Compose BOM: 2024.10.00 (compiler via Kotlin Compose plugin 2.0.21)
- Room: 2.6.1
- WorkManager: 2.9.0
- ML Kit language-id: 17.0.4

### Troubleshooting

- Ensure JDK 21 is installed and selected: `java -version`
- Android SDK: set `sdk.dir` in `local.properties`
- Self-hosted runner labels: `self-hosted`, `ubuntu`, `android`
