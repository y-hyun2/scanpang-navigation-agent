package com.scanpang.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.scanpang.app.navigation.ScanPangRoot
import com.scanpang.app.ui.theme.ScanPangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanPangTheme {
                ScanPangRoot()
            }
        }
    }
}
