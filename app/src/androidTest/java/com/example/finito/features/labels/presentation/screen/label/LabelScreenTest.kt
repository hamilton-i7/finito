package com.example.finito.features.labels.presentation.screen.label

import android.content.SharedPreferences
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import com.example.finito.AppModule
import com.example.finito.MainActivity
import com.example.finito.R
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.core.di.PreferencesModuleTest
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import com.example.finito.features.subtasks.di.SubtaskModule
import com.example.finito.features.tasks.di.TaskModule
import com.example.finito.ui.theme.FinitoTheme
import com.google.common.truth.Truth.assertThat
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
import com.example.finito.core.domain.Result

@HiltAndroidTest
@UninstallModules(
    AppModule::class,
    BoardModule::class,
    TaskModule::class,
    LabelModule::class,
    SubtaskModule::class,
    PreferencesModuleTest::class
)
class LabelScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Inject
    lateinit var db: FinitoDatabase

    @Inject
    lateinit var boardUseCases: BoardUseCases

    @Inject
    lateinit var labelUseCases: LabelUseCases

    @Inject
    lateinit var preferences: SharedPreferences

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

        Board.activeBoards.forEach {
            boardUseCases.createBoard(
                BoardWithLabelsAndTasks(board = it, labels = labels)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Before
    fun setUp() {
        composeRule.activity.setContent {
            with(preferences.edit()) {
                clear()
                apply()
            }

            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.apply {
                addNavigator(ComposeNavigator())
            }
            FinitoTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Label.route
                ) {
                    composable(
                        route = Screen.Label.route,
                        arguments = listOf(
                            navArgument(Screen.LABEL_ROUTE_ARGUMENT) {
                                type = NavType.IntType
                                defaultValue = this@LabelScreenTest.label.labelId
                            }
                        )
                    ) { LabelScreen() }
                }
            }
        }
    }

    @Test
    fun should_display_label_screen() {
        findLabelScreen().assertIsDisplayed()
    }

    @Test
    fun should_display_rename_dialog_when_menu_option_clicked() {
        openScreenMenu()
        findMenuOption(index = 0).performClick()
        findRenameDialog().assertIsDisplayed()
    }

    @Test
    fun should_disable_rename_button_when_input_blank() {
        openScreenMenu()
        findMenuOption(index = 0).performClick()
        findDialogConfirmButton().assertIsNotEnabled()

        findRenameTextField().performTextInput(text = "Label name")
        findDialogConfirmButton().assertIsEnabled()
    }

    @Test
    fun should_display_delete_dialog_when_menu_option_clicked() {
        openScreenMenu()
        findMenuOption(index = 1).performClick()
        findDeleteDialog().assertIsDisplayed()
    }

    @Test
    fun should_display_board_card_menu_when_options_button_clicked() {
        openBoardCardMenu()
        findBoardCardMenu().assertIsDisplayed()
    }

    @Test
    fun should_show_search_bar_when_search_icon_clicked() {
        openSearchBar()
        findSearchBar().assertIsDisplayed()
    }

    @Test
    fun should_have_only_one_active_sorting_chip_when_clicked() {
        findSortingChip(index = 0).assertIsSelected()
        findSortingChip(index = 1).assertIsNotSelected()

        findSortingChip(index = 1).performClick()

        findSortingChip(index = 0).assertIsNotSelected()
        findSortingChip(index = 1).assertIsSelected()
    }

    @Test
    fun should_toggle_boards_layout_when_toggle_icon_clicked() {
        val toggleButton = composeRule.onNodeWithTag(TestTags.TOGGLE_LAYOUT_BUTTON)

        findBoardsList().assertDoesNotExist()
        toggleButton.performClick()
        findBoardsList().assertIsDisplayed()

        findBoardsGrid().assertDoesNotExist()
        toggleButton.performClick()
        findBoardsGrid().assertIsDisplayed()
    }

    @Test
    fun should_remove_board_when_archived() {
        val board = findBoardCard()

        openBoardCardMenu()
        // Click on 'Archive' menu option
        findMenuOption(index = 0).performClick()

        assertThat(board).isNotEqualTo(findBoardCard())
    }

    @Test
    fun should_remove_board_when_deleted() {
        val board = findBoardCard()

        openBoardCardMenu()
        // Click on 'Archive' menu option
        findMenuOption(index = 1).performClick()

        assertThat(board).isNotEqualTo(findBoardCard())
    }

    @Test
    fun should_change_boards_order_when_sorting_chip_clicked() {
        // Click on the 'Z-A' sorting chip
        findSortingChip(index = 3).performClick()
        findBoardCard().assertTextContains("Board Z")

        // Click on the 'A-Z' sorting chip
        findSortingChip(index = 2).performClick()
        findBoardCard().assertTextContains("Board A")
    }

    @Test
    fun should_update_top_bar_title_when_label_renamed() {
        findLabelScreen().assertTextContains(label.name)

        // Rename label
        openScreenMenu()
        findMenuOption(index = 0).performClick()
        findRenameTextField().performTextInput(text = "New label name")
        findDialogConfirmButton().performClick()

        findLabelScreen().assertTextContains(value = "New label name")
    }

    @Test
    fun should_display_found_boards_when_searched() {
        openSearchBar()
        findSearchTextField().performTextInput(text = "Board A")
        findBoardCards().assertCountEquals(expectedSize = 1)
    }

    private fun findLabelScreen(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.LABEL_SCREEN)
    }

    private fun findBoardCard(index: Int = 0): SemanticsNodeInteraction {
        return composeRule.onAllNodesWithTag(TestTags.BOARD_CARD)[index]
    }

    private fun findBoardCards(): SemanticsNodeInteractionCollection {
        return composeRule.onAllNodesWithTag(TestTags.BOARD_CARD)
    }

    private fun openScreenMenu() {
        composeRule.onNodeWithTag(TestTags.MENU_BUTTON).performClick()
    }

    private fun findMenuOption(index: Int): SemanticsNodeInteraction {
        return composeRule.onAllNodesWithTag(TestTags.MENU_OPTION)[index]
    }

    private fun findRenameDialog(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.RENAME_LABEL_DIALOG)
    }

    private fun findDeleteDialog(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.DELETE_DIALOG)
    }

    private fun findRenameTextField(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.RENAME_TEXT_FIELD)
    }

    private fun findDialogConfirmButton(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.DIALOG_CONFIRM_BUTTON)
    }

    private fun findBoardCardMenu(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.CARD_MENU)
    }

    private fun openBoardCardMenu(): SemanticsNodeInteraction {
        return composeRule.onAllNodesWithTag(TestTags.CARD_MENU_BUTTON)[0].performClick()
    }

    private fun findSortingChip(index: Int): SemanticsNodeInteraction {
        return composeRule.onAllNodesWithTag(TestTags.SORTING_CHIP)[index]
    }

    private fun findBoardsGrid(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.BOARDS_GRID, useUnmergedTree = true)
    }

    private fun findBoardsList(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.BOARDS_LIST, useUnmergedTree = true)
    }

    private fun findSearchBar(): SemanticsNodeInteraction {
        return composeRule.onNodeWithText(text = composeRule.activity.getString(R.string.search_boards))
    }

    private fun findSearchTextField(): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD)
    }

    private fun openSearchBar() {
        composeRule.onNodeWithContentDescription(label = composeRule.activity.getString(R.string.search_boards)).performClick()
    }
}