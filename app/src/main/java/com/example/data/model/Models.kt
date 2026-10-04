package com.example.data.model

import com.squareup.moshi.JsonClass

enum class SearchMode(val displayName: String, val description: String) {
    QUICK("Quick", "Fast answer using focused web sources"),
    DEEP("Deep", "In-depth multi-source educational synthesis"),
    RESOURCES("Resources", "Curated learning materials & official docs")
}

@JsonClass(generateAdapter = true)
data class WebSource(
    val title: String,
    val url: String,
    val snippet: String,
    val sourceName: String,
    val date: String? = null,
    val domainCategory: String = "Educational Website",
    val credibilityScore: Int = 85
)

@JsonClass(generateAdapter = true)
data class EducationalTopic(
    val query: String,
    val quickAnswer: String,
    val learnMore: String,
    val keyPoints: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val recentDevelopments: List<String> = emptyList(),
    val relatedTopics: List<String> = emptyList(),
    val sources: List<WebSource> = emptyList(),
    val isTimeSensitive: Boolean = false,
    val confidenceLevel: String = "High Confidence • Multiple Verified Sources",
    val sourceConsensus: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val searchMode: SearchMode = SearchMode.QUICK,
    val quizQuestions: List<QuizQuestion> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val sourceReference: String,
    val isTrueFalse: Boolean = false,
    val difficulty: String = "Intermediate"
)

@JsonClass(generateAdapter = true)
data class ResourceItem(
    val title: String,
    val category: String, // Books, Articles, Documentation, Courses, Tutorials, Research Papers, Videos, Websites
    val url: String,
    val source: String,
    val description: String,
    val badge: String = "Verified"
)

data class FollowUpMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "nexora"
    val content: String,
    val sources: List<WebSource> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class SearchProgressStep(val title: String, val subtitle: String) {
    IDLE("Ready", "Enter a topic to begin"),
    SEARCHING_WEB("Querying Live Web", "Retrieving real-time indexed knowledge and articles..."),
    ANALYZING_DOMAINS("Ranking & Verifying Sources", "Prioritizing .gov, .edu, scientific papers & official documentation..."),
    SYNTHESIZING_AI("AI Educational Synthesis", "Structuring explanations, key concepts, and citations..."),
    GENERATING_QUIZ("Grounding Quiz Generator", "Formulating 5 interactive test questions strictly from source facts..."),
    COMPLETED("Complete", "Knowledge synthesized successfully"),
    ERROR("Search Error", "Unable to complete request")
}
