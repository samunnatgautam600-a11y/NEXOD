package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EducationalTopic
import com.example.data.model.QuizQuestion
import com.example.data.model.SearchMode
import com.example.data.model.WebSource

@Entity(tableName = "saved_topics")
data class SavedTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val quickAnswer: String,
    val learnMore: String,
    val keyPoints: List<String>,
    val examples: List<String>,
    val recentDevelopments: List<String>,
    val relatedTopics: List<String>,
    val sources: List<WebSource>,
    val isTimeSensitive: Boolean,
    val confidenceLevel: String,
    val sourceConsensus: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val searchMode: String = SearchMode.QUICK.name,
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val category: String = "General"
) {
    fun toEducationalTopic(): EducationalTopic {
        val mode = try {
            SearchMode.valueOf(searchMode)
        } catch (e: Exception) {
            SearchMode.QUICK
        }
        return EducationalTopic(
            query = query,
            quickAnswer = quickAnswer,
            learnMore = learnMore,
            keyPoints = keyPoints,
            examples = examples,
            recentDevelopments = recentDevelopments,
            relatedTopics = relatedTopics,
            sources = sources,
            isTimeSensitive = isTimeSensitive,
            confidenceLevel = confidenceLevel,
            sourceConsensus = sourceConsensus,
            timestamp = timestamp,
            searchMode = mode,
            quizQuestions = quizQuestions
        )
    }

    companion object {
        fun fromEducationalTopic(topic: EducationalTopic, category: String = "General"): SavedTopicEntity {
            return SavedTopicEntity(
                query = topic.query,
                quickAnswer = topic.quickAnswer,
                learnMore = topic.learnMore,
                keyPoints = topic.keyPoints,
                examples = topic.examples,
                recentDevelopments = topic.recentDevelopments,
                relatedTopics = topic.relatedTopics,
                sources = topic.sources,
                isTimeSensitive = topic.isTimeSensitive,
                confidenceLevel = topic.confidenceLevel,
                sourceConsensus = topic.sourceConsensus,
                timestamp = topic.timestamp,
                searchMode = topic.searchMode.name,
                quizQuestions = topic.quizQuestions,
                category = category
            )
        }
    }
}
