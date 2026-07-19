package com.plantsense.ai.presentation.camera

import app.cash.turbine.test
import com.plantsense.ai.domain.usecase.GetApiKeyUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val getApiKeyUseCase: GetApiKeyUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun hasApiKey_whenKeyIsNotEmpty_emitsTrue() = runTest {
        every { getApiKeyUseCase() } returns flowOf("api_key_123")
        val viewModel = CameraViewModel(getApiKeyUseCase)

        viewModel.hasApiKey.test {
            testDispatcher.scheduler.runCurrent()
            assertTrue(awaitItem()) // initial value
            expectNoEvents()
        }
    }

    @Test
    fun hasApiKey_whenKeyIsEmpty_emitsFalse() = runTest {
        every { getApiKeyUseCase() } returns flowOf("")
        val viewModel = CameraViewModel(getApiKeyUseCase)

        viewModel.hasApiKey.test {
            testDispatcher.scheduler.runCurrent()
            assertTrue(awaitItem()) // initial value
            assertFalse(awaitItem()) // updated mapped value
        }
    }
}
