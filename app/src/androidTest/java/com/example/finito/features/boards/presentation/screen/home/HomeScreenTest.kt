package com.example.finito.features.boards.presentation.screen.home

import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.finito.AppModule
import com.example.finito.MainActivity
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.core.di.PreferencesModuleTest
import com.example.finito.core.presentation.App
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import com.example.finito.features.subtasks.di.SubtaskModule
import com.example.finito.features.tasks.di.TaskModule
import com.example.finito.ui.theme.FinitoTheme
import com.google.accompanist.navigation.animation.AnimatedComposeNavigator
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
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
class HomeScreenTest {
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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.apply {
                addNavigator(AnimatedComposeNavigator())
                addNavigator(DialogNavigator())
            }
            FinitoTheme {
                App(navController = navController)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun prepopulate() = runTest {
        Label.dummyLabels.forEach {
            labelUseCases.createLabel(it)
        }
        val labels = labelUseCases.findSimpleLabels().first()
        BoardWithLabelsAndTasks.dummyBoards.take(10).forEach {
            boardUseCases.createBoard(it.copy(
                board = it.board.copy(boardId = 0),
                labels = labels
            ))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun should_show_remove_filters_chip_when_label_clicked() {
        val removeFiltersChip = composeRule.onNodeWithTag(TestTags.REMOVE_FILTERS_CHIP)
        val labelFilter = composeRule.onAllNodesWithTag(TestTags.LABEL_FILTER_ITEM)[0]

        removeFiltersChip.assertDoesNotExist()
        labelFilter.performClick()
        removeFiltersChip.assertExists()
    }
}