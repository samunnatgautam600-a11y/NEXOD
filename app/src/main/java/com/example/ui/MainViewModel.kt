package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EducationalTopic
import com.example.data.model.FollowUpMessage
import com.example.data.model.QuizQuestion
import com.example.data.model.ResourceItem
import com.example.data.model.SearchMode
import com.example.data.model.SearchProgressStep
import com.example.data.repository.NexoraRepository
import com.example.data.search.SearchRankingEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEARCH_LOADING,
    TOPIC_DETAIL,
    QUIZ,
    SAVED,
    SETTINGS
}

data class UiState(
    val currentScreen: Screen = Screen.HOME,
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.QUICK,
    val webSearchEnabled: Boolean = true,
    val isAutoWebSearchDetected: Boolean = false,
    val progressStep: SearchProgressStep = SearchProgressStep.IDLE,
    val progressDetail: String = "",
    val currentTopic: EducationalTopic? = null,
    val resourceResults: List<ResourceItem> = emptyList(),
    val selectedResourceFilter: String = "All",
    val followUpMessages: List<FollowUpMessage> = emptyList(),
    val isAnsweringFollowUp: Boolean = false,
    val isCurrentTopicSaved: Boolean = false,
    val savedTopics: List<EducationalTopic> = emptyList(),
    val savedFilterQuery: String = "",
    // Quiz State
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionId -> optionIndex
    val isQuizSubmitted: Boolean = false,
    val quizScore: Int = 0,
    val selectedDifficultyFilter: String = "All",
    // Settings & Stats
    val geminiApiKey: String = "",
    val tavilyApiKey: String = "",
    val searchProvider: String = "open_web",
    val quizzesTakenCount: Int = 0,
    val averageQuizScore: Int = 0,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NexoraRepository(application)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Load settings and stats
        _uiState.update {
            it.copy(
                webSearchEnabled = repository.isWebSearchEnabled(),
                searchMode = repository.getDefaultSearchMode(),
                geminiApiKey = repository.getGeminiApiKey(),
                tavilyApiKey = repository.getTavilyApiKey(),
                searchProvider = repository.getActiveSearchProvider().providerId,
                quizzesTakenCount = repository.getQuizzesTakenCount(),
                averageQuizScore = repository.getAverageQuizScore()
            )
        }

        // Collect saved topics
        viewModelScope.launch {
            repository.getAllSavedTopics().collectLatest { savedList ->
                _uiState.update { state ->
                    val isSaved = state.currentTopic?.let { topic ->
                        savedList.any { it.query.equals(topic.query, ignoreCase = true) }
                    } ?: false
                    state.copy(
                        savedTopics = savedList,
                        isCurrentTopicSaved = isSaved
                    )
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, errorMessage = null) }
    }

    fun onSearchQueryChanged(query: String) {
        val isAuto = SearchRankingEngine.isQueryTimeSensitive(query)
        _uiState.update {
            it.copy(
                searchQuery = query,
                isAutoWebSearchDetected = isAuto,
                webSearchEnabled = if (isAuto) true else it.webSearchEnabled
            )
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _uiState.update { it.copy(searchMode = mode) }
    }

    fun toggleWebSearch(enabled: Boolean) {
        repository.setWebSearchEnabled(enabled)
        _uiState.update { it.copy(webSearchEnabled = enabled) }
    }

    fun executeSearch(query: String = _uiState.value.searchQuery) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchQuery = cleanQuery,
                    currentScreen = Screen.SEARCH_LOADING,
                    progressStep = SearchProgressStep.SEARCHING_WEB,
                    progressDetail = "Initiating live web search index...",
                    errorMessage = null,
                    followUpMessages = emptyList()
                )
            }

            if (_uiState.value.searchMode == SearchMode.RESOURCES) {
                try {
                    _uiState.update {
                        it.copy(
                            progressStep = SearchProgressStep.ANALYZING_DOMAINS,
                            progressDetail = "Searching official documentation, courses & legal libraries..."
                        )
                    }
                    delay(500)
                    val resources = repository.searchResources(cleanQuery)
                    val pseudoTopic = repository.searchAndSynthesize(
                        query = cleanQuery,
                        mode = SearchMode.RESOURCES,
                        forceWebSearch = _uiState.value.webSearchEnabled
                    ) { detail ->
                        _uiState.update { it.copy(progressDetail = detail) }
                    }

                    _uiState.update {
                        it.copy(
                            resourceResults = resources,
                            currentTopic = pseudoTopic,
                            quizQuestions = pseudoTopic.quizQuestions,
                            currentScreen = Screen.TOPIC_DETAIL,
                            progressStep = SearchProgressStep.COMPLETED
                        )
                    }
                    checkIfCurrentTopicSaved(cleanQuery)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            currentScreen = Screen.HOME,
                            errorMessage = "Search failed: ${e.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
                return@launch
            }

            try {
                // Step 1: Web search
                delay(300)
                _uiState.update {
                    it.copy(
                        progressStep = SearchProgressStep.SEARCHING_WEB,
                        progressDetail = "Connecting to web search providers..."
                    )
                }

                // Step 2: Ranking & Verification
                delay(300)
                _uiState.update {
                    it.copy(
                        progressStep = SearchProgressStep.ANALYZING_DOMAINS,
                        progressDetail = "Evaluating domain authority (.gov, .edu, scientific institutions)..."
                    )
                }

                val topic = repository.searchAndSynthesize(
                    query = cleanQuery,
                    mode = _uiState.value.searchMode,
                    forceWebSearch = _uiState.value.webSearchEnabled
                ) { detail ->
                    _uiState.update {
                        it.copy(
                            progressStep = SearchProgressStep.SYNTHESIZING_AI,
                            progressDetail = detail
                        )
                    }
                }

                // Step 3: Quiz formulation
                _uiState.update {
                    it.copy(
                        progressStep = SearchProgressStep.GENERATING_QUIZ,
                        progressDetail = "Grounding interactive 5-question quiz strictly in retrieved sources..."
                    )
                }
                delay(200)

                _uiState.update {
                    it.copy(
                        currentTopic = topic,
                        quizQuestions = topic.quizQuestions,
                        currentScreen = Screen.TOPIC_DETAIL,
                        progressStep = SearchProgressStep.COMPLETED,
                        selectedAnswers = emptyMap(),
                        isQuizSubmitted = false,
                        currentQuestionIndex = 0
                    )
                }

                checkIfCurrentTopicSaved(cleanQuery)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        currentScreen = Screen.HOME,
                        errorMessage = "Search failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun checkIfCurrentTopicSaved(query: String) {
        viewModelScope.launch {
            val isSaved = _uiState.value.savedTopics.any { it.query.equals(query, ignoreCase = true) }
            _uiState.update { it.copy(isCurrentTopicSaved = isSaved) }
        }
    }

    fun toggleSaveCurrentTopic() {
        val topic = _uiState.value.currentTopic ?: return
        viewModelScope.launch {
            if (_uiState.value.isCurrentTopicSaved) {
                repository.removeSavedTopic(topic.query)
                _uiState.update { it.copy(isCurrentTopicSaved = false) }
            } else {
                repository.saveTopic(topic)
                _uiState.update { it.copy(isCurrentTopicSaved = true) }
            }
        }
    }

    fun openSavedTopic(topic: EducationalTopic) {
        _uiState.update {
            it.copy(
                currentTopic = topic,
                searchQuery = topic.query,
                searchMode = topic.searchMode,
                quizQuestions = topic.quizQuestions,
                currentScreen = Screen.TOPIC_DETAIL,
                isCurrentTopicSaved = true,
                selectedAnswers = emptyMap(),
                isQuizSubmitted = false,
                currentQuestionIndex = 0
            )
        }
    }

    fun removeSavedTopic(topic: EducationalTopic) {
        viewModelScope.launch {
            repository.removeSavedTopic(topic.query)
        }
    }

    fun setSavedFilterQuery(query: String) {
        _uiState.update { it.copy(savedFilterQuery = query) }
    }

    fun setSelectedResourceFilter(filter: String) {
        _uiState.update { it.copy(selectedResourceFilter = filter) }
    }

    // AI Follow-Up Operations
    fun sendFollowUp(questionText: String) {
        val topic = _uiState.value.currentTopic ?: return
        val clean = questionText.trim()
        if (clean.isBlank()) return

        val userMsg = FollowUpMessage(sender = "user", content = clean)
        _uiState.update {
            it.copy(
                followUpMessages = it.followUpMessages + userMsg,
                isAnsweringFollowUp = true
            )
        }

        viewModelScope.launch {
            val answer = repository.answerFollowUp(topic, clean)
            val assistantMsg = FollowUpMessage(sender = "nexora", content = answer)
            _uiState.update {
                it.copy(
                    followUpMessages = it.followUpMessages + assistantMsg,
                    isAnsweringFollowUp = false
                )
            }
        }
    }

    // Quiz Operations
    fun startQuiz() {
        val topic = _uiState.value.currentTopic ?: return
        _uiState.update {
            it.copy(
                quizQuestions = topic.quizQuestions,
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                isQuizSubmitted = false,
                quizScore = 0,
                currentScreen = Screen.QUIZ
            )
        }
    }

    fun selectQuizAnswer(questionId: Int, optionIndex: Int) {
        if (_uiState.value.isQuizSubmitted) return
        _uiState.update {
            val updated = it.selectedAnswers.toMutableMap()
            updated[questionId] = optionIndex
            it.copy(selectedAnswers = updated)
        }
    }

    fun nextQuizQuestion() {
        _uiState.update {
            if (it.currentQuestionIndex < it.quizQuestions.size - 1) {
                it.copy(currentQuestionIndex = it.currentQuestionIndex + 1)
            } else it
        }
    }

    fun previousQuizQuestion() {
        _uiState.update {
            if (it.currentQuestionIndex > 0) {
                it.copy(currentQuestionIndex = it.currentQuestionIndex - 1)
            } else it
        }
    }

    fun submitQuiz() {
        val state = _uiState.value
        var correct = 0
        state.quizQuestions.forEach { q ->
            val userPick = state.selectedAnswers[q.id]
            if (userPick == q.correctIndex) {
                correct++
            }
        }
        repository.recordQuizScore(correct)
        _uiState.update {
            it.copy(
                isQuizSubmitted = true,
                quizScore = correct,
                quizzesTakenCount = repository.getQuizzesTakenCount(),
                averageQuizScore = repository.getAverageQuizScore()
            )
        }
    }

    fun retakeQuiz() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                isQuizSubmitted = false,
                quizScore = 0
            )
        }
    }

    // Settings Updates
    fun updateGeminiApiKey(key: String) {
        repository.setGeminiApiKey(key)
        _uiState.update { it.copy(geminiApiKey = key) }
    }

    fun updateTavilyApiKey(key: String) {
        repository.setTavilyApiKey(key)
        _uiState.update { it.copy(tavilyApiKey = key) }
    }

    fun updateSearchProvider(providerId: String) {
        repository.setActiveSearchProvider(providerId)
        _uiState.update { it.copy(searchProvider = providerId) }
    }

    fun updateDefaultSearchMode(mode: SearchMode) {
        repository.setDefaultSearchMode(mode)
        _uiState.update { it.copy(searchMode = mode) }
    }
}
