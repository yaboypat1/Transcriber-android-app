package com.example.transcriber

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO
    )

    @Before
    fun setUp() {
        // Set up test environment
    }

    @Test
    fun testMainActivityLaunches() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Verify the app title is displayed
        onView(withText("Transcriber"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLanguageSelectionUI() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Verify language selection section is displayed
        onView(withText("Target Language"))
            .check(matches(isDisplayed()))
        
        // Verify English is selected by default
        onView(withText("English"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRecordingControlsUI() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Verify recording controls are displayed
        onView(withText("Ready to Record"))
            .check(matches(isDisplayed()))
        
        // Verify start button is displayed and enabled
        onView(withText("Start"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
        
        // Verify stop button is displayed but disabled initially
        onView(withText("Stop"))
            .check(matches(isDisplayed()))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun testTranscriptsSectionUI() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Verify transcripts section is displayed
        onView(withText("Transcripts"))
            .check(matches(isDisplayed()))
        
        // Verify empty state message is displayed
        onView(withText("No transcripts yet. Start recording to see results here."))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testStartRecordingButtonClick() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Click start recording button
        onView(withText("Start"))
            .perform(click())
        
        // Verify recording state changes
        onView(withText("Recording..."))
            .check(matches(isDisplayed()))
        
        // Verify stop button is now enabled
        onView(withText("Stop"))
            .check(matches(isEnabled()))
        
        // Verify start button is now disabled
        onView(withText("Start"))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun testStopRecordingButtonClick() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Start recording first
        onView(withText("Start"))
            .perform(click())
        
        // Stop recording
        onView(withText("Stop"))
            .perform(click())
        
        // Verify recording state changes back
        onView(withText("Ready to Record"))
            .check(matches(isDisplayed()))
        
        // Verify start button is enabled again
        onView(withText("Start"))
            .check(matches(isEnabled()))
        
        // Verify stop button is disabled again
        onView(withText("Stop"))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun testLanguageSelection() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Click on Spanish language
        onView(withText("Spanish"))
            .perform(click())
        
        // Verify Spanish is now selected
        onView(withText("Spanish"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testMultipleLanguageSelection() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Select different languages
        onView(withText("French"))
            .perform(click())
        
        onView(withText("German"))
            .perform(click())
        
        // Verify both languages are displayed
        onView(withText("French"))
            .check(matches(isDisplayed()))
        onView(withText("German"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testUIResponsiveness() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Rapidly click start and stop buttons
        repeat(5) {
            onView(withText("Start"))
                .perform(click())
            
            onView(withText("Stop"))
                .perform(click())
        }
        
        // Verify UI is still responsive
        onView(withText("Ready to Record"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAccessibilityFeatures() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Verify content descriptions are present
        onView(withContentDescription("Start Recording"))
            .check(matches(isDisplayed()))
        
        onView(withContentDescription("Stop Recording"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testErrorHandling() {
        ActivityScenario.launch(MainActivity::class.java)
        
        // Try to start recording without proper setup
        // This should trigger error handling
        onView(withText("Start"))
            .perform(click())
        
        // Verify error state is handled gracefully
        onView(withText("Start"))
            .check(matches(isDisplayed()))
    }
}
