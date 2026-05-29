package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyHelperDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // --- Studied Topics Progress Tracker ---
    @Query("SELECT * FROM studied_topics WHERE userEmail = :userEmail ORDER BY dateStudied DESC")
    fun getStudiedTopicsByUser(userEmail: String): Flow<List<StudiedTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudiedTopic(topic: StudiedTopic)

    @Update
    suspend fun updateStudiedTopic(topic: StudiedTopic)

    @Query("DELETE FROM studied_topics WHERE id = :id")
    suspend fun deleteStudiedTopicById(id: Int)

    // --- Study Questions ---
    @Query("SELECT * FROM study_questions WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getQuestionsByUser(userEmail: String): Flow<List<StudyQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: StudyQuestion)

    @Query("DELETE FROM study_questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Int)

    // --- Note Summaries ---
    @Query("SELECT * FROM note_summaries WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getNoteSummariesByUser(userEmail: String): Flow<List<NoteSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteSummary(summary: NoteSummary)

    @Query("DELETE FROM note_summaries WHERE id = :id")
    suspend fun deleteNoteSummaryById(id: Int)

    // --- Quiz Sets ---
    @Query("SELECT * FROM quiz_sets WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getQuizzesByUser(userEmail: String): Flow<List<QuizSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizSet): Long

    @Update
    suspend fun updateQuiz(quiz: QuizSet)

    @Query("DELETE FROM quiz_sets WHERE id = :id")
    suspend fun deleteQuizById(id: Int)

    // --- Flashcard Sets ---
    @Query("SELECT * FROM flashcard_sets WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getFlashcardSetsByUser(userEmail: String): Flow<List<FlashcardSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcardSet(flashcardSet: FlashcardSet): Long

    @Query("DELETE FROM flashcard_sets WHERE id = :id")
    suspend fun deleteFlashcardSetById(id: Int)

    // --- Study Plans ---
    @Query("SELECT * FROM study_plans WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getStudyPlansByUser(userEmail: String): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlan)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deleteStudyPlanById(id: Int)
}

