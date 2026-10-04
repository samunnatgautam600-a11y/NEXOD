package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.search.SearchRankingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NEXORA", appName)
  }

  @Test
  fun `test domain categorization for educational sources`() {
    val (catGov, scoreGov) = SearchRankingEngine.categorizeDomain("https://science.nasa.gov/universe/black-holes/")
    assertEquals("Government Organization", catGov)
    assertTrue(scoreGov >= 95)

    val (catEdu, scoreEdu) = SearchRankingEngine.categorizeDomain("https://ocw.mit.edu/courses/physics")
    assertEquals("University / Academic", catEdu)
    assertTrue(scoreEdu >= 90)
  }

  @Test
  fun `test time-sensitivity query detector`() {
    assertTrue(SearchRankingEngine.isQueryTimeSensitive("Latest discoveries about black holes"))
    assertTrue(SearchRankingEngine.isQueryTimeSensitive("Fusion breakthroughs in 2026"))
  }
}
