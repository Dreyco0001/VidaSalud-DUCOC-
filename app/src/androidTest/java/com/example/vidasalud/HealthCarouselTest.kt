package com.example.vidasalud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.vidasalud.ui.screens.home.HomeScreen
import com.example.vidasalud.viewmodel.HomeViewModel
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                userName = "Test User",
                userRole = "user",
                homeViewModel = HomeViewModel()
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("¡Bienvenido/a, Test User!")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysResumenText() {
        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                userName = "Test User",
                userRole = "user",
                homeViewModel = HomeViewModel()
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Resumen").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysAllCards() {
        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                userName = "Test User",
                userRole = "user",
                homeViewModel = HomeViewModel()
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sueño (últimos 7 días)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pasos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Peso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estatura").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calorías quemadas").assertIsDisplayed()
    }

    @Test
    fun weightDialog_showsWhenWeightCardClicked() {
        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                userName = "Test User",
                userRole = "user",
                homeViewModel = HomeViewModel()
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Peso").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ingresar Peso").assertIsDisplayed()
    }

    @Test
    fun heightDialog_showsWhenHeightCardClicked() {
        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                userName = "Test User",
                userRole = "user",
                homeViewModel = HomeViewModel()
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Estatura").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ingresar Estatura").assertIsDisplayed()
    }
}