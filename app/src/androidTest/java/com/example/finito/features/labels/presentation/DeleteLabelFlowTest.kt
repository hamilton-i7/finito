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
import com.example.finito.core.di.PreferencesModuleTest
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.App
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import com.example.finito.features.subtasks.di.SubtaskModule
import com.example.finito.features.tasks.di.TaskModule
import com.example.finito.ui.theme.FinitoTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    AppModule::class,
    BoardModule::class,
    TaskModule::class,
    LabelModule::class,
    SubtaskModule::class,
    PreferencesModuleTest::class
)
class DeleteLabelFlowTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var labelUseCases: LabelUseCases

    private lateinit var navController: TestNavHostController
    private lateinit var label: Label

    @Before
    fun init() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun prepopulate() = runTest {
        Label.dummyLabels.take(5).forEach {
            labelUseCases.createLabel(it)
        }
        val labels = labelUseCases.findSimpleLabels().first()
        label = (labelUseCases.findLabel(labels.random().labelId) as Result.Success).data
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
    fun should_delete_requested_label() {
        openDrawer()
        findLabelToDeleteInstances()[1].performClick()
        findLabelScreen().assertIsDisplayed()

        openScreenMenu()
        findDeleteMenuOption().performClick()
        findConfirmDialogButton().performClick()

        findHomeScreen().assertIsDisplayed()
        findLabelToDeleteInstances().assertCountEquals(expectedSize = 0)
    }

    private fun openDrawer() {
        composeRule.onNodeWithTag(TestTags.DRAWER_BUTTON).performClick()
    }

    private fun findLabelToDeleteInstances(): SemanticsNodeInteractionCollection {
        return composeRule.onAllNodesWithText(text = label.name)
    }

    private fun openScreenMenu() {
        composeRule.onNodeWithTag(TestTags.MENU_BUTTON).performClick()
    }

    private fun findDeleteMenuOption(): SemanticsNodeInteraction {
        return composeRule.onAllNodesWithTag(TestTags.MENU_OPTION)[1]
    }

    private fun findConfirmDialogButton(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.DIALOG_CONFIRM_BUTTON)
    }

    private fun findLabelScreen(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.LABEL_SCREEN)
    }

    private fun findHomeScreen(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.HOME_SCREEN)
    }
}