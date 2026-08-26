package com.petros.efthymiou.dailypulse

expect class Platform {
    constructor()

    val osName: String
    val osVersion: String
    val deviceModel: String
    val density: Int

    fun logSystemInfo()
}
