package com.xiaogong.csestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xiaogong.csestudy.ui.navigation.AppNavigation
import com.xiaogong.csestudy.ui.theme.CertifiedSafetyEngineerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CseApplication
        setContent {
            CertifiedSafetyEngineerTheme {
                AppNavigation(application = app)
            }
        }
    }
}
