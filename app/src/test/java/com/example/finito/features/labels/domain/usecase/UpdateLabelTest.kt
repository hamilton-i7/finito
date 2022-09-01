package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.ResourceException
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateLabelTest {
    private lateinit var updateLabel: UpdateLabel
    private lateinit var fakeLabelRepository: FakeLabelRepository
    private lateinit var dummyLabels: MutableList<Label>

    @Before
    fun setUp() = runTest {
        fakeLabelRepository = FakeLabelRepository()
        updateLabel = UpdateLabel(fakeLabelRepository)
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
    fun `update label throws Exception if invalid ID`() {
        var label = dummyLabels.random().copy(labelId = -3)
        assertThrows(InvalidIdException::class.java) {
            runTest { updateLabel(label) }
        }

        label = dummyLabels.random().copy(labelId = 0)
        assertThrows(InvalidIdException::class.java) {
            runTest { updateLabel(label) }
        }
    }

    @Test
    fun `update label throws Exception if invalid name`() {
        var label = dummyLabels.random().copy(name = "")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateLabel(label) }
        }

        label = dummyLabels.random().copy(name = "     ")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateLabel(label) }
        }
    }

    @Test
    fun `update label makes changes to requested label`() = runTest {
        val label = dummyLabels.random()
        Truth.assertThat(dummyLabels.find { it.labelId == label.labelId }?.name).startsWith("Label")

        val updatedLabel = label.copy(name = "Updated label")
        updateLabel(updatedLabel)

        Truth.assertThat(fakeLabelRepository.findOne(updatedLabel.labelId)?.name)
            .isEqualTo("Updated label")
    }

    @Test
    fun `update label makes no changes`() = runTest {
        val label = dummyLabels.random()
        Truth.assertThat(dummyLabels.find { it.labelId == label.labelId }?.name).startsWith("Label")
        val latestId = dummyLabels.map { it.labelId }.max()

        val updatedLabel = label.copy(
            labelId = latestId + 1,
            name = "Updated label",
        )
        updateLabel(updatedLabel)
        Truth.assertThat(fakeLabelRepository.findSimpleLabels().first().any {
            it.name == "Updated label"
        }).isFalse()
    }
}