package com.example.finito.features.labels.presentation

import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.finito.AppModule
import com.example.finito.MainActivity
import com.example.finito.R
import com.example.finito.core.di.PreferencesModuleTest
import com.example.finito.core.presentation.App
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.subtasks.di.SubtaskModule
import com.example.finito.features.tasks.di.TaskModule
import com.example.finito.ui.theme.FinitoTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(
    AppModule::class,
    BoardModule::class,
    TaskModule::class,
    LabelModule::class,
    SubtaskModule::class,
    PreferencesModuleTest::class
)
class CreateLabelFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun init() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Before
    fun setUp() {
        composeRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.apply {
                addNavigator(ComposeNavigator())
                addNavigator(DialogNavigator())
            }
            FinitoTheme { App() }
        }
    }

    @Test
    fun should_display_create_label_dialog_when_navigated_to() {
        openDrawer()
        findCreateNewLabelItem()
            .performScrollTo()
            .performClick()

        findCreateLabelDialog().assertIsDisplayed()
    }

    @Test
    fun should_create_new_label() {
        findCreatedLabel().assertCountEquals(expectedSize = 0)

        openDrawer()
        findCreateNewLabelItem()
            .performScrollTo()
            .performClick()

        findNameTextField().performTextInput(text = "Recently created label")
        findConfirmDialogButton().performClick()

        findCreatedLabel().assertCountEquals(expectedSize = 2)
    }

    private fun findCreateLabelDialog(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.CREATE_LABEL_DIALOG)
    }

    private fun openDrawer() {
        composeRule.onNodeWithTag(TestTags.DRAWER_BUTTON).performClick()
    }

    private fun findCreateNewLabelItem(): SemanticsNodeInteraction {
        val description = composeRule.activity.getString(R.string.create_new_label)
        return composeRule.onNodeWithText(description)
    }

    private fun findNameTextField(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.NAME_TEXT_FIELD)
    }

    private fun findConfirmDialogButton(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.DIALOG_CONFIRM_BUTTON)
    }

    private fun findCreatedLabel(): SemanticsNodeInteractionCollection {
        return composeRule.onAllNodesWithText(text = "Recently created label")
    }
}