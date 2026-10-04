package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.ai.EducationalAiSynthesizer
import com.example.data.local.NexoraDatabase
import com.example.data.local.SavedTopicEntity
import com.example.data.model.EducationalTopic
import com.example.data.model.ResourceItem
import com.example.data.model.SearchMode
import com.example.data.model.WebSource
import com.example.data.search.LiveOpenWebSearchProvider
import com.example.data.search.SearchRankingEngine
import com.example.data.search.TavilySearchProvider
import com.example.data.search.WebSearchProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLEncoder

class NexoraRepository(context: Context) {

    private val db = NexoraDatabase.getInstance(context)
    private val topicDao = db.topicDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexora_preferences", Context.MODE_PRIVATE)

    private val openWebProvider = LiveOpenWebSearchProvider()
    private val tavilyProvider = TavilySearchProvider(apiKeyProvider = { getTavilyApiKey() })
    private val aiSynthesizer = EducationalAiSynthesizer()

    fun getActiveSearchProvider(): WebSearchProvider {
        val providerKey = prefs.getString("search_provider", "open_web") ?: "open_web"
        return when (providerKey) {
            "tavily" -> tavilyProvider
            else -> openWebProvider
        }
    }

    fun setActiveSearchProvider(providerId: String) {
        prefs.edit().putString("search_provider", providerId).apply()
    }

    fun getGeminiApiKey(): String {
        return prefs.getString("gemini_api_key", "") ?: ""
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun getTavilyApiKey(): String {
        val userKey = prefs.getString("tavily_api_key", "") ?: ""
        return when {
            userKey.isNotBlank() -> userKey
            BuildConfig.TAVILY_API_KEY.isNotBlank() && BuildConfig.TAVILY_API_KEY != "MY_TAVILY_API_KEY" -> BuildConfig.TAVILY_API_KEY
            else -> ""
        }
    }

    fun setTavilyApiKey(key: String) {
        prefs.edit().putString("tavily_api_key", key.trim()).apply()
    }

    fun isWebSearchEnabled(): Boolean {
        return prefs.getBoolean("web_search_enabled", true)
    }

    fun setWebSearchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("web_search_enabled", enabled).apply()
    }

    fun getDefaultSearchMode(): SearchMode {
        val name = prefs.getString("default_search_mode", SearchMode.QUICK.name) ?: SearchMode.QUICK.name
        return try {
            SearchMode.valueOf(name)
        } catch (e: Exception) {
            SearchMode.QUICK
        }
    }

    fun setDefaultSearchMode(mode: SearchMode) {
        prefs.edit().putString("default_search_mode", mode.name).apply()
    }

    fun getQuizzesTakenCount(): Int {
        return prefs.getInt("quizzes_taken", 0)
    }

    fun incrementQuizzesTaken() {
        val current = getQuizzesTakenCount()
        prefs.edit().putInt("quizzes_taken", current + 1).apply()
    }

    fun getAverageQuizScore(): Int {
        val total = prefs.getInt("total_quiz_score", 0)
        val count = getQuizzesTakenCount()
        return if (count > 0) (total / count) else 0
    }

    fun recordQuizScore(scoreOutOfFive: Int) {
        incrementQuizzesTaken()
        val total = prefs.getInt("total_quiz_score", 0)
        val percent = (scoreOutOfFive * 100) / 5
        prefs.edit().putInt("total_quiz_score", total + percent).apply()
    }

    suspend fun searchAndSynthesize(
        query: String,
        mode: SearchMode,
        forceWebSearch: Boolean,
        onProgress: (String) -> Unit
    ): EducationalTopic {
        val effectiveWebSearch = forceWebSearch || SearchRankingEngine.isQueryTimeSensitive(query)

        val sources = if (effectiveWebSearch) {
            onProgress("Querying live web index and peer-reviewed repositories...")
            val provider = getActiveSearchProvider()
            val maxCount = when (mode) {
                SearchMode.QUICK -> 4
                SearchMode.DEEP -> 8
                SearchMode.RESOURCES -> 6
            }
            val rawSources = provider.search(query, mode, maxCount)
            onProgress("Analyzing source credibility & ranking primary institutions (.gov, .edu)...")
            SearchRankingEngine.rankAndFilterSources(rawSources)
        } else {
            emptyList()
        }

        onProgress("Synthesizing educational explanation & factual citations...")
        val topic = aiSynthesizer.synthesizeTopic(
            query = query,
            sources = sources,
            mode = mode,
            apiKeyOverride = getGeminiApiKey()
        )

        return topic
    }

