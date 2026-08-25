package com.petros.efthymiou.dailypulse.network

/**
 * Default for the Android emulator (host loopback). Overridden when
 * [BffBuildConfig.OVERRIDE_BASE_URL] is set (local.properties or CI).
 */
actual val bffBaseUrl: String =
    BffBuildConfig.OVERRIDE_BASE_URL.ifBlank { "http://10.0.2.2:8080" }
