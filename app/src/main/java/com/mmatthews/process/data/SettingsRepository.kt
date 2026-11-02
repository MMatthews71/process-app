package com.mmatthews.process.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mmatthews.process.domain.AppSettings
import com.mmatthews.process.domain.CrisisLine
import com.mmatthews.process.domain.Defaults
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.domain.ReframeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(private val store: DataStore<Preferences>) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val BREATH_CYCLES = intPreferencesKey("breath_cycles")
        val FEELINGS = stringPreferencesKey("feelings_json")
        val CUSTOM_CHIPS = stringPreferencesKey("custom_chips_json")
        val HELPERS = stringPreferencesKey("helpers_json")
        val CRISIS = stringPreferencesKey("crisis_lines_json")
        val LAST_TAB = stringPreferencesKey("last_reframe_tab")
        val WORRY_SLOT = booleanPreferencesKey("worry_slot_enabled")
        val LAST_HELPER = stringPreferencesKey("last_helper")
        val DRAFT = stringPreferencesKey("process_draft_json")
    }

    private val prefs: Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val settings: Flow<AppSettings> = prefs.map { p ->
        AppSettings(
            onboarded = p[Keys.ONBOARDED] ?: false,
            name = p[Keys.NAME].orEmpty(),
            breathCycles = p[Keys.BREATH_CYCLES] ?: Defaults.BREATH_CYCLES,
            feelings = decode<Map<String, List<String>>>(p[Keys.FEELINGS]) ?: Defaults.feelings,
            customChips = decode<List<String>>(p[Keys.CUSTOM_CHIPS]) ?: emptyList(),
            helpers = decode<List<String>>(p[Keys.HELPERS]) ?: Defaults.helpers,
            crisisLines = decode<List<CrisisLine>>(p[Keys.CRISIS]) ?: Defaults.crisisLines,
            lastReframeTab = p[Keys.LAST_TAB]
                ?.let { runCatching { ReframeType.valueOf(it) }.getOrNull() }
                ?.takeIf { it != ReframeType.NONE } ?: ReframeType.TRUER,
            worrySlotEnabled = p[Keys.WORRY_SLOT] ?: false,
            lastHelper = p[Keys.LAST_HELPER],
        )
    }.distinctUntilChanged()

    private inline fun <reified T> decode(raw: String?): T? =
        raw?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }

    suspend fun completeOnboarding(name: String) {
        store.edit {
            it[Keys.NAME] = name.trim()
            it[Keys.ONBOARDED] = true
        }
    }

    suspend fun setLastReframeTab(tab: ReframeType) {
        store.edit { it[Keys.LAST_TAB] = tab.name }
    }

    suspend fun setLastHelper(helper: String) {
        store.edit { it[Keys.LAST_HELPER] = helper }
    }

    suspend fun loadDraft(): ProcessDraft? = decode<ProcessDraft>(prefs.first()[Keys.DRAFT])

    suspend fun saveDraft(draft: ProcessDraft) {
        store.edit { it[Keys.DRAFT] = json.encodeToString(ProcessDraft.serializer(), draft) }
    }

    suspend fun clearDraft() {
        store.edit { it.remove(Keys.DRAFT) }
    }
}
