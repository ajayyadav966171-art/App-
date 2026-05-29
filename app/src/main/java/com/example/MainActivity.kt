package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.StudyHelperRepository
import com.example.ui.StudyHelperViewModel
import com.example.ui.StudyHelperViewModelFactory
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Build the persistent local Room database state
        val database = AppDatabase.getDatabase(this)
        val repository = StudyHelperRepository(database.studyHelperDao())
        val factory = StudyHelperViewModelFactory(application, repository)
        
        // Use standard non-reflective Android viewModels property delegate
        val viewModel: StudyHelperViewModel by viewModels { factory }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(
                darkTheme = isDarkMode,
                dynamicColor = false // Forces our highly curated dark slate academic visual branding colors
            ) {
                MainAppScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
