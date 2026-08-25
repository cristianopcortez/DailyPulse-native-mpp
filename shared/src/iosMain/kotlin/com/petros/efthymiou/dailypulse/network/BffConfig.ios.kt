package com.petros.efthymiou.dailypulse.network

/**
 * Default for the iOS Simulator. Overridden when
 * [BffBuildConfig.OVERRIDE_BASE_URL] is set (local.properties or CI).
 */
actual val bffBaseUrl: String =
    BffBuildConfig.OVERRIDE_BASE_URL.ifBlank { "http://localhost:8080" }
