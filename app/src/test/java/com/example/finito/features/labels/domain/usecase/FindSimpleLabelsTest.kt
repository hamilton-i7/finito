package com.example.finito.features.labels.domain.usecase

import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindSimpleLabelsTest {

    private lateinit var findSimpleLabels: FindSimpleLabels
    private lateinit var fakeLabelRepository: FakeLabelRepository
    private lateinit var dummyLabels: MutableList<Label>

    @Before
    fun setUp() = runTest {
        fakeLabelRepository = FakeLabelRepository()
        findSimpleLabels = FindSimpleLabels(fakeLabelRepository)
        dummyLabels = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyLabels.add(
                Label(
                    labelId = index + 1,
                    name = if (index % 2 == 0) "Label $c" else "lÁbËl $c",
                )
            )
        }
        dummyLabels.shuffle()
        dummyLabels.forEach { fakeLabelRepository.create(it) }
    }

    @Test
    fun `find simple boards returns all labels`() = runTest {
        val labels = findSimpleLabels().first()
        assertThat(labels).isNotEmpty()
        assertThat(labels.size).isEqualTo(dummyLabels.size)
    }

    @Test
    fun `find simple labels returns sorted labels by name ascending`() = runTest {
        val sortedLabels = findSimpleLabels().first()

        for (i in 0..sortedLabels.size - 2) {
            assertThat(sortedLabels[i].normalizedName)
                .isLessThan(sortedLabels[i+1].normalizedName)
        }
    }
}