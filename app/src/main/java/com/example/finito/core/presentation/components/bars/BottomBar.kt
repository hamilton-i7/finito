package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.AnimationDurationConstants

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
    showBottomBar: Boolean,
    @StringRes fabDescription: Int,
    @StringRes searchDescription: Int,
    modifier: Modifier = Modifier,
    gridLayout: Boolean = true,
    onSearchClick: () -> Unit = {},
    onChangeLayoutClick: () -> Unit = {},
    onFabClick: (() -> Unit)? = null
) {
    AnimatedContent(
        targetState = showBottomBar,
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(
                    durationMillis = AnimationDurationConstants.LongDurationMillis,
                    delayMillis = AnimationDurationConstants.ShortestDurationMillis
                ),
                initialOffsetY = { it / 2 }
            ) with slideOutVertically(
                animationSpec = tween(
                    durationMillis = AnimationDurationConstants.LongDurationMillis,
                    delayMillis = AnimationDurationConstants.ShortestDurationMillis
                ),
                targetOffsetY = { it / 2 }
            )
        }
    ) { show ->
        if (!show) return@AnimatedContent
        BottomAppBar(
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(id = searchDescription)
                    )
                }
                IconButton(onClick = onChangeLayoutClick) {
                    if (gridLayout) {
                        Icon(
                            imageVector = Icons.Outlined.ViewStream,
                            contentDescription = stringResource(id = R.string.list_view)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.GridView,
                            contentDescription = stringResource(id = R.string.grid_view)
                        )
                    }
                }
            },
            floatingActionButton = fab@{
                if (onFabClick == null) return@fab
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = fabDescription)
                    )
                }
            },
            modifier = modifier
        )
    }
}