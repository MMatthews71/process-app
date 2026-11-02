package com.mmatthews.process.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RulesTest {
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    @Test
    fun draftResumesWithin30Minutes() {
        val d = ProcessDraft(startedAt = 0, updatedAt = 0, step = Step.STORY)
        assertTrue(DraftRules.shouldResume(d, 29 * minute))
        assertTrue(DraftRules.shouldResume(d, 30 * minute))
        assertFalse(DraftRules.shouldResume(d, 31 * minute))
        assertFalse(DraftRules.shouldResume(null, 0))
        assertFalse(DraftRules.shouldResume(d.copy(step = Step.DONE), minute))
    }

    @Test
    fun predictionLockedForSevenDaysAfterAnotherTest() {
        val started = 10 * day
        assertNull(PredictionRules.nextAvailableAt(null, thisEventHasPrediction = false, now = started))
        assertEquals(
            started + 7 * day,
            PredictionRules.nextAvailableAt(started, thisEventHasPrediction = false, now = started + 3 * day),
        )
        assertEquals(
            started + 7 * day,
            PredictionRules.nextAvailableAt(started, thisEventHasPrediction = false, now = started + 7 * day - 1),
        )
        assertNull(PredictionRules.nextAvailableAt(started, thisEventHasPrediction = false, now = started + 7 * day))
    }

    @Test
    fun onePredictionPerEventCanStillBeEdited() {
        // The event's own prediction doesn't lock it; it just gets edited in place.
        assertNull(PredictionRules.nextAvailableAt(10 * day, thisEventHasPrediction = true, now = 11 * day))
    }

    @Test
    fun onePredictionPerEventRewritesTheSameEntry() {
        // Saving a prediction, going back and saving again keeps the event's single entry id.
        var d = ProcessDraft(startedAt = 0, step = Step.ACT, storyChip = "They think I'm weird")
        d = Flow.saveAction(Flow.chooseAction(d, ActionType.PREDICTION)).copy(savedEntryId = 42)
        d = Flow.back(d)!!
        d = Flow.saveAction(Flow.chooseAction(d, ActionType.PREDICTION).copy(predictionPercent = 80))
        assertEquals(42L, d.savedEntryId)
        assertEquals(ActionType.PREDICTION, d.actionType)
    }

    @Test
    fun predictionFollowUpDueAfter24Hours() {
        assertFalse(PredictionRules.isFollowUpDue(0, null, 23 * hour))
        assertTrue(PredictionRules.isFollowUpDue(0, null, 24 * hour))
        assertFalse(PredictionRules.isFollowUpDue(0, PredictionOutcome.BETTER, 48 * hour))
    }

    @Test
    fun wordCountIgnoresPunctuationOnlyTokens() {
        assertEquals(0, TextRules.wordCount("   "))
        assertEquals(3, TextRules.wordCount("she  smiled - back"))
        assertEquals(2, TextRules.wordCount("hi there!"))
    }

    @Test
    fun spokenTextIsAppended() {
        assertEquals("Hello there", TextRules.appendSpoken("", "hello there"))
        assertEquals("I said hi and left", TextRules.appendSpoken("I said hi ", "and left"))
        assertEquals("Same", TextRules.appendSpoken("Same", "  "))
    }

    @Test
    fun promptsUseNameWhenSet() {
        assertEquals(
            "Max, which part of this story might be true, and which part is overstated?",
            Prompts.truer("Max"),
        )
        assertEquals("What would you tell a friend who did this?", Prompts.friend(""))
        assertEquals("How strong is the humiliated right now?", Prompts.intensity("humiliated", "Ashamed"))
        assertEquals("How strong is the ashamed right now?", Prompts.intensity(null, "Ashamed"))
        assertEquals("How strong is it right now?", Prompts.intensity(null, null))
    }

    @Test
    fun helperPickerAvoidsLastSuggestion() {
        val helpers = listOf("A", "B")
        repeat(20) { assertEquals("B", HelperPicker.pick(helpers, last = "A", random = Random(it))) }
        assertEquals("A", HelperPicker.pick(listOf("A"), last = "A"))
        assertNull(HelperPicker.pick(listOf(" "), last = null))
    }

    @Test
    fun breathPlanFitsFiveMinutes() {
        assertEquals(9_000L, BreathPlan.cycleMillis)
        assertTrue(BreathPlan.canRunAnotherCycle(0))
        assertTrue(BreathPlan.canRunAnotherCycle(BreathPlan.MAX_TOTAL_MS - 9_000))
        assertFalse(BreathPlan.canRunAnotherCycle(BreathPlan.MAX_TOTAL_MS - 8_999))
    }

    @Test
    fun elapsedFormatsMinutesAndSeconds() {
        assertEquals("0:00", DateText.elapsed(0))
        assertEquals("1:05", DateText.elapsed(65_400))
        assertEquals("5:00", DateText.elapsed(300_000))
    }

    @Test
    fun defaultFeelingListsMatchSpec() {
        assertEquals(listOf("Anxious", "Ashamed", "Sad", "Angry", "Lonely", "Guilty"), Defaults.feelings.keys.toList())
        assertEquals(listOf("regretful", "remorseful"), Defaults.feelings["Guilty"])
        assertEquals(6, Defaults.storyChips.size)
    }
}
