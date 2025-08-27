# Native Libraries

This directory should contain the compiled native ASR library for different CPU architectures.

## Required Files

For each architecture directory, you need to add:
- `libasr.so` - The compiled ASR native library

## How to Build

The native ASR library should be compiled from C/C++ source code using the Android NDK.
The library should export these JNI functions:

- `Java_com_example_session_AsrSession_nativeCreate()`
- `Java_com_example_session_AsrSession_nativePush()`
- `Java_com_example_session_AsrSession_nativePartial()`
- `Java_com_example_session_AsrSession_nativeFinal()`
- `Java_com_example_session_AsrSession_nativeClose()`

## Placeholder

Until the native library is built, you can create empty placeholder files:
```bash
touch arm64-v8a/libasr.so
touch armeabi-v7a/libasr.so
touch x86/libasr.so
touch x86_64/libasr.so
```

This will allow the project to build, though the ASR functionality won't work.
