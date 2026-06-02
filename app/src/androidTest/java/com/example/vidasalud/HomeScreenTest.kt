package com.example.vidasalud

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.vidasalud.ui.screens.home.HealthCarousel
import com.example.vidasalud.ui.screens.home.TipSalud
import org.junit.Rule
import org.junit.Test

class HealthCarouselTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun healthCarousel_displaysFirstTip() {
        val tips = listOf(
            TipSalud("Primer tip de salud", Color(0xFFFFBACD)),
            TipSalud("Segundo tip de salud", Color(0xFFFFF9C4)),
            TipSalud("Tercer tip de salud", Color(0xFFC7EAE7))
        )

        composeTestRule.setContent {
            HealthCarousel(tips = tips)
        }

        composeTestRule.onNodeWithText("Primer tip de salud").assertIsDisplayed()
    }

    @Test
    fun healthCarousel_handlesMultipleTips() {
        val tips = listOf(
            TipSalud("Primer tip", Color(0xFFFFBACD)),
            TipSalud("Segundo tip", Color(0xFFFFF9C4)),
            TipSalud("Tercer tip", Color(0xFFC7EAE7))
        )

        composeTestRule.setContent {
            HealthCarousel(tips = tips)
        }

        composeTestRule.waitForIdle()

        // Al menos el primer tip debería estar visible
        composeTestRule.onNodeWithText("Primer tip").assertIsDisplayed()
    }
}