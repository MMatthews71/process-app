package com.mmatthews.process.ui.process

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmatthews.process.domain.Step

@Composable
fun ProcessRoute(onExit: () -> Unit, onFinished: () -> Unit, vm: ProcessViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val exit by rememberUpdatedState(onExit)
    LaunchedEffect(vm) { vm.exit.collect { exit() } }
    val state = ui ?: return
    BackHandler(enabled = state.draft.step != Step.DONE) { vm.back() }
    ProcessFlowContent(state, vm, onFinished)
}
