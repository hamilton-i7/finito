package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class ActiveBoardScreenOption(label: Int) : BoardScreenMenuOption(label) {
    object EditBoard : ActiveBoardScreenOption(R.string.edit_board)

    object ArchiveBoard : ActiveBoardScreenOption(R.string.archive_board)

    object DeleteBoard : ActiveBoardScreenOption(R.string.move_to_trash)

    object DeleteCompletedTasks : ActiveBoardScreenOption(R.string.delete_completed_tasks)
}

sealed class ArchivedBoardScreenMenuOption(label: Int) : BoardScreenMenuOption(label) {
    object EditBoard : ArchivedBoardScreenMenuOption(R.string.edit_board)

    object UnarchiveBoard : ArchivedBoardScreenMenuOption(R.string.unarchive_board)

    object DeleteBoard : ArchivedBoardScreenMenuOption(R.string.move_to_trash)

    object DeleteCompletedTasks : ArchivedBoardScreenMenuOption(R.string.delete_completed_tasks)
}

sealed class DeletedBoardScreenMenuOption(label: Int) : BoardScreenMenuOption(label) {
    object EditBoard : DeletedBoardScreenMenuOption(R.string.edit_board)

    object RestoreBoard : DeletedBoardScreenMenuOption(R.string.restore_board)

    object DeleteCompletedTasks : DeletedBoardScreenMenuOption(R.string.delete_completed_tasks)
}

sealed class DeletedBoardCardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Restore : DeletedBoardCardMenuOption(R.string.restore)

    object DeleteForever : DeletedBoardCardMenuOption(R.string.delete_forever)
}

sealed class ArchivedBoardCardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Unarchive : ArchivedBoardCardMenuOption(R.string.unarchive)

    object Delete : ArchivedBoardCardMenuOption(R.string.delete)
}

sealed class ActiveBoardCardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Archive : ActiveBoardCardMenuOption(R.string.archive)

    object Delete : ActiveBoardCardMenuOption(R.string.delete)
}
