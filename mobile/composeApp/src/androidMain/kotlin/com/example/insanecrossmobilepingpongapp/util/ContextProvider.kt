package com.example.insanecrossmobilepingpongapp.util

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextProvider {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getContext(): Context {
        return context ?: throw IllegalStateException("Context not initialized. Call ContextProvider.init(context) in Application or Activity onCreate.")
    }
}
