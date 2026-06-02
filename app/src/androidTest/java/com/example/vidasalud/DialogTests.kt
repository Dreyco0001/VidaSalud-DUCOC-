package com.example.vidasalud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.vidasalud.ui.screens.home.HeightInputDialog
import com.example.vidasalud.ui.screens.home.WeightInputDialog
import org.junit.Rule
import org.junit.Test

class DialogTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weightInputDialog_displaysCorrectComponents() {
        composeTestRule.setContent {
            WeightInputDialog(
                onDismiss = {},
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Ingresar Peso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Peso (kg)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun heightInputDialog_displaysCorrectComponents() {
        composeTestRule.setContent {
            HeightInputDialog(
                onDismiss = {},
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Ingresar Estatura").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estatura (cm)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsDisplayed()
    }

    @Test
    fun weightDialog_callsOnDismissWhenCancelClicked() {
        var wasDismissed = false

        composeTestRule.setContent {
            WeightInputDialog(
                onDismiss = { wasDismissed = true },
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Cancelar").performClick()
        assert(wasDismissed)
    }

    @Test
    fun heightDialog_callsOnDismissWhenCancelClicked() {
        var wasDismissed = false

        composeTestRule.setContent {
            HeightInputDialog(
                onDismiss = { wasDismissed = true },
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Cancelar").performClick()
        assert(wasDismissed)
    }
}