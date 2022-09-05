package com.example.finito.features.labels.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LabelRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: FinitoDatabase
    private lateinit var labelRepositoryImpl: LabelRepositoryImpl
    private lateinit var dummyLabels: List<Label>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, FinitoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        labelRepositoryImpl = LabelRepositoryImpl(db.labelDao)
    }

    @Before
    fun prepopulate() = runTest {
        ('A'..'C').forEach {
            db.labelDao.create(
                Label(name = "Label $it")
            )
        }
        dummyLabels = db.labelDao.findAll()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun should_insert_label_into_list() = runTest {
        listOf(
            Label(name = "New label"),
            Label(name = "New label"),
            Label(name = "New label"),
        ).onEach { labelRepositoryImpl.create(it) }
        assertThat(labelRepositoryImpl.findAll().size).isGreaterThan(dummyLabels.size)
    }

    @Test
    fun should_return_simple_labels() = runTest {
        assertThat(labelRepositoryImpl.findSimpleLabels().first()).isNotEmpty()
    }

    @Test
    fun should_update_requested_label() = runTest {
        val label = dummyLabels.random()
        assertThat(label.name).startsWith("Label")

        labelRepositoryImpl.update(label.copy(name = "Updated name"))
        assertThat(db.labelDao.findOne(label.labelId)?.name).isEqualTo("Updated name")
    }

    @Test
    fun should_remove_label_from_list() = runTest {
        labelRepositoryImpl.remove(dummyLabels.random())
        assertThat(db.labelDao.findAll().size).isLessThan(dummyLabels.size)
    }
}