# sonara_core

Native performance core for Sonara, exported to Kotlin through JNI.

| Module | Description |
| :----- | :---------- |
| `BufferCache` | Bounded LRU cache of decoded PCM16 buffers keyed by song id. |
| `nativeParseTags` | Metadata/tag extraction via [`lofty`](https://crates.io/crates/lofty). |
| `nativeSpectrum` | FFT magnitude spectrum (`rustfft`) for 60/120 FPS visualizers. |

Every exported function is wrapped in `catch_unwind` and returns an error/null sentinel instead of
panicking across the FFI boundary. The Kotlin bridge (`SonaraNativeCore.kt`) loads the library with
`try { System.loadLibrary("sonara_core") } catch (e: UnsatisfiedLinkError)`, and falls back to
pure-Kotlin implementations when the `.so` is absent.

## Build

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cargo build --release --target aarch64-linux-android
```

Copy `target/<triple>/release/libsonara_core.so` into `app/src/main/jniLibs/<abi>/`.

The crate is optional: a source-only checkout without the Rust toolchain still builds and runs the
app using the Kotlin fallback paths.
