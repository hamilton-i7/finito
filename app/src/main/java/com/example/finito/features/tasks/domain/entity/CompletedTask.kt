package com.example.finito.features.tasks.domain.entity

data class CompletedTask(
    val completed: Boolean = false
) {
    companion object {
        val dummyTasks = (0..25).map {
            CompletedTask(completed = it % 3 == 0)
        }.shuffled()
    }
}
