package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
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
    fun `Should throw NegativeIdException when ID is invalid`() {
        var label = dummyLabels.random().copy(labelId = 0)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { deleteLabel(label) }
        }

        label = dummyLabels.random().copy(labelId = -2)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { deleteLabel(label) }
        }
    }

    @Test
    fun `Should throw NotFoundException when label isn't found`() {
        val latestId = dummyLabels.map { it.labelId }.max()
        val label = dummyLabels.random().copy(labelId = latestId + 1)
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { deleteLabel(label) }
        }
    }

    @Test
    fun `Should remove label from the list when it is found`() = runTest {
        deleteLabel(dummyLabels.random())
        fakeLabelRepository.findSimpleLabels().first().let {
            assertThat(it.size).isLessThan(dummyLabels.size)
        }
    }
}