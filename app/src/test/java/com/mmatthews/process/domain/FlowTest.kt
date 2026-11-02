package com.mmatthews.process.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowTest {
    private val start = ProcessDraft(startedAt = 1_000L)

    @Test
    fun fullFlowReachesDoneThroughFourScreensAndEnding() {
        val seen = mutableListOf<Step>()
        var d = start
        seen += d.step
        d = Flow.selectBroad(d, "Anxious")
        d = Flow.togglePrecise(d, "on edge")
        d = Flow.setIntensity(d, 8)
        d = Flow.nameItNext(d)
        seen += d.step
        d = Flow.bodyDone(d, breathSeconds = 45)
        seen += d.step
        d = Flow.setWhatHappened(d, "  I said hi and they walked off.  ")
        d = Flow.whatNext(d)
        d = Flow.selectChip(d, "They don't like me")
        d = d.copy(newFact = "Sam invited me to lunch")
        d = Flow.reframeNext(d)
        seen += d.step
        d = Flow.chooseAction(d, ActionType.IF_THEN)
        d = d.copy(ifCue = "someone walks off", ifAction = "say hi to the next person")
        d = Flow.saveAction(d)
        seen += d.step
        d = Flow.rerateNext(Flow.setAfter(d, 5), helper = "Call family")
        seen += d.step
        d = Flow.helperAnswer(d, done = true)
        seen += d.step

        assertEquals(
            listOf(Step.NAME_IT, Step.BODY, Step.STORY, Step.ACT, Step.RERATE, Step.HELPER, Step.DONE),
            seen,
        )
        val o = d.outcome()
        assertEquals("Anxious", o.feelingBroad)
        assertEquals("on edge", o.feelingPrecise)
        assertEquals(8, o.intensityBefore)
        assertEquals(5, o.intensityAfter)
        assertEquals("I said hi and they walked off.", o.whatHappened)
        assertEquals(ReframeType.TRUER, o.reframeType)
        assertEquals("Sam invited me to lunch", o.newFact)
        assertEquals(ActionType.IF_THEN, o.actionType)
        assertEquals("someone walks off", o.ifCue)
        assertEquals("Call family", o.helperSuggested)
        assertEquals(true, o.helperDone)
        assertEquals(45, o.breathSeconds)
        assertEquals("Sam invited me to lunch", o.libraryText)
    }

    @Test
    fun everyStepCanBeSkipped() {
        var d = start
        val steps = mutableListOf(d.step)
        repeat(7) {
            d = Flow.skip(d, helper = "Stretch 2 min")
            steps += d.step
        }
        assertEquals(Step.HELPER, d.step)
        assertNull(d.intensityBefore)
        assertEquals(ReframeType.NONE, d.reframeUsed)
        assertEquals(ActionType.NONE, d.actionType)
        assertNull(d.intensityAfter)
        d = Flow.helperAnswer(d, done = false)
        assertEquals(Step.DONE, d.step)
        assertEquals(false, d.outcome().helperDone)
    }

    @Test
    fun preciseWordsToggleWithOptionalSecond() {
        var d = Flow.selectBroad(start, "Sad")
        d = Flow.togglePrecise(d, "hurt")
        d = Flow.togglePrecise(d, "empty")
        assertEquals("hurt", d.feelingPrecise)
        assertEquals("empty", d.feelingPrecise2)
        d = Flow.togglePrecise(d, "hurt")
        assertEquals("empty", d.feelingPrecise)
        assertNull(d.feelingPrecise2)
        d = Flow.togglePrecise(d, "disappointed")
        d = Flow.togglePrecise(d, "hopeless")
        assertEquals("hopeless", d.feelingPrecise2)
        // Changing the broad feeling clears precise words.
        d = Flow.selectBroad(d, "Angry")
        assertNull(d.feelingPrecise)
        assertNull(d.feelingPrecise2)
    }

    @Test
    fun truerVersionNeedsANewFactOfThreeWords() {
        val d = Flow.selectChip(Flow.whatNext(start.copy(step = Step.STORY)), "I'm a loser")
        assertFalse(Flow.canSaveReframe(d.copy(newFact = "she smiled")))
        assertEquals(d.copy(newFact = "she smiled"), Flow.reframeNext(d.copy(newFact = "she smiled")))
        assertTrue(Flow.canSaveReframe(d.copy(newFact = "she smiled back")))
        val friend = d.copy(reframeTab = ReframeType.FRIEND)
        assertFalse(Flow.canSaveReframe(friend))
        assertTrue(Flow.canSaveReframe(friend.copy(friendText = "Everyone has off days")))
    }

    @Test
    fun friendReframeIsSavedAsReframeText() {
        val d = start.copy(
            storyChip = "I hurt someone",
            reframeTab = ReframeType.FRIEND,
            friendText = " You noticed and you care. ",
            newFact = "left over from truer tab",
        )
        val o = Flow.reframeNext(d).outcome()
        assertEquals(ReframeType.FRIEND, o.reframeType)
        assertEquals("You noticed and you care.", o.reframeText)
        assertEquals("", o.newFact)
        assertEquals("You noticed and you care.", o.libraryText)
    }

    @Test
    fun predictionIsPrefilledWithChipText() {
        val d = Flow.chooseAction(start.copy(step = Step.ACT, storyChip = "They think I'm weird"), ActionType.PREDICTION)
        assertEquals(ActStage.PREDICTION, d.actStage)
        assertEquals("They think I'm weird", d.predictionText)
        val saved = Flow.saveAction(d.copy(predictionPercent = 70))
        assertEquals(ActionType.PREDICTION, saved.actionType)
        assertEquals(70, saved.outcome().predictionPercent)
    }

    @Test
    fun repairNoIsSavedAsLetGo() {
        var d = Flow.chooseAction(start.copy(step = Step.ACT), ActionType.REPAIR)
        assertEquals(ActStage.REPAIR_ASK, d.actStage)
        d = Flow.repairAnswer(d, concreteHarm = false)
        assertEquals(ActStage.REPAIR_NO, d.actStage)
        assertEquals(ActionType.LET_GO, Flow.saveAction(d).actionType)

        var yes = Flow.repairAnswer(Flow.chooseAction(start.copy(step = Step.ACT), ActionType.REPAIR), true)
        assertFalse(Flow.canSaveAction(yes))
        yes = yes.copy(repairNote = "Text Jo tonight to apologise")
        val saved = Flow.saveAction(yes)
        assertEquals(ActionType.REPAIR, saved.actionType)
        assertEquals("Text Jo tonight to apologise", saved.outcome().actionNote)
    }

    @Test
    fun therapyCommitsImmediatelyAndFlags() {
        val d = Flow.chooseAction(start.copy(step = Step.ACT), ActionType.THERAPY)
        assertEquals(Step.RERATE, d.step)
        assertTrue(d.outcome().flaggedTherapy)
    }

    @Test
    fun onlyTheChosenActionsFieldsAreSaved() {
        // User filled an if-then, went back, and picked Let it go instead.
        val d = start.copy(step = Step.ACT, actStage = ActStage.LET_GO, ifCue = "x", ifAction = "y")
        val o = Flow.saveAction(d).outcome()
        assertEquals(ActionType.LET_GO, o.actionType)
        assertEquals("", o.ifCue)
        assertEquals("", o.ifAction)
        assertNull(o.predictionPercent)
    }

    @Test
    fun backWalksThroughStagesAndLeavesFromScreenOne() {
        assertNull(Flow.back(start))
        val story = start.copy(step = Step.STORY, storyStage = StoryStage.REFRAME)
        assertEquals(StoryStage.CHIP, Flow.back(story)!!.storyStage)
        assertEquals(Step.BODY, Flow.back(story.copy(storyStage = StoryStage.WHAT))!!.step)
        val act = start.copy(step = Step.ACT, actStage = ActStage.REPAIR_YES)
        assertEquals(ActStage.REPAIR_ASK, Flow.back(act)!!.actStage)
        assertEquals(ActStage.CHOOSE, Flow.back(act.copy(actStage = ActStage.IF_THEN))!!.actStage)
        assertEquals(StoryStage.REFRAME, Flow.back(act.copy(actStage = ActStage.CHOOSE))!!.storyStage)
        assertEquals(Step.ACT, Flow.back(start.copy(step = Step.RERATE))!!.step)
        assertEquals(Step.DONE, Flow.back(start.copy(step = Step.DONE))!!.step)
    }

    @Test
    fun rerateDefaultsToBeforeValueAndHelperIsKeptOnceChosen() {
        val d = start.copy(step = Step.RERATE, intensityBefore = 7)
        assertEquals(7, Flow.afterSliderValue(d))
        val h = Flow.rerateNext(d, helper = "Call family")
        assertEquals(7, h.intensityAfter)
        // Going back and forward again keeps the same suggestion.
        val again = Flow.rerateNext(Flow.back(h)!!, helper = "Stretch 2 min")
        assertEquals("Call family", again.helper)
    }

    @Test
    fun noHelpersGoesStraightToDone() {
        assertEquals(Step.DONE, Flow.rerateNext(start.copy(step = Step.RERATE), helper = null).step)
    }
}
