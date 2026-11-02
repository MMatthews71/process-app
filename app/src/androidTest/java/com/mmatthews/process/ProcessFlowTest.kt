package com.mmatthews.process

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mmatthews.process.data.AppDatabase
import com.mmatthews.process.data.SettingsRepository
import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.EntryType
import com.mmatthews.process.domain.ReframeType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/** The full PROCESS flow completes in 4 screens + the ending, and saves one entry. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProcessFlowTest {

    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var db: AppDatabase

    @Before
    fun setUp() {
        hilt.inject()
        runBlocking {
            settings.clearDraft()
            settings.completeOnboarding()
            settings.setLastReframeTab(ReframeType.TRUER)
        }
    }

    private fun waitForText(text: String, timeoutMs: Long = 5_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun fullFlowCompletesInFourScreensPlusEnding() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Home
            waitForText("Process it")
            compose.onNodeWithText("Process it").performClick()

            // Screen 1 — Name it
            waitForText("What are you feeling?")
            compose.onNodeWithText("Anxious").performClick()
            compose.onNodeWithText("on edge").performClick()
            compose.onNodeWithText("How strong is the on edge right now?").assertExists()
            compose.onNodeWithText("Next").performClick()

            // Screen 2 — Body (Skip appears after 20 s)
            waitForText("In through the nose")
            waitForText("Skip", timeoutMs = 25_000)
            compose.onNodeWithText("Skip").performClick()

            // Screen 3 — Story
            waitForText("What happened?")
            compose.onNodeWithTag("whatHappened").performTextInput("I said hi and they walked past")
            compose.onNodeWithText("Next").performClick()
            waitForText("What's the story?")
            compose.onNodeWithText("They don't like me").performScrollTo().performClick()
            waitForText("Which part of this story might be true")
            compose.onNodeWithTag("newFact").performTextInput("Sam asked me to lunch")
            compose.onNodeWithText("Next").performClick()

            // Screen 4 — Act
            waitForText("Pick one.")
            compose.onNodeWithText("If-then plan").performScrollTo().performClick()
            compose.onNodeWithTag("ifCue").performTextInput("someone walks past")
            compose.onNodeWithTag("ifAction").performTextInput("say hi to the next person")
            compose.onNodeWithText("Save").performClick()

            // Ending
            waitForText("How strong now? Optional")
            compose.onNodeWithText("Next").performClick()
            waitForText("Not now")
            // Hold the clock so the app doesn't close itself before we check the final screen.
            compose.mainClock.autoAdvance = false
            compose.onNodeWithText("Not now").performClick()
            compose.mainClock.advanceTimeBy(500)
            compose.onNodeWithText("Done. Back to your day.").assertExists()
            compose.mainClock.autoAdvance = true
        }

        // The last write (helper answer) is async; give it a moment.
        var entries = runBlocking { db.entryDao().all() }
        val deadline = System.currentTimeMillis() + 5_000
        while (entries.singleOrNull()?.helperDone == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
            entries = runBlocking { db.entryDao().all() }
        }
        assertEquals(1, entries.size)
        val e = entries.single()
        assertEquals(EntryType.PROCESS, e.type)
        assertEquals("on edge", e.feelingPrecise)
        assertEquals(6, e.intensityBefore)
        assertEquals("They don't like me", e.storyChip)
        assertEquals(ReframeType.TRUER, e.reframeType)
        assertEquals("Sam asked me to lunch", e.newFact)
        assertEquals(ActionType.IF_THEN, e.actionType)
        assertEquals("say hi to the next person", e.ifAction)
        assertEquals(false, e.helperDone)
    }
}
