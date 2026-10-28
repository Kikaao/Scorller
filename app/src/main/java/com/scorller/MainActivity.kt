package com.scorller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.scorller.nav.ScorllerNavGraph
import com.scorller.ui.theme.ScorllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScorllerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScorllerNavGraph()
                }
            }
        }
    }
}
