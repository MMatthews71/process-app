package com.mmatthews.process.ui.good

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmatthews.process.data.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class GoodThingViewModel @Inject constructor(
    private val entries: EntryRepository,
    private val clock: Clock,
) : ViewModel() {
    fun save(text: String, then: () -> Unit) {
        if (text.isBlank()) return
        viewModelScope.launch {
            entries.addGood(text, clock.millis())
            then()
        }
    }
}

@Composable
fun GoodThingRoute(onDone: () -> Unit, vm: GoodThingViewModel = hiltViewModel()) {
    var text by rememberSaveable { mutableStateOf("") }
    GoodThingScreen(text = text, onText = { text = it }, onSave = { vm.save(text, onDone) }, onBack = onDone)
}
