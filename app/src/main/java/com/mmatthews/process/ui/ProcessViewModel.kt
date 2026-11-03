package com.mmatthews.process.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mmatthews.process.data.AppOpen
import com.mmatthews.process.data.Entry
import com.mmatthews.process.data.EntryStore
import com.mmatthews.process.data.Outcome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One per app open (it lives as long as the activity, including rotation). */
class ProcessViewModel(private val store: EntryStore, private val now: () -> Long) : ViewModel() {

    /** Null while deciding; then [Routes.CHECK] or [Routes.BREATHE]. */
    var startRoute by mutableStateOf<String?>(null)
        private set
    var check by mutableStateOf<Entry?>(null)
        private set

    var happened by mutableStateOf("")
    var story by mutableStateOf<String?>(null)
    var nextTime by mutableStateOf("")

    private var saving = false

    val rules: StateFlow<List<Entry>> =
        store.rules().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val due = AppOpen(store).takeCheck(now())
            check = due
            startRoute = if (due != null) Routes.CHECK else Routes.BREATHE
        }
    }

    fun answer(outcome: Outcome) {
        val entry = check ?: return
        check = null
        viewModelScope.launch { store.answer(entry.id, outcome) }
    }

    fun done(onSaved: () -> Unit) {
        val s = story ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            store.save(happened, s, nextTime, now())
            onSaved()
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch { store.deleteRule(id) }
    }

    companion object {
        fun factory(store: EntryStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProcessViewModel(store, System::currentTimeMillis) }
        }
    }
}
