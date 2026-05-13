package com.jassin.customdrome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jassin.customdrome.ui.theme.CustomDromeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Global Settings / Prefrences object
        val userPrefs = UserPreferences(applicationContext)
        setContent {
            CustomDromeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(userPrefs)
                }
            }
        }
    }
}
