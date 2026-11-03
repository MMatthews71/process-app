package com.mmatthews.process

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmatthews.process.data.NextDayCheck
import com.mmatthews.process.data.Stories
import com.mmatthews.process.ui.BreatheScreen
import com.mmatthews.process.ui.CheckScreen
import com.mmatthews.process.ui.LocalPalette
import com.mmatthews.process.ui.NextTimeScreen
import com.mmatthews.process.ui.ProcessTheme
import com.mmatthews.process.ui.ProcessViewModel
import com.mmatthews.process.ui.Routes
import com.mmatthews.process.ui.RulesScreen
import com.mmatthews.process.ui.SplitScreen

class MainActivity : ComponentActivity() {

    private val vm: ProcessViewModel by viewModels {
        ProcessViewModel.factory((application as ProcessApp).store)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val buzz = Buzz(this)
        setContent {
            ProcessTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(LocalPalette.current.background)
                        .safeDrawingPadding(),
                ) {
                    vm.startRoute?.let { start ->
                        AppNav(start, vm, buzz, onClose = { finishAndRemoveTask() })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNav(start: String, vm: ProcessViewModel, buzz: Buzz, onClose: () -> Unit) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = start,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Routes.CHECK) {
            val entry = vm.check
            CheckScreen(
                headline = entry?.let(NextDayCheck::headline).orEmpty(),
                onAnswer = { outcome ->
                    vm.answer(outcome)
                    nav.navigate(Routes.BREATHE) { popUpTo(Routes.CHECK) { inclusive = true } }
                },
            )
        }
        composable(Routes.BREATHE) {
            BreatheScreen(
                onPhase = { buzz.short() },
                onFinished = { nav.navigate(Routes.SPLIT) },
                onRules = { nav.navigate(Routes.RULES) },
            )
        }
        composable(Routes.SPLIT) {
            SplitScreen(
                happened = vm.happened,
                onHappened = { vm.happened = it },
                stories = Stories.all,
                story = vm.story,
                onStory = { vm.story = it },
                onNext = { nav.navigate(Routes.NEXT) },
            )
        }
        composable(Routes.NEXT) {
            NextTimeScreen(
                nextTime = vm.nextTime,
                onNextTime = { vm.nextTime = it },
                onDone = {
                    vm.done {
                        buzz.short()
                        onClose()
                    }
                },
            )
        }
        composable(Routes.RULES) {
            val rules by vm.rules.collectAsStateWithLifecycle()
            RulesScreen(rules, vm::deleteRule)
        }
    }
}

private class Buzz(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun short() {
        val v = vibrator ?: return
        if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
