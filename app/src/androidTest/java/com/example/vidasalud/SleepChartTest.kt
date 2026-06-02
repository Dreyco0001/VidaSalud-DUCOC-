package com.example.vidasalud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.vidasalud.ui.screens.home.SleepChart
import com.example.vidasalud.ui.screens.home.SleepChartCard
import org.junit.Rule
import org.junit.Test

class SleepChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sleepChart_displaysWhenDataExists() {
        val sleepData = listOf(7.5f, 8.0f, 6.5f, 7.0f, 8.5f, 7.0f, 7.5f)

        composeTestRule.setContent {
            SleepChart(sleepData = sleepData)
        }

        composeTestRule.onNodeWithText("L").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("X").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("V").assertIsDisplayed()
        composeTestRule.onNodeWithText("S").assertIsDisplayed()
        composeTestRule.onNodeWithText("D").assertIsDisplayed()
    }

    @Test
    fun sleepChart_showsMessageWhenNoData() {
        composeTestRule.setContent {
            SleepChart(sleepData = emptyList())
        }

        composeTestRule.onNodeWithText("No hay datos de sueño disponibles.")
            .assertIsDisplayed()
    }

    @Test
    fun sleepChartCard_displaysTitle() {
        composeTestRule.setContent {
            SleepChartCard(
                navController = rememberNavController(),
                sleepData = listOf(7.5f),
                userName = "Test",
                userRole = "user"
            )
        }

        composeTestRule.onNodeWithText("Sueño (últimos 7 días)").assertIsDisplayed()
    }
}