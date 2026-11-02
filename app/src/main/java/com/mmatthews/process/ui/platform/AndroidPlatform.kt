package com.mmatthews.process.ui.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mmatthews.process.ui.components.Haptics
import com.mmatthews.process.ui.components.VoiceInput

/**
 * Speech-to-text with SpeechRecognizer. Uses the on-device recognizer on API 31+ when
 * available, otherwise (or if the on-device language pack is missing) the standard one.
 * RECORD_AUDIO is requested on the first mic tap.
 */
class AndroidVoiceInput(private val context: Context) : VoiceInput {
    private var active by mutableStateOf<String?>(null)
    override val activeKey: String? get() = active

    internal var requestPermission: (() -> Unit)? = null
    private var pending: Pair<String, (String) -> Unit>? = null
    private var recognizer: SpeechRecognizer? = null
    private val main = Handler(Looper.getMainLooper())

    override fun start(key: String, onText: (String) -> Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            pending = key to onText
            requestPermission?.invoke()
            return
        }
        listen(key, onText, preferOnDevice = true)
    }

    internal fun onPermissionResult(granted: Boolean) {
        val p = pending ?: return
        pending = null
        if (granted) listen(p.first, p.second, preferOnDevice = true)
    }

    override fun stop() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        active = null
    }

    private fun listen(key: String, onText: (String) -> Unit, preferOnDevice: Boolean) {
        destroy()
        val onDevice = preferOnDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        if (!onDevice && !SpeechRecognizer.isRecognitionAvailable(context)) {
            toast("Voice input isn't available on this phone.")
            return
        }
        val r = if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        recognizer = r
        active = key
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) onText(text)
                main.post { if (recognizer === r) destroy() }
            }

            override fun onError(error: Int) {
                main.post {
                    if (recognizer !== r) return@post
                    if (onDevice && error in ON_DEVICE_FALLBACK_ERRORS) {
                        listen(key, onText, preferOnDevice = false)
                        return@post
                    }
                    destroy()
                    if (error !in QUIET_ERRORS) toast("Couldn't hear that. Try again or type.")
                }
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    private fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    private companion object {
        // ERROR_SERVER_DISCONNECTED, ERROR_LANGUAGE_NOT_SUPPORTED, ERROR_LANGUAGE_UNAVAILABLE (API 31)
        val ON_DEVICE_FALLBACK_ERRORS = setOf(11, 12, 13)
        val QUIET_ERRORS = setOf(
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            SpeechRecognizer.ERROR_CLIENT,
        )
    }
}

@Composable
fun rememberAndroidVoiceInput(): VoiceInput {
    val context = LocalContext.current
    val input = remember { AndroidVoiceInput(context.applicationContext) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        input.onPermissionResult(it)
    }
    SideEffect { input.requestPermission = { launcher.launch(Manifest.permission.RECORD_AUDIO) } }
    DisposableEffect(input) { onDispose { input.destroy() } }
    return input
}

class AndroidHaptics(context: Context) : Haptics {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    override fun pulse(millis: Long, strong: Boolean) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val amplitude = if (v.hasAmplitudeControl()) (if (strong) 200 else 70) else VibrationEffect.DEFAULT_AMPLITUDE
        v.vibrate(VibrationEffect.createOneShot(millis, amplitude))
    }
}
