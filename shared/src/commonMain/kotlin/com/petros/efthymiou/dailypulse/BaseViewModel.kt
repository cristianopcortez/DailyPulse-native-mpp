package com.petros.efthymiou.dailypulse

import kotlinx.coroutines.CoroutineScope

expect open class BaseViewModel {
    constructor()

    val scope: CoroutineScope
}