    suspend fun searchResources(query: String): List<ResourceItem> {
        val clean = query.trim()
        val encoded = URLEncoder.encode(clean, "UTF-8")
        return listOf(
            ResourceItem(
                title = "$clean Official Guide & Reference",
                category = "Documentation",
                url = "https://developer.mozilla.org/en-US/search?q=$encoded",
                source = "MDN Web Docs / Official Documentation",
                description = "Comprehensive standard specifications, tutorials, and verified developer guides.",
                badge = "Official Reference"
            ),
            ResourceItem(
                title = "Introduction to $clean - MIT OpenCourseWare",
                category = "Courses",
                url = "https://ocw.mit.edu/search/?q=$encoded",
                source = "MIT OpenCourseWare",
                description = "Free peer-reviewed university lecture notes, problem sets, and syllabus recordings.",
                badge = "Free University Course"
            ),
            ResourceItem(
                title = "Open Access Scholarly Papers on $clean",
                category = "Research Papers",
                url = "https://arxiv.org/search/?query=$encoded&searchtype=all",
                source = "arXiv.org Cornell University",
                description = "Pre-print e-prints in Physics, Mathematics, Computer Science, and Quantitative Biology.",
                badge = "Open Access Paper"
            ),
            ResourceItem(
                title = "Legal Books & Library Access for $clean",
                category = "Books",
                url = "https://openlibrary.org/search?q=$encoded",
                source = "Internet Archive Open Library",
                description = "Borrow and read verified published textbooks and literature legally online.",
                badge = "Legal Library Loan"
            ),
            ResourceItem(
                title = "Interactive Educational Articles on $clean",
                category = "Articles",
                url = "https://en.wikipedia.org/wiki/$encoded",
                source = "Global Educational Encyclopedia",
                description = "Curated foundational overview with extensive citations and historical milestones.",
                badge = "Peer-Reviewed Article"
            ),
            ResourceItem(
                title = "Video Lectures & Visual Demonstrations: $clean",
                category = "Videos",
                url = "https://www.youtube.com/results?search_query=${encoded}+educational+lecture",
                source = "Verified Academic Channels (Khan, 3Blue1Brown, MIT)",
                description = "Step-by-step visual explanations and laboratory demonstrations.",
                badge = "Video Lecture"
            )
        )
    }

    suspend fun answerFollowUp(
        topic: EducationalTopic,
        question: String
    ): String {
        val additionalSources = if (SearchRankingEngine.isQueryTimeSensitive(question)) {
            val provider = getActiveSearchProvider()
            provider.search(question, SearchMode.QUICK, 3)
        } else {
            emptyList()
        }

        return aiSynthesizer.answerFollowUp(
            topic = topic,
            question = question,
            additionalSources = additionalSources,
            apiKeyOverride = getGeminiApiKey()
        )
    }

    // Room Database Operations
    fun getAllSavedTopics(): Flow<List<EducationalTopic>> {
        return topicDao.getAllSavedTopics().map { list ->
            list.map { it.toEducationalTopic() }
        }
    }

    fun isTopicSaved(query: String): Flow<Boolean> {
        return topicDao.isTopicSaved(query)
    }

    suspend fun saveTopic(topic: EducationalTopic, category: String = "General") {
        val entity = SavedTopicEntity.fromEducationalTopic(topic, category)
        topicDao.insertTopic(entity)
    }

    suspend fun removeSavedTopic(query: String) {
        topicDao.deleteTopicByQuery(query)
    }

    suspend fun getSavedTopicByQuery(query: String): EducationalTopic? {
        return topicDao.getTopicByQuery(query)?.toEducationalTopic()
    }
}
