package com.mmatthews.process

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmatthews.process.ui.components.LocalHaptics
import com.mmatthews.process.ui.components.LocalVoiceInput
import com.mmatthews.process.ui.platform.AndroidHaptics
import com.mmatthews.process.ui.platform.rememberAndroidVoiceInput
import com.mmatthews.process.ui.theme.ProcessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vm.resolveStart(openProcess = intent?.action == ACTION_PROCESS_IT)
        setContent {
            ProcessTheme {
                val start by vm.startRoute.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val haptics = remember { AndroidHaptics(context) }
                val voice = rememberAndroidVoiceInput()
                CompositionLocalProvider(
                    LocalVoiceInput provides voice,
                    LocalHaptics provides haptics,
                ) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                            start?.let { AppNav(startRoute = it, onFinished = { finishAndRemoveTask() }) }
                        }
                    }
                }
            }
        }
    }

    companion object {
        /** Used by the launcher shortcut and the Quick Settings tile. */
        const val ACTION_PROCESS_IT = "com.mmatthews.process.action.PROCESS_IT"
    }
}
