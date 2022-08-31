package com.example.finito.core

import androidx.annotation.StringRes

data class ResourceException(@StringRes val error: Int) : Exception()