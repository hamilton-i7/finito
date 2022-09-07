package com.example.finito.core.domain.util

import androidx.annotation.StringRes
import com.example.finito.R

sealed class MenuOption(@StringRes val label: Int)

sealed class TopBarMenuOption(label: Int) : MenuOption(label)
sealed class BoardScreenMenuOption(label: Int) : TopBarMenuOption(label) {
    object EditBoard : BoardScreenMenuOption(R.string.edit_board)

    object ArchiveBoard : BoardScreenMenuOption(R.string.archive_board)

    object DeleteBoard : BoardScreenMenuOption(R.string.delete_board)

    object DeleteCompletedTasks : BoardScreenMenuOption(R.string.delete_completed_tasks)
}

sealed class LabelScreenMenuOption(label: Int) : TopBarMenuOption(label) {
    object RenameLabel : LabelScreenMenuOption(R.string.rename_label)

    object DeleteLabel : LabelScreenMenuOption(R.string.delete_label)
}

sealed class TrashScreenMenuOption(label: Int) : TopBarMenuOption(label) {
    object EmptyTrash : TrashScreenMenuOption(label = R.string.empty_trash)
}

sealed class BoardCardMenuOption(label: Int) : MenuOption(label)
sealed class DeletedBoardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Restore : DeletedBoardMenuOption(R.string.restore)

    object DeleteForever : DeletedBoardMenuOption(R.string.delete_forever)
}

sealed class ArchivedBoardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Unarchive : ArchivedBoardMenuOption(R.string.unarchive)

    object Delete : ArchivedBoardMenuOption(R.string.delete)
}

sealed class ActiveBoardMenuOption(label: Int) : BoardCardMenuOption(label) {
    object Archive : ActiveBoardMenuOption(R.string.archive)

    object Delete : ActiveBoardMenuOption(R.string.delete)
}
