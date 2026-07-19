package com.plantsense.ai.domain.usecase

import com.plantsense.ai.core.network.NetworkResult
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.domain.repository.PlantRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentifyPlantUseCaseTest {

    private val repository: PlantRepository = mockk()
    private val useCase = IdentifyPlantUseCase(repository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val expectedResult = PlantIdentificationResult(
            plantName = "Fern",
            botanicalName = "Polypodiopsida",
            confidence = 0.95,
            description = "A green fern",
            careLight = "Indirect",
            careWater = "Moist",
            careTemp = "Warm"
        )
        coEvery { repository.identifyPlant("test_path.jpg") } returns NetworkResult.Success(expectedResult)

        val result = useCase("test_path.jpg")
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedResult, (result as NetworkResult.Success).data)
    }
}
