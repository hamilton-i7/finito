package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.util.ResourceException
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
class CreateLabelTest {
    private lateinit var createLabel: CreateLabel
    private lateinit var fakeLabelRepository: FakeLabelRepository

    @Before
    fun setUp() {
        fakeLabelRepository = FakeLabelRepository()
        createLabel = CreateLabel(fakeLabelRepository)
    }

    @Test
    fun `Should throw EmptyException when label name is empty`() {
        var label = Label(name = "")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { createLabel(label) }
        }

        label = Label(name = "    ")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { createLabel(label) }
        }
    }

    @Test
    fun `Should insert new label into list when label state is valid`() = runTest {
        val label = Label(name = "Label name")
        var labels = fakeLabelRepository.findSimpleLabels().first()

        assertThat(labels.size).isEqualTo(0)
        createLabel(label)
        createLabel(label)

        labels = fakeLabelRepository.findSimpleLabels().first()
        assertThat(labels.size).isEqualTo(2)
    }
}