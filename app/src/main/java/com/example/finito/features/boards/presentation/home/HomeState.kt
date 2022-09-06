package com.example.finito.features.boards.presentation.home

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.labels.domain.entity.SimpleLabel

data class HomeState(
    val labels: List<SimpleLabel> = emptyList(),
    val labelFilters: List<Int> = emptyList(),
    val boards: List<BoardWithLabelsAndTasks> = emptyList(),
    val boardsOrder: SortingOption.Common = SortingOption.Common.NameAZ,
    val gridLayout: Boolean = true
)
