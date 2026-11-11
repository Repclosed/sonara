//! Sonara native performance core.
//!
//! Responsibilities:
//! * bounded in-memory cache/index for decoded audio buffers (`BufferCache`)
//! * metadata / tag extraction via `lofty`
//! * FFT magnitude spectrum for the 60/120 FPS visualizer
//!
//! Every exported symbol is JNI-safe: no panic may cross the FFI boundary, the Kotlin side
//! degrades to its pure-Kotlin fallback when this library is missing or an entry point fails.

use std::collections::HashMap;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::sync::Mutex;

use jni::objects::{JByteArray, JClass, JString};
use jni::sys::{jbyteArray, jfloatArray, jint, jlong, jstring};
use jni::JNIEnv;
use once_cell::sync::Lazy;
use rustfft::num_complex::Complex;
use rustfft::FftPlanner;

/// Maximum number of cached buffers. Keeps the native heap bounded on low-memory devices.
const MAX_CACHE_ENTRIES: usize = 64;

struct CacheEntry {
    data: Vec<i16>,
    last_touch: u64,
}

struct CacheState {
    entries: HashMap<i64, CacheEntry>,
    clock: u64,
}

static CACHE: Lazy<Mutex<CacheState>> = Lazy::new(|| {
    Mutex::new(CacheState {
        entries: HashMap::new(),
        clock: 0,
    })
});

fn with_cache<R>(f: impl FnOnce(&mut CacheState) -> R) -> R {
    let mut guard = CACHE.lock().unwrap_or_else(|poisoned| poisoned.into_inner());
    f(&mut guard)
}

