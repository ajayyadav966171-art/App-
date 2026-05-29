package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val fullName: String,
    val signupTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "studied_topics")
data class StudiedTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val topicName: String,
    val isStudied: Boolean = true,
    val dateStudied: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_questions")
data class StudyQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "note_summaries")
data class NoteSummary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val notes: String,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_sets")
data class QuizSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val topic: String,
    val questionsJson: String,
    val score: Int? = null, // Completed quiz score (out of 10)
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcard_sets")
data class FlashcardSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val topic: String,
    val flashcardsJson: String, // Array of Question-Answer pairs
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val examDate: String,
    val subjects: String,
    val scheduleJson: String, // Stringified schedule contents
    val timestamp: Long = System.currentTimeMillis()
)

