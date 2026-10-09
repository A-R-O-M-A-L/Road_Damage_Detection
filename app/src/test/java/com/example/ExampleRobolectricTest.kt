package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ComplaintRepository
import com.example.model.PipelineStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    ComplaintRepository.resetToDefaults()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CivicFix", appName)
  }

  @Test
  fun `complaint 2041 seeded in repository`() {
    val complaints = ComplaintRepository.complaints.value
    val c2041 = complaints.find { it.id == "#2041" }
    assertNotNull(c2041)
    assertEquals(PipelineStage.DISPATCH, c2041?.stage)
    assertTrue(c2041?.formattedCoordinates()?.contains("37.77490° N") == true)
  }

  @Test
  fun `assign engineer and mark repaired updates repository state`() {
    val engineer = ComplaintRepository.availableEngineers.first()
    ComplaintRepository.assignEngineer("#2041", engineer, 12)
    val updated = ComplaintRepository.complaints.value.find { it.id == "#2041" }
    assertEquals(engineer.id, updated?.assignedEngineer?.id)
    assertEquals(PipelineStage.DISPATCH, updated?.stage)

    ComplaintRepository.markRepaired("#2041", "Pothole filled and sealed.")
    val repaired = ComplaintRepository.complaints.value.find { it.id == "#2041" }
    assertTrue(repaired?.isRepaired == true)
    assertEquals(PipelineStage.VERIFY, repaired?.stage)
  }
}
