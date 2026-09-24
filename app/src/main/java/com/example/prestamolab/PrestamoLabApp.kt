package com.example.prestamolab

import android.app.Application
import com.example.prestamolab.di.AppContainer

class PrestamoLabApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
