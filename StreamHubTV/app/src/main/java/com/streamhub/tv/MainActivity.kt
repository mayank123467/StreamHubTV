package com.streamhub.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.streamhub.tv.ui.MainScreen
import com.streamhub.tv.ui.StreamHubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: StreamHubViewModel = viewModel()
            MainScreen(vm)
        }
    }
}
