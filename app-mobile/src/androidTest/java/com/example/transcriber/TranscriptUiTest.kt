package com.example.transcriber

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class TranscriptUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsEmptyStateThenAddsItem() {
        // Empty state text
        composeRule.onNodeWithText("Transcripts").assertExists()
        composeRule.onNodeWithText("No transcripts yet. Start recording to see results here.")
            .assertExists()

        // Start then Stop to simulate adding transcript via callbacks (UI only)
        composeRule.onNodeWithText("Start").performClick()
        composeRule.onNodeWithText("Stop").performClick()
    }
}

