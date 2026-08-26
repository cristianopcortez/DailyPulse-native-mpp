package com.petros.efthymiou.dailypulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

actual open class BaseViewModel : ViewModel {
    actual constructor() : super()

    actual val scope = viewModelScope
}
