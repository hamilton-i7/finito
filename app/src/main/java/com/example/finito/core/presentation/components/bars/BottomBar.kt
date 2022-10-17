package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.TestTags

@Composable
fun BottomBar(
    @StringRes fabDescription: Int,
    @StringRes searchDescription: Int,
    modifier: Modifier = Modifier,
    gridLayout: Boolean = true,
    onSearchClick: () -> Unit = {},
    onChangeLayoutClick: () -> Unit = {},
    onFabClick: (() -> Unit)? = null
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(id = searchDescription)
                )
            }
            IconButton(
                onClick = onChangeLayoutClick,
                modifier = Modifier.testTag(TestTags.TOGGLE_LAYOUT_BUTTON)
            ) {
                if (gridLayout) {
                    Icon(
                        painter = painterResource(id = R.drawable.rows),
                        contentDescription = stringResource(id = R.string.list_view)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.grip_vertical),
                        contentDescription = stringResource(id = R.string.grid_view)
                    )
                }
            }
        },
        floatingActionButton = if (onFabClick != null) {
            {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus_math),
                        contentDescription = stringResource(id = fabDescription)
                    )
                }
            }
        } else null,
        modifier = modifier
    )
}