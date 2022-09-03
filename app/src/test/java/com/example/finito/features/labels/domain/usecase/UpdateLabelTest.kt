package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `Should throw InvalidIdException when ID is invalid`() {
        var label = dummyLabels.random().copy(labelId = -3)
        assertThrows(ResourceException.InvalidIdException::class.java) {
            runTest { updateLabel(label) }
        }

        label = dummyLabels.random().copy(labelId = 0)
        assertThrows(ResourceException.InvalidIdException::class.java) {
            runTest { updateLabel(label) }
        }
    }

    @Test
    fun `Should throw EmptyException when name is empty`() {
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
    fun `Should throw NotFoundException if no label is found`() {
        val label = dummyLabels.random().copy(labelId = 10_000)
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { updateLabel(label) }
        }
    }

    @Test
    fun `Should update label when it's found & its state is valid`() = runTest {
        val label = dummyLabels.random()
        assertThat(dummyLabels.find { it.labelId == label.labelId }?.name).startsWith("Label")

        val updatedLabel = label.copy(name = "Updated label")
        updateLabel(updatedLabel)

        assertThat(fakeLabelRepository.findOne(updatedLabel.labelId)?.name)
            .isEqualTo("Updated label")
    }
}