fn evict_lru(state: &mut CacheState) {
    while state.entries.len() > MAX_CACHE_ENTRIES {
        let victim = state
            .entries
            .iter()
            .min_by_key(|(_, entry)| entry.last_touch)
            .map(|(key, _)| *key);
        match victim {
            Some(key) => {
                state.entries.remove(&key);
            }
            None => break,
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeInit(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    match catch_unwind(AssertUnwindSafe(|| {
        with_cache(|state| state.entries.clear());
        1
    })) {
        Ok(value) => value,
        Err(_) => 0,
    }
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeCachePut(
    env: JNIEnv,
    _class: JClass,
    song_id: jlong,
    pcm: jbyteArray,
) -> jint {
    let Ok(bytes) = env.convert_byte_array(JByteArray::from(pcm)) else {
        return -1;
    };
    let samples: Vec<i16> = bytes
        .chunks_exact(2)
        .map(|chunk| i16::from_le_bytes([chunk[0], chunk[1]]))
        .collect();

    catch_unwind(AssertUnwindSafe(|| {
        with_cache(|state| {
            state.clock = state.clock.wrapping_add(1);
            let stamp = state.clock;
            state.entries.insert(
                song_id,
                CacheEntry {
                    data: samples,
                    last_touch: stamp,
                },
            );
            evict_lru(state);
            state.entries.len() as jint
        })
    }))
    .unwrap_or(-2)
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeCacheGet(
    env: JNIEnv,
    _class: JClass,
    song_id: jlong,
) -> jbyteArray {
    let payload = catch_unwind(AssertUnwindSafe(|| {
        with_cache(|state| {
            state.clock = state.clock.wrapping_add(1);
            let stamp = state.clock;
            match state.entries.get_mut(&song_id) {
                Some(entry) => {
                    entry.last_touch = stamp;
                    let mut bytes = Vec::with_capacity(entry.data.len() * 2);
                    for sample in &entry.data {
                        bytes.extend_from_slice(&sample.to_le_bytes());
                    }
                    Some(bytes)
                }
                None => None,
            }
        })
    }))
    .ok()
    .flatten();

    match payload {
        Some(bytes) => match env.byte_array_from_slice(&bytes) {
            Ok(array) => array.into_raw(),
            Err(_) => std::ptr::null_mut(),
        },
        None => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeCacheRelease(
    _env: JNIEnv,
    _class: JClass,
    song_id: jlong,
) -> jint {
    catch_unwind(AssertUnwindSafe(|| {
        with_cache(|state| {
            let removed = state.entries.remove(&song_id).is_some();
            if removed {
                1
            } else {
                0
            }
        })
    }))
    .unwrap_or(-1)
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeCacheClear(
    _env: JNIEnv,
    _class: JClass,
) {
    let _ = catch_unwind(AssertUnwindSafe(|| {
        with_cache(|state| {
            state.entries.clear();
            state.entries.shrink_to_fit();
        })
    }));
}

/// Tag extraction. Returns a `title\0artist\0album\0duration_ms` blob or null on failure.
#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeParseTags(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
) -> jstring {
    let Ok(path) = env.get_string(&path) else {
        return std::ptr::null_mut();
    };
    let path: String = path.into();

    let parsed = catch_unwind(AssertUnwindSafe(|| {
        use lofty::file::TaggedFileExt;
        use lofty::probe::Probe;
        use lofty::tag::Accessor;

        let tagged = Probe::open(&path).ok()?.read().ok()?;
        let tag = tagged.primary_tag().or_else(|| tagged.first_tag())?;
        let duration = tagged.properties().duration().as_millis() as u64;
        Some(format!(
            "{}\u{0}{}\u{0}{}\u{0}{}",
            tag.title().map(|v| v.to_string()).unwrap_or_default(),
            tag.artist().map(|v| v.to_string()).unwrap_or_default(),
            tag.album().map(|v| v.to_string()).unwrap_or_default(),
            duration
        ))
    }))
    .ok()
    .flatten();

    match parsed {
        Some(value) => match env.new_string(value) {
            Ok(string) => string.into_raw(),
            Err(_) => std::ptr::null_mut(),
        },
        None => std::ptr::null_mut(),
    }
}

/// FFT magnitude spectrum of a mono PCM16 buffer, normalized to 0..1.
///
/// `bands` is clamped to a sane range so a bad caller cannot allocate unbounded memory.
#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeSpectrum(
    env: JNIEnv,
    _class: JClass,
    pcm: jbyteArray,
    bands: jint,
) -> jfloatArray {
    let Ok(bytes) = env.convert_byte_array(JByteArray::from(pcm)) else {
        return std::ptr::null_mut();
    };
    let band_count = bands.clamp(8, 512) as usize;

    let magnitudes = catch_unwind(AssertUnwindSafe(|| {
        let samples: Vec<f32> = bytes
            .chunks_exact(2)
            .map(|chunk| i16::from_le_bytes([chunk[0], chunk[1]]) as f32 / 32768.0)
            .collect();
        if samples.len() < 8 {
            return vec![0f32; band_count];
        }

        let mut buffer: Vec<Complex<f32>> =
            samples.iter().map(|s| Complex::new(*s, 0.0)).collect();
        let mut planner = FftPlanner::<f32>::new();
        planner.plan_fft_forward(buffer.len()).process(&mut buffer);

        let half = buffer.len() / 2;
        let mut out = vec![0f32; band_count];
        for (index, slot) in out.iter_mut().enumerate() {
            let start = index * half / band_count;
            let end = ((index + 1) * half / band_count).max(start + 1);
            let mut peak = 0f32;
            for bin in &buffer[start..end.min(half)] {
                peak = peak.max(bin.norm());
            }
            *slot = (peak / (half as f32).sqrt()).clamp(0.0, 1.0);
        }
        out
    }))
    .unwrap_or_else(|_| vec![0f32; band_count]);

    match env.new_float_array(magnitudes.len() as i32) {
        Ok(array) => {
            if env.set_float_array_region(&array, 0, &magnitudes).is_err() {
                return std::ptr::null_mut();
            }
            array.into_raw()
        }
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_echo_music_iad1tya_utils_SonaraNativeCore_nativeVersion(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    match env.new_string(env!("CARGO_PKG_VERSION")) {
        Ok(string) => string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
