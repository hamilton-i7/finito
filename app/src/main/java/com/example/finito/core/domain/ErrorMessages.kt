package com.example.finito.core.domain

object ErrorMessages {
    const val INVALID_ID = "ID must be a positive integer"
    const val NOT_FOUND = "Resource not found"
    const val EMPTY_NAME = "Name cannot be blank"

    const val INVALID_TASK_STATE = "Date must not be null if time is set"
    const val DIFFERENT_SUBTASKS_ORIGIN = "All subtasks must come from the same task"
    const val DIFFERENT_TASKS_ORIGIN = "All tasks must come from the same board"
}