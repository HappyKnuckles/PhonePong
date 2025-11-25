package com.example.insanecrossmobilepingpongapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.insanecrossmobilepingpongapp.sensor.AndroidMotionSensor
import com.example.insanecrossmobilepingpongapp.util.ContextProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextProvider.init(this)

        setContent {
            App()
        }
    }
}