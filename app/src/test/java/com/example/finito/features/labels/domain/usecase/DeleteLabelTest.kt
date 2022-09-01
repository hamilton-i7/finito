package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DeleteLabelTest {
    private lateinit var deleteLabel: DeleteLabel
    private lateinit var fakeLabelRepository: FakeLabelRepository
    private lateinit var dummyLabels: MutableList<Label>

    @Before
    fun setUp() = runTest {
        fakeLabelRepository = FakeLabelRepository()
        deleteLabel = DeleteLabel(fakeLabelRepository)
        dummyLabels = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyLabels.add(
                Label(
                    labelId = index + 1,
                    name = "Label $c",
                )
            )
        }
        dummyLabels.forEach { fakeLabelRepository.create(it) }
    }

    @Test
    fun `delete board throws Exception if invalid ID`() {
        var label = dummyLabels.random().copy(labelId = 0)
        Assert.assertThrows(InvalidIdException::class.java) {
            runTest { deleteLabel(label) }
        }

        label = dummyLabels.random().copy(labelId = -2)
        Assert.assertThrows(InvalidIdException::class.java) {
            runTest { deleteLabel(label) }
        }
    }

    @Test
    fun `delete board does not remove any board`() = runTest {
        val latestId = dummyLabels.map { it.labelId }.max()
        val boardToDelete = dummyLabels.random().copy(labelId = latestId + 1)
        deleteLabel(boardToDelete)

        val labels = fakeLabelRepository.findSimpleLabels().first()
        Truth.assertThat(labels.size).isEqualTo(dummyLabels.size)
    }

    @Test
    fun `delete board removes board from the list`() = runTest {
        val boardToDelete = dummyLabels.random()
        deleteLabel(boardToDelete)

        val labels = fakeLabelRepository.findSimpleLabels().first()
        Truth.assertThat(labels.size).isLessThan(dummyLabels.size)
    }
}