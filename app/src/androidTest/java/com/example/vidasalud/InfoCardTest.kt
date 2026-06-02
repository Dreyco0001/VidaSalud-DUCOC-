package com.example.vidasalud

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.vidasalud.ui.screens.home.BurnedCaloriesCard
import com.example.vidasalud.ui.screens.home.HeightCard
import com.example.vidasalud.ui.screens.home.InfoCard
import com.example.vidasalud.ui.screens.home.StepsCard
import com.example.vidasalud.ui.screens.home.WeightCard
import org.junit.Rule
import org.junit.Test

class InfoCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun infoCard_displaysTitleAndValue() {
        composeTestRule.setContent {
            InfoCard(
                icon = Icons.Default.Person,
                title = "Test Title",
                value = "Test Value",
                onClick = {},
                showEditIcon = false
            )
        }

        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Value").assertIsDisplayed()
    }

    @Test
    fun weightCard_displaysWeightCorrectly() {
        composeTestRule.setContent {
            WeightCard(onClick = {}, weight = 72.5f)
        }

        composeTestRule.onNodeWithText("72.5 kg").assertIsDisplayed()
    }

    @Test
    fun weightCard_displaysNAWhenWeightNull() {
        composeTestRule.setContent {
            WeightCard(onClick = {}, weight = null)
        }

        composeTestRule.onNodeWithText("N/A").assertIsDisplayed()
    }

    @Test
    fun heightCard_displaysHeightCorrectly() {
        composeTestRule.setContent {
            HeightCard(onClick = {}, height = 175f)
        }

        composeTestRule.onNodeWithText("175 cm").assertIsDisplayed()
    }

    @Test
    fun heightCard_displaysNAWhenHeightNull() {
        composeTestRule.setContent {
            HeightCard(onClick = {}, height = null)
        }

        composeTestRule.onNodeWithText("N/A").assertIsDisplayed()
    }

    @Test
    fun stepsCard_displaysDefaultSteps() {
        composeTestRule.setContent {
            StepsCard()
        }

        composeTestRule.onNodeWithText("8,450").assertIsDisplayed()
    }

    @Test
    fun burnedCaloriesCard_displaysDefaultCalories() {
        composeTestRule.setContent {
            BurnedCaloriesCard()
        }

        composeTestRule.onNodeWithText("505").assertIsDisplayed()
    }
}