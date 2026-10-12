package com.thelazybattley.hiddenridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.thelazybattley.hiddenridge.core.designsystem.theme.HiddenRidgeTheme
import com.thelazybattley.hiddenridge.ui.HiddenRidgeApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiddenRidgeTheme {
                HiddenRidgeApp()
            }
        }
    }
}
