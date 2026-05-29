package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyHelperRepository(private val dao: StudyHelperDao) {

    // --- Authenticators and Helpers ---
    suspend fun getUserByEmail(email: String): User? = dao.getUserByEmail(email)
    suspend fun insertUser(user: User) = dao.insertUser(user)

    // --- Dynamic User-Specific Flow Collectors ---
    fun getQuestions(userEmail: String): Flow<List<StudyQuestion>> = dao.getQuestionsByUser(userEmail)
    fun getNoteSummaries(userEmail: String): Flow<List<NoteSummary>> = dao.getNoteSummariesByUser(userEmail)
    fun getQuizzes(userEmail: String): Flow<List<QuizSet>> = dao.getQuizzesByUser(userEmail)
    fun getFlashcardSets(userEmail: String): Flow<List<FlashcardSet>> = dao.getFlashcardSetsByUser(userEmail)
    fun getStudyPlans(userEmail: String): Flow<List<StudyPlan>> = dao.getStudyPlansByUser(userEmail)
    fun getStudiedTopics(userEmail: String): Flow<List<StudiedTopic>> = dao.getStudiedTopicsByUser(userEmail)

    private val modelName = "gemini-3.5-flash"

    // --- Database Writers & Progress Mutators ---
    suspend fun deleteQuestion(id: Int) = dao.deleteQuestionById(id)
    suspend fun deleteNoteSummary(id: Int) = dao.deleteNoteSummaryById(id)
    suspend fun deleteQuiz(id: Int) = dao.deleteQuizById(id)
    suspend fun updateQuizScore(quizSet: QuizSet) = dao.updateQuiz(quizSet)
    suspend fun deleteFlashcardSet(id: Int) = dao.deleteFlashcardSetById(id)
    suspend fun deleteStudyPlan(id: Int) = dao.deleteStudyPlanById(id)

    suspend fun insertStudiedTopic(topic: StudiedTopic) = dao.insertStudiedTopic(topic)
    suspend fun updateStudiedTopic(topic: StudiedTopic) = dao.updateStudiedTopic(topic)
    suspend fun deleteStudiedTopic(id: Int) = dao.deleteStudiedTopicById(id)

    // --- Gemini API Callers ---

    // 1. Q&A
    suspend fun askQuestion(userEmail: String, questionText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Please configure your GEMINI_API_KEY secure secret in AI Studio's Secrets panel."
        }

        val prompt = "Analyze and explain the following study question or topic in an exceptionally clear, friendly, and structured layout. Use clean markdown styling with bullet points, bold sections, and short paragraphs suitable for study notes.\n\nQuestion / Topic:\n$questionText"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5)
        )

        return try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    // Cache to DB
                    dao.insertQuestion(StudyQuestion(userEmail = userEmail, question = questionText, answer = text))
                    text
                } else {
                    "Error: The model returned an empty content candidate."
                }
            } else {
                "Error Call Failed: ${response.code()} - ${response.errorBody()?.string()}"
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in askQuestion", e)
            "Error Connection Failure: ${e.message}"
        }
    }

    // 2. Note Summarizer
    suspend fun summarizeNotes(userEmail: String, notesText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Please configure your GEMINI_API_KEY secure secret in AI Studio's Secrets panel."
        }

        val prompt = "Create a structured, highly informative summary of the following study notes. Make it logical, digestible, and concise. Outline the major takeaways, core terms/definitions, and key facts in separate sections using clean markdown headers and bullet points.\n\nOriginal Notes:\n$notesText"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.3)
        )

        return try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    // Cache to DB
                    dao.insertNoteSummary(NoteSummary(userEmail = userEmail, notes = notesText, summary = text))
                    text
                } else {
                    "Error: Empty response candidate from AI model."
                }
            } else {
                "Error: Network failed with code ${response.code()}."
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in summarizeNotes", e)
            "Error: ${e.message}"
        }
    }

    // 3. Quiz Generator
    suspend fun generateQuiz(userEmail: String, topic: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Secrets missing. Provide GEMINI_API_KEY in AI Studio's Secrets panel."
        }

        val prompt = "Generate a multiple-choice school quiz on the topic of \"$topic\". It MUST consist of exactly 10 high-quality multiple choice questions. Each question must have exactly 4 choices (index 0 to 3) with exactly one correction index.\n" +
                "You MUST reply with a raw JSON string containing a flat JSON array ONLY. DO NOT wrap with markdown formatting like ```json or any other text. Output must start with '[' and end with ']'.\n\n" +
                "Format required:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"The question text?\",\n" +
                "    \"options\": [\"Choice A\", \"Choice B\", \"Choice C\", \"Choice D\"],\n" +
                "    \"correctIndex\": 1,\n" +
                "    \"explanation\": \"A short helpful explanation of why Option 1 is the correct choice.\"\n" +
                "  }\n" +
                "]"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.6
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            if (response.isSuccessful) {
                val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanJson = cleanJsonArrayText(rawText)
                if (validateJsonArray(cleanJson)) {
                    // Store in local DB
                    dao.insertQuiz(QuizSet(userEmail = userEmail, topic = topic, questionsJson = cleanJson))
                    "SUCCESS"
                } else {
                    "Error: Failed to parse generated content into valid JSON. RAW: \n$rawText"
                }
            } else {
                "Error: Call failed with HTTP ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in generateQuiz", e)
            "Error: ${e.message}"
        }
    }

    // 4. Flashcard Generator
    suspend fun generateFlashcards(userEmail: String, topic: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: Secrets missing."
        }

        val prompt = "Generate a set of 8 to 12 interactive educational study flashcards from the topic/notes of \"$topic\". Each flashcard contains a terms/questions on the card's front side, and dynamic descriptions on the back side.\n" +
                "You MUST respond ONLY with a raw JSON flat array (no markdown backticks, no comments, no header text). Output starts with '[' and ends with ']'.\n\n" +
                "Format required:\n" +
                "[\n" +
                "  {\n" +
                "    \"front\": \"The term, concept, or question?\",\n" +
                "    \"back\": \"Standard, crisp explanatory answer or definition.\"\n" +
                "  }\n" +
                "]"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.5
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            if (response.isSuccessful) {
                val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanJson = cleanJsonArrayText(rawText)
                if (validateJsonArray(cleanJson)) {
                    dao.insertFlashcardSet(FlashcardSet(userEmail = userEmail, topic = topic, flashcardsJson = cleanJson))
                    "SUCCESS"
                } else {
                    "Error: Invalid JSON structure generated by AI. RAW: $rawText"
                }
            } else {
                "Error: HTTP ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in generateFlashcards", e)
            "Error: ${e.message}"
        }
    }

    // 5. Study Planner
    suspend fun generateStudyPlan(userEmail: String, examDate: String, subjectsText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Error: API Key missing in AI Studio secrets panel."
        }

        val prompt = "Develop an exceptionally visual, structured, week-by-week study schedule leading up to the target exam date of \"$examDate\". The plan must allocate proportional time and prioritize tasks based on the following subject areas: $subjectsText. Include specific daily study routines, rest guidelines, test-day practices, and mental wellness recommendations. Organize it using gorgeous markdown headers (e.g. #, ##) with bullet lists."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5)
        )

        return try {
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    // Cache to DB
                    dao.insertStudyPlan(StudyPlan(userEmail = userEmail, examDate = examDate, subjects = subjectsText, scheduleJson = text))
                    text
                } else {
                    "Error: The model returned empty response plan."
                }
            } else {
                "Error Code: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in generateStudyPlan", e)
            "Error: ${e.message}"
        }
    }

    // --- JSON Sanitizer Helpers ---
    private fun cleanJsonArrayText(raw: String): String {
        val trimmed = raw.trim()
        val startIndex = trimmed.indexOf('[')
        val endIndex = trimmed.lastIndexOf(']')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            trimmed.substring(startIndex, endIndex + 1)
        } else {
            trimmed
        }
    }

    private fun validateJsonArray(jsonStr: String): Boolean {
        try {
            JSONArray(jsonStr)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
