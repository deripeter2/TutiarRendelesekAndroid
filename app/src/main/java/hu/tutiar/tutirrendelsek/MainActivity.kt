package hu.tutiar.tutirrendelsek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import hu.tutiar.tutirrendelsek.ui.TutiarApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TutiarApp()
        }
    }
}
