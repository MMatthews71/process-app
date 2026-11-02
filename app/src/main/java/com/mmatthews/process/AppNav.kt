package com.mmatthews.process

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmatthews.process.ui.good.GoodThingRoute
import com.mmatthews.process.ui.home.HomeRoute
import com.mmatthews.process.ui.onboarding.OnboardingRoute
import com.mmatthews.process.ui.process.ProcessRoute

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PROCESS = "process"
    const val GOOD = "good"
}

@Composable
fun AppNav(startRoute: String, onFinished: () -> Unit, nav: NavHostController = rememberNavController()) {
    val goHome = {
        nav.navigate(Routes.HOME) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    NavHost(navController = nav, startDestination = startRoute) {
        composable(Routes.ONBOARDING) { OnboardingRoute(onDone = goHome) }
        composable(Routes.HOME) {
            HomeRoute(
                onProcess = { nav.navigate(Routes.PROCESS) },
                onGood = { nav.navigate(Routes.GOOD) },
            )
        }
        composable(Routes.PROCESS) { ProcessRoute(onExit = goHome, onFinished = onFinished) }
        composable(Routes.GOOD) { GoodThingRoute(onDone = goHome) }
    }
}
