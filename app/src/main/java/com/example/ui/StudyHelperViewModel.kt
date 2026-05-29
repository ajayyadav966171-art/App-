package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

enum class StudyTab {
    DASHBOARD,
    ASK,
    SUMMARIZER,
    QUIZ,
    FLASHCARDS,
    PLANNER
}

// Inline helper models parsed from cached JSON
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class FlashCardItem(
    val front: String,
    val back: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudyHelperViewModel(
    application: Application,
    private val repository: StudyHelperRepository
) : AndroidViewModel(application) {

    // --- Tab state ---
    private val _currentTab = MutableStateFlow(StudyTab.DASHBOARD)
    val currentTab: StateFlow<StudyTab> = _currentTab.asStateFlow()

    fun setTab(tab: StudyTab) {
        _currentTab.value = tab
    }

    // --- Dark / Light Theme Toggle State ---
    private val _isDarkMode = MutableStateFlow(true) // Defaults to sophisticated dark
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- User Session & Authentication States ---
    val currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserName = MutableStateFlow<String?>(null)

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _signupError = MutableStateFlow<String?>(null)
    val signupError = _signupError.asStateFlow()

    fun clearAuthErrors() {
        _loginError.value = null
        _signupError.value = null
    }

    // --- Dynamic DB Flow collections filtered by active currentUserEmail ---
    val questionsList: StateFlow<List<StudyQuestion>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getQuestions(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summariesList: StateFlow<List<NoteSummary>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getNoteSummaries(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzesList: StateFlow<List<QuizSet>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getQuizzes(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcardsList: StateFlow<List<FlashcardSet>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getFlashcardSets(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plansList: StateFlow<List<StudyPlan>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getStudyPlans(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studiedTopicsList: StateFlow<List<StudiedTopic>> = currentUserEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.getStudiedTopics(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- INPUT BUFFERS & LOADING STATES ---

    // 1. Ask Q&A State
    val askInput = MutableStateFlow("")
    private val _askLoading = MutableStateFlow(false)
    val askLoading = _askLoading.asStateFlow()
    private val _lastAnswer = MutableStateFlow<String?>(null)
    val lastAnswer = _lastAnswer.asStateFlow()

    // 2. Notes Summary State
    val notesInput = MutableStateFlow("")
    private val _summaryLoading = MutableStateFlow(false)
    val summaryLoading = _summaryLoading.asStateFlow()
    private val _lastSummary = MutableStateFlow<String?>(null)
    val lastSummary = _lastSummary.asStateFlow()

    // 3. Quiz Generation State
    val quizTopicInput = MutableStateFlow("")
    private val _quizLoading = MutableStateFlow(false)
    val quizLoading = _quizLoading.asStateFlow()
    private val _quizStatusMessage = MutableStateFlow<String?>(null)
    val quizStatusMessage = _quizStatusMessage.asStateFlow()

    // 4. Flashcard Generation State
    val flashcardsTopicInput = MutableStateFlow("")
    private val _flashcardsLoading = MutableStateFlow(false)
    val flashcardsLoading = _flashcardsLoading.asStateFlow()
    private val _flashcardStatusMessage = MutableStateFlow<String?>(null)
    val flashcardStatusMessage = _flashcardStatusMessage.asStateFlow()

    // 5. Planner State
    val planExamDateInput = MutableStateFlow("")
    val planSubjectsInput = MutableStateFlow("")
    private val _planLoading = MutableStateFlow(false)
    val planLoading = _planLoading.asStateFlow()
    private val _lastPlanText = MutableStateFlow<String?>(null)
    val lastPlanText = _lastPlanText.asStateFlow()


    // --- GAMEPLAY ENGINES ---

    // Interactive Quiz Gameplay State
    private val _playingQuizSet = MutableStateFlow<QuizSet?>(null)
    val playingQuizSet = _playingQuizSet.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex = _currentQuizIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption = _selectedOption.asStateFlow()

    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked = _isAnswerChecked.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _quizGameplayFinished = MutableStateFlow(false)
    val quizGameplayFinished = _quizGameplayFinished.asStateFlow()

    // Playback Flashcard Deck State
    private val _activeFlashcardSet = MutableStateFlow<FlashcardSet?>(null)
    val activeFlashcardSet = _activeFlashcardSet.asStateFlow()

    private val _activeCards = MutableStateFlow<List<FlashCardItem>>(emptyList())
    val activeCards = _activeCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped = _isCardFlipped.asStateFlow()


    // --- SERVICE HANDLERS ---

    // 1. Submit Question Action
    fun submitQuestion() {
        val email = currentUserEmail.value ?: return
        val query = askInput.value.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _askLoading.value = true
            _lastAnswer.value = null
            val result = repository.askQuestion(email, query)
            _lastAnswer.value = result
            _askLoading.value = false
            if (!result.startsWith("Error")) {
                askInput.value = "" // clear input on success
            }
        }
    }

    // 2. Submit Note Summarize Action
    fun submitSummarize() {
        val email = currentUserEmail.value ?: return
        val text = notesInput.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _summaryLoading.value = true
            _lastSummary.value = null
            val result = repository.summarizeNotes(email, text)
            _lastSummary.value = result
            _summaryLoading.value = false
            if (!result.startsWith("Error")) {
                notesInput.value = ""
            }
        }
    }

    // 3. Submit Quiz Generation Action
    fun submitGenerateQuiz() {
        val email = currentUserEmail.value ?: return
        val topic = quizTopicInput.value.trim()
        if (topic.isEmpty()) return

        viewModelScope.launch {
            _quizLoading.value = true
            _quizStatusMessage.value = null
            val result = repository.generateQuiz(email, topic)
            if (result == "SUCCESS") {
                _quizStatusMessage.value = "Quiz on \"$topic\" generated successfully!"
                quizTopicInput.value = ""
            } else {
                _quizStatusMessage.value = result // Show error details
            }
            _quizLoading.value = false
        }
    }

    // 4. Submit Flashcards Generation Action
    fun submitGenerateFlashcards() {
        val email = currentUserEmail.value ?: return
        val topic = flashcardsTopicInput.value.trim()
        if (topic.isEmpty()) return

        viewModelScope.launch {
            _flashcardsLoading.value = true
            _flashcardStatusMessage.value = null
            val result = repository.generateFlashcards(email, topic)
            if (result == "SUCCESS") {
                _flashcardStatusMessage.value = "Flashcards deck on \"$topic\" generated successfully!"
                flashcardsTopicInput.value = ""
            } else {
                _flashcardStatusMessage.value = result
            }
            _flashcardsLoading.value = false
        }
    }

    // 5. Submit Study Plan Action
    fun submitGeneratePlan() {
        val email = currentUserEmail.value ?: return
        val examDate = planExamDateInput.value.trim()
        val subjects = planSubjectsInput.value.trim()
        if (examDate.isEmpty() || subjects.isEmpty()) return

        viewModelScope.launch {
            _planLoading.value = true
            _lastPlanText.value = null
            val result = repository.generateStudyPlan(email, examDate, subjects)
            _lastPlanText.value = result
            _planLoading.value = false
            if (!result.startsWith("Error")) {
                planExamDateInput.value = ""
                planSubjectsInput.value = ""
            }
        }
    }

    // --- GAMEPLAY ACTIONS ---

    // Interactive Quiz Start
    fun startQuizGame(quizSet: QuizSet) {
        try {
            val list = mutableListOf<QuizQuestion>()
            val array = JSONArray(quizSet.questionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val optsArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optsArray.length()) {
                    options.add(optsArray.getString(j))
                }
                list.add(
                    QuizQuestion(
                        question = obj.optString("question", "Question $i"),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", "")
                    )
                )
            }
            _quizQuestions.value = list
            _playingQuizSet.value = quizSet
            _currentQuizIndex.value = 0
            _selectedOption.value = null
            _isAnswerChecked.value = false
            _quizScore.value = 0
            _quizGameplayFinished.value = false
            setTab(StudyTab.QUIZ)
        } catch (e: Exception) {
            Log.e("ViewModel", "Failed to parse quiz json", e)
        }
    }

    fun selectQuizOption(index: Int) {
        if (_isAnswerChecked.value) return
        _selectedOption.value = index
    }

    fun checkQuizAnswer() {
        val selected = _selectedOption.value ?: return
        if (_isAnswerChecked.value) return

        val current = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return

        _isAnswerChecked.value = true
        if (selected == current.correctIndex) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        val nextIndex = _currentQuizIndex.value + 1
        if (nextIndex < _quizQuestions.value.size) {
            _currentQuizIndex.value = nextIndex
            _selectedOption.value = null
            _isAnswerChecked.value = false
        } else {
            // Finished! Store scorecard in database
            _quizGameplayFinished.value = true
            val quiz = _playingQuizSet.value
            if (quiz != null) {
                viewModelScope.launch {
                    val finalScore = _quizScore.value
                    repository.updateQuizScore(quiz.copy(score = finalScore))
                }
            }
        }
    }

    fun resetQuizEngine() {
        _playingQuizSet.value = null
        _quizQuestions.value = emptyList()
        _currentQuizIndex.value = 0
        _selectedOption.value = null
        _isAnswerChecked.value = false
        _quizScore.value = 0
        _quizGameplayFinished.value = false
    }

    // Interactive Flashcards Deck Start
    fun startFlashcardDeck(set: FlashcardSet) {
        try {
            val list = mutableListOf<FlashCardItem>()
            val array = JSONArray(set.flashcardsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FlashCardItem(
                        front = obj.optString("front", "Term Front"),
                        back = obj.optString("back", "Definition Back")
                    )
                )
            }
            _activeCards.value = list
            _activeFlashcardSet.value = set
            _currentCardIndex.value = 0
            _isCardFlipped.value = false
            setTab(StudyTab.FLASHCARDS)
        } catch (e: Exception) {
            Log.e("ViewModel", "Failed to parse flashcard deck json", e)
        }
    }

    fun flipFlashcard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextFlashcard() {
        val nextIndex = _currentCardIndex.value + 1
        if (nextIndex < _activeCards.value.size) {
            _currentCardIndex.value = nextIndex
            _isCardFlipped.value = false
        }
    }

    fun prevFlashcard() {
        val prevIndex = _currentCardIndex.value - 1
        if (prevIndex >= 0) {
            _currentCardIndex.value = prevIndex
            _isCardFlipped.value = false
        }
    }

    fun resetFlashcardEngine() {
        _activeFlashcardSet.value = null
        _activeCards.value = emptyList()
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
    }

    // --- Study Tracker Progress ---
    fun markTopicStudied(topicName: String) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.insertStudiedTopic(
                StudiedTopic(
                    userEmail = email,
                    topicName = topicName,
                    isStudied = true
                )
            )
        }
    }

    fun toggleTopicStudiation(studiedTopic: StudiedTopic) {
        viewModelScope.launch {
            repository.updateStudiedTopic(studiedTopic.copy(isStudied = !studiedTopic.isStudied))
        }
    }

    fun deleteStudiedTopicItem(id: Int) {
        viewModelScope.launch {
            repository.deleteStudiedTopic(id)
        }
    }


    // --- Authentication Actions ---
    fun signUp(email: String, name: String, pass: String) {
        viewModelScope.launch {
            _signupError.value = null
            if (email.trim().isEmpty() || pass.trim().isEmpty() || name.trim().isEmpty()) {
                _signupError.value = "All fields are required."
                return@launch
            }
            val existing = repository.getUserByEmail(email.trim())
            if (existing != null) {
                _signupError.value = "An account with this email already exists."
                return@launch
            }
            // Hash password securely (no plain credentials)
            val passwordHash = hashPassword(pass.trim())
            val newUser = User(email = email.trim(), passwordHash = passwordHash, fullName = name.trim())
            repository.insertUser(newUser)

            // Auto seed 5 default customizable tracked topics for progress tracker
            listOf(
                "Introduction to Neural Networks 🧠",
                "World War II Chronologies 🗺️",
                "Solving Recursion & Dynamic Programming 💻",
                "Advanced Electrochemistry 🧪",
                "English Composition & Grammatic Rhythm 📚"
            ).forEach { topicName ->
                repository.insertStudiedTopic(
                    StudiedTopic(
                        userEmail = newUser.email,
                        topicName = topicName,
                        isStudied = false
                    )
                )
            }
            
            // Log in automatically
            currentUserEmail.value = newUser.email
            currentUserName.value = newUser.fullName
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (email.trim().isEmpty() || pass.trim().isEmpty()) {
                _loginError.value = "Email and password are required."
                return@launch
            }
            val user = repository.getUserByEmail(email.trim())
            if (user == null) {
                _loginError.value = "Invalid email or password."
                return@launch
            }
            val valHash = hashPassword(pass.trim())
            if (user.passwordHash != valHash) {
                _loginError.value = "Invalid email or password."
                return@launch
            }
            currentUserEmail.value = user.email
            currentUserName.value = user.fullName
        }
    }

    fun logout() {
        currentUserEmail.value = null
        currentUserName.value = null
        _currentTab.value = StudyTab.DASHBOARD
        resetQuizEngine()
        resetFlashcardEngine()
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // safe fallback
        }
    }

    // --- History Deletion Hooks ---
    fun deleteQuestionItem(id: Int) = viewModelScope.launch { repository.deleteQuestion(id) }
    fun deleteNoteSummaryItem(id: Int) = viewModelScope.launch { repository.deleteNoteSummary(id) }
    fun deleteQuizSetItem(id: Int) {
        if (_playingQuizSet.value?.id == id) {
            resetQuizEngine()
        }
        viewModelScope.launch { repository.deleteQuiz(id) }
    }
    fun deleteFlashcardItem(id: Int) {
        if (_activeFlashcardSet.value?.id == id) {
            resetFlashcardEngine()
        }
        viewModelScope.launch { repository.deleteFlashcardSet(id) }
    }
    fun deleteStudyPlanItem(id: Int) = viewModelScope.launch { repository.deleteStudyPlan(id) }
}

class StudyHelperViewModelFactory(
    private val application: Application,
    private val repository: StudyHelperRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyHelperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyHelperViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
