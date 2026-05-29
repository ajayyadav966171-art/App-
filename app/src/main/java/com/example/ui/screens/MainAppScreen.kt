package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.FlashCardItem
import com.example.ui.QuizQuestion
import com.example.ui.StudyHelperViewModel
import com.example.ui.StudyTab
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: StudyHelperViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    if (currentUserEmail.isNullOrBlank()) {
        AuthScreen(viewModel = viewModel)
    } else {
        // Responsive design width check
        val configuration = LocalConfiguration.current
        val isWideScreen = configuration.screenWidthDp >= 600

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "StudyAI",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = currentUserName?.let { "Hi, $it 👋" } ?: "Your personal tutor",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        // Polished custom toggle bar matching w-10 h-10 pill in HTML
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { viewModel.toggleTheme() }
                                .testTag("theme_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.Settings else Icons.Default.Home,
                                contentDescription = "Switch Theme",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Logout Icon Action with rounded clip matching theme
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                                .clickable { viewModel.logout() }
                                .testTag("logout_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    BottomNavigationBar(
                        selectedTab = currentTab,
                        onTabSelected = {
                            // Reset sub-engines when changing tabs
                            viewModel.resetQuizEngine()
                            viewModel.resetFlashcardEngine()
                            viewModel.setTab(it)
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRailBar(
                        selectedTab = currentTab,
                        onTabSelected = {
                            viewModel.resetQuizEngine()
                            viewModel.resetFlashcardEngine()
                            viewModel.setTab(it)
                        }
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "TabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            StudyTab.DASHBOARD -> DashboardScreen(viewModel)
                            StudyTab.ASK -> AskScreen(viewModel)
                            StudyTab.SUMMARIZER -> SummarizerScreen(viewModel)
                            StudyTab.QUIZ -> QuizScreen(viewModel)
                            StudyTab.FLASHCARDS -> FlashcardScreen(viewModel)
                            StudyTab.PLANNER -> PlannerScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ------ BOTTOM NAVIGATION BAR (MOBILE) ------
@Composable
fun BottomNavigationBar(
    selectedTab: StudyTab,
    onTabSelected: (StudyTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == StudyTab.DASHBOARD,
            onClick = { onTabSelected(StudyTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = selectedTab == StudyTab.ASK,
            onClick = { onTabSelected(StudyTab.ASK) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Ask") },
            label = { Text("Ask", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_ask")
        )
        NavigationBarItem(
            selected = selectedTab == StudyTab.SUMMARIZER,
            onClick = { onTabSelected(StudyTab.SUMMARIZER) },
            icon = { Icon(Icons.Default.List, contentDescription = "Summarize") },
            label = { Text("Sum", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_summarizer")
        )
        NavigationBarItem(
            selected = selectedTab == StudyTab.QUIZ,
            onClick = { onTabSelected(StudyTab.QUIZ) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Quiz") },
            label = { Text("Quiz", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_quiz")
        )
        NavigationBarItem(
            selected = selectedTab == StudyTab.FLASHCARDS,
            onClick = { onTabSelected(StudyTab.FLASHCARDS) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Flashcards") },
            label = { Text("Cards", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_cards")
        )
        NavigationBarItem(
            selected = selectedTab == StudyTab.PLANNER,
            onClick = { onTabSelected(StudyTab.PLANNER) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Planner") },
            label = { Text("Plan", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_planner")
        )
    }
}

// ------ NAVIGATION RAIL (TABLET/DESKTOP) ------
@Composable
fun NavigationRailBar(
    selectedTab: StudyTab,
    onTabSelected: (StudyTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("navigation_rail")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = selectedTab == StudyTab.DASHBOARD,
            onClick = { onTabSelected(StudyTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationRailItem(
            selected = selectedTab == StudyTab.ASK,
            onClick = { onTabSelected(StudyTab.ASK) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Ask Questions") },
            label = { Text("Q&A") }
        )
        NavigationRailItem(
            selected = selectedTab == StudyTab.SUMMARIZER,
            onClick = { onTabSelected(StudyTab.SUMMARIZER) },
            icon = { Icon(Icons.Default.List, contentDescription = "Notes Summarizer") },
            label = { Text("Summarizer") }
        )
        NavigationRailItem(
            selected = selectedTab == StudyTab.QUIZ,
            onClick = { onTabSelected(StudyTab.QUIZ) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Quiz Generator") },
            label = { Text("Quizzes") }
        )
        NavigationRailItem(
            selected = selectedTab == StudyTab.FLASHCARDS,
            onClick = { onTabSelected(StudyTab.FLASHCARDS) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Flashcards") },
            label = { Text("Flashcards") }
        )
        NavigationRailItem(
            selected = selectedTab == StudyTab.PLANNER,
            onClick = { onTabSelected(StudyTab.PLANNER) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Study Planner") },
            label = { Text("Planner") }
        )
    }
}

// ====== SECTION 1: DASHBOARD OVERVIEW SCREEN ======
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: StudyHelperViewModel) {
    val questions by viewModel.questionsList.collectAsStateWithLifecycle()
    val summaries by viewModel.summariesList.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzesList.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcardsList.collectAsStateWithLifecycle()
    val plans by viewModel.plansList.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcomer Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Assistant",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ask anything about your studies...",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var localMiniQueryText by remember { mutableStateOf("") }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = localMiniQueryText,
                                onValueChange = { localMiniQueryText = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        if (localMiniQueryText.isEmpty()) {
                                            Text(
                                                text = "Explain Quantum Physics...",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = {
                                    if (localMiniQueryText.isNotBlank()) {
                                        viewModel.askInput.value = localMiniQueryText
                                        viewModel.setTab(StudyTab.ASK)
                                        viewModel.submitQuestion()
                                        localMiniQueryText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feature Selector Shortcuts (2x3 Grid Layout)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Core Study Engines",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardShortcutCard(
                        title = "Q&A Engine",
                        subtitle = "Instant answers",
                        icon = Icons.Default.Search,
                        badgeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(StudyTab.ASK) }
                    )
                    DashboardShortcutCard(
                        title = "Summarizer",
                        subtitle = "Cuts note fatigue",
                        icon = Icons.Default.List,
                        badgeColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(StudyTab.SUMMARIZER) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardShortcutCard(
                        title = "10 MCQ Quizzes",
                        subtitle = "Gamified revision",
                        icon = Icons.Default.Star,
                        badgeColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(StudyTab.QUIZ) }
                    )
                    DashboardShortcutCard(
                        title = "Flashcard deck",
                        subtitle = "Tactile review",
                        icon = Icons.Default.PlayArrow,
                        badgeColor = Color(0xFFEF9A9A),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(StudyTab.FLASHCARDS) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardShortcutCard(
                        title = "Study Planner",
                        subtitle = "Get weekly maps",
                        icon = Icons.Default.Settings,
                        badgeColor = Color(0xFFCE93D8),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setTab(StudyTab.PLANNER) }
                    )
                    Box(modifier = Modifier.weight(1f)) // Empty block for grid layout consistency
                }
            }
        }

        // Progress Tracking Metrics (Analytics Charts & Study checklist tracker)
        item {
            ProgressTrackerSection(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        // Study Plans Tab History
        if (plans.isNotEmpty()) {
            item {
                SectionHeader("Active Study Plans")
            }
            items(plans, key = { "plan_${it.id}" }) { plan ->
                StudyPlanHistoryCard(plan = plan, onDelete = { viewModel.deleteStudyPlanItem(plan.id) })
            }
        }

        // Quiz Set History Collection
        if (quizzes.isNotEmpty()) {
            item {
                SectionHeader("Generated Quizzes")
            }
            items(quizzes, key = { "quiz_${it.id}" }) { quiz ->
                QuizHistoryCard(
                    quizSet = quiz,
                    onPlay = { viewModel.startQuizGame(quiz) },
                    onDelete = { viewModel.deleteQuizSetItem(quiz.id) }
                )
            }
        }

        // Flashcards History Collection
        if (flashcards.isNotEmpty()) {
            item {
                SectionHeader("Flashcard Decks")
            }
            items(flashcards, key = { "card_${it.id}" }) { set ->
                FlashcardHistoryCard(
                    set = set,
                    onPlay = { viewModel.startFlashcardDeck(set) },
                    onDelete = { viewModel.deleteFlashcardItem(set.id) }
                )
            }
        }

        // Summary History Collection
        if (summaries.isNotEmpty()) {
            item {
                SectionHeader("Summarized Study Notes")
            }
            items(summaries, key = { "sum_${it.id}" }) { sum ->
                SummaryHistoryCard(
                    summary = sum,
                    onDelete = { viewModel.deleteNoteSummaryItem(sum.id) }
                )
            }
        }

        // QA History Collection
        if (questions.isNotEmpty()) {
            item {
                SectionHeader("Saved Q&A Answers")
            }
            items(questions, key = { "qa_${it.id}" }) { qa ->
                QuestionHistoryCard(
                    qa = qa,
                    onDelete = { viewModel.deleteQuestionItem(qa.id) }
                )
            }
        }

        // Welcome Placeholder empty layout
        if (questions.isEmpty() && summaries.isEmpty() && quizzes.isEmpty() && flashcards.isEmpty() && plans.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        modifier = Modifier
                            .size(56.dp)
                            .alpha(0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved study records yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Use the study helper tools to create notes, flashcards, plans or ask academic queries.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(134.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// ====== SECTION 2: ASK QUESTIONS (Q&A) ======
@Composable
fun AskScreen(viewModel: StudyHelperViewModel) {
    val askInput by viewModel.askInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.askLoading.collectAsStateWithLifecycle()
    val answer by viewModel.lastAnswer.collectAsStateWithLifecycle()
    val kbController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ask_root")
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ask Any Study Question 💡",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type any complex academic topic, formulas, historical events, or coding queries and get precise explanations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = askInput,
                    onValueChange = { viewModel.askInput.value = it },
                    placeholder = { Text("e.g. Briefly explain photosynthesis light-dependent stages") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .testTag("ask_question_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        kbController?.hide()
                        viewModel.submitQuestion()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ask_question_submit_btn"),
                    enabled = askInput.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Search AI Explanation")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Answer Results
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading) {
                // Pulse loading animation placeholder
                LoadingSkeleton()
            } else if (answer != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "AI Answer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        MarkdownText(text = answer!!)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Response will load here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}


// ====== SECTION 3: NOTES SUMMARIZER SCREEN ======
@Composable
fun SummarizerScreen(viewModel: StudyHelperViewModel) {
    val notesInput by viewModel.notesInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.summaryLoading.collectAsStateWithLifecycle()
    val summary by viewModel.lastSummary.collectAsStateWithLifecycle()
    val kbController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("summarizer_root")
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI Notes Summarizer 📝",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Paste your long chapters, lecture sheets, or textbook notes to get back a highly structured core bullet-point summary.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { viewModel.notesInput.value = it },
                    placeholder = { Text("Paste your long academic study text notes here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("summarizer_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.notesInput.value = "" },
                        enabled = notesInput.isNotEmpty() && !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Clear")
                    }
                    Button(
                        onClick = {
                            kbController?.hide()
                            viewModel.submitSummarize()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("summarizer_submit_btn"),
                        enabled = notesInput.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Summarize Text")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading) {
                LoadingSkeleton()
            } else if (summary != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Generated Notes Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        MarkdownText(text = summary!!)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Summarized notes display area.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}


// ====== SECTION 4: MCQ QUIZ SCREEN ======
@Composable
fun QuizScreen(viewModel: StudyHelperViewModel) {
    val topicInput by viewModel.quizTopicInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.quizLoading.collectAsStateWithLifecycle()
    val statusMessage by viewModel.quizStatusMessage.collectAsStateWithLifecycle()

    val playingQuizSet by viewModel.playingQuizSet.collectAsStateWithLifecycle()
    val quizQuestions by viewModel.quizQuestions.collectAsStateWithLifecycle()
    val currentQuestionIndex by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val selectedOption by viewModel.selectedOption.collectAsStateWithLifecycle()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsStateWithLifecycle()
    val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val gameplayFinished by viewModel.quizGameplayFinished.collectAsStateWithLifecycle()

    val kbController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quiz_root")
            .padding(16.dp)
    ) {
        if (playingQuizSet == null) {
            // Setup quiz block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "10 MCQ School Quiz Creator 🎯",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter any topic (e.g. \"Java Programming Exceptions\", \"Periodic Table alkaline metals\"). Gemini will generate exactly 10 fully interactive custom multiple choice questions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { viewModel.quizTopicInput.value = it },
                        placeholder = { Text("e.g. Calculus Derivatives or World War II Battles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_topic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            kbController?.hide()
                            viewModel.submitGenerateQuiz()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_generate_btn"),
                        enabled = topicInput.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Generate 10 MCQ Quiz")
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (statusMessage!!.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Helper notification info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Start compiled quizzes from the Home tab history panel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Once created, they remain locally cached permanently.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // INTERACTIVE QUIZ PLAYING ENGINE
            val totalQuestions = quizQuestions.size
            val currentQuestion = quizQuestions.getOrNull(currentQuestionIndex)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.resetQuizEngine() },
                    colors = ButtonDefaults.textButtonColors(),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exit Quiz")
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Score: $quizScore / $totalQuestions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!gameplayFinished && currentQuestion != null) {
                // Ongoing question block
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Progress Indicator Bar
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex.toFloat() + 1) / totalQuestions.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Column of options answers
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = selectedOption == optIndex
                            val optionLetter = when (optIndex) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                else -> "D"
                            }

                            // Dynamic colors states matching check submission
                            val containerColor = when {
                                isAnswerChecked && optIndex == currentQuestion.correctIndex -> MaterialTheme.colorScheme.primaryContainer
                                isAnswerChecked && isSelected && optIndex != currentQuestion.correctIndex -> MaterialTheme.colorScheme.errorContainer
                                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            val borderOutlineColor = when {
                                isAnswerChecked && optIndex == currentQuestion.correctIndex -> MaterialTheme.colorScheme.primary
                                isAnswerChecked && isSelected && optIndex != currentQuestion.correctIndex -> MaterialTheme.colorScheme.error
                                isSelected -> MaterialTheme.colorScheme.secondary
                                else -> Color.Transparent
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor,
                                border = if (borderOutlineColor != Color.Transparent) {
                                    androidx.compose.foundation.BorderStroke(2.dp, borderOutlineColor)
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_option_$optIndex")
                                    .clickable { viewModel.selectQuizOption(optIndex) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = optionLetter,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Explanatory summary text if checked
                    if (isAnswerChecked) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (selectedOption == currentQuestion.correctIndex) Icons.Default.Check else Icons.Default.Clear,
                                        contentDescription = "Result",
                                        tint = if (selectedOption == currentQuestion.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedOption == currentQuestion.correctIndex) "Correct Answer!" else "Incorrect!",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedOption == currentQuestion.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Button actions: Submit check or Next
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isAnswerChecked) {
                            Button(
                                onClick = { viewModel.checkQuizAnswer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_answer_btn"),
                                enabled = selectedOption != null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Check Answer")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.nextQuizQuestion() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("next_question_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (currentQuestionIndex + 1 < totalQuestions) "Next Question" else "Finish Quiz"
                                )
                            }
                        }
                    }
                }
            } else {
                // Completed game summary scoreboard page
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Quiz Completed",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Quiz Celebration! 🎉",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic: ${playingQuizSet!!.topic}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Total Scorecard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "$quizScore / $totalQuestions",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val percentage = (quizScore.toFloat() / totalQuestions.toFloat() * 100).toInt()
                    Text(
                        text = when {
                            percentage >= 90 -> "Outstanding! Master level knowledge."
                            percentage >= 70 -> "Well Done! Strong study foundations."
                            else -> "Keep Practicing! Review notes and try again."
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { viewModel.resetQuizEngine() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finish_quiz_card_exit"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Exit to Dashboard")
                    }
                }
            }
        }
    }
}


// ====== SECTION 5: FLASHCARDS SCREEN ======
@Composable
fun FlashcardScreen(viewModel: StudyHelperViewModel) {
    val topicInput by viewModel.flashcardsTopicInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.flashcardsLoading.collectAsStateWithLifecycle()
    val statusMessage by viewModel.flashcardStatusMessage.collectAsStateWithLifecycle()

    val activeSet by viewModel.activeFlashcardSet.collectAsStateWithLifecycle()
    val cards by viewModel.activeCards.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()

    val kbController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("flashcard_root")
            .padding(16.dp)
    ) {
        if (activeSet == null) {
            // Unselected setup page
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Flashcard Generator 🎟️",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter notes or high-yield topic and watch Gemini compiles structured question/answer flashcards for dynamic reviewing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { viewModel.flashcardsTopicInput.value = it },
                        placeholder = { Text("e.g. Organic Chemistry reactions or French vocabulary basics") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcards_topic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            kbController?.hide()
                            viewModel.submitGenerateFlashcards()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("flashcards_generate_btn"),
                        enabled = topicInput.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Generate Flashcards")
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (statusMessage!!.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation guide info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Launch generated card decks from the home dashboard history list.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Decks can be closed, flipped, and tracked offline any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // ACTIVE STUDY FLASHCARDS CAROUSEL
            val totalCards = cards.size
            val card = cards.getOrNull(currentIndex)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.resetFlashcardEngine() },
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Close Deck")
                }

                Text(
                    text = "Topic: ${activeSet!!.topic}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (card != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Header Tracker
                    Text(
                        text = "Card ${currentIndex + 1} of $totalCards",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Classic Flippable Card
                    val cardStateScale by animateFloatAsState(
                        targetValue = if (isFlipped) 0.99f else 1f,
                        animationSpec = spring(),
                        label = "CardScale"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .scale(cardStateScale)
                            .testTag("flashcard_body")
                            .clickable { viewModel.flipFlashcard() },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Side Label Banner
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFlipped) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = if (isFlipped) " BACK (ANSWER) " else " FRONT (TERM/QUESTION) ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                        color = if (isFlipped) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = if (isFlipped) card.back else card.front,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 28.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = if (isFlipped) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Flip",
                                    tint = if (isFlipped) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Tap Card to Flip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Prev-Next buttons setup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.prevFlashcard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("flashcard_prev_btn"),
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { viewModel.nextFlashcard() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("flashcard_next_btn"),
                            enabled = currentIndex + 1 < totalCards,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No cards found in this set.")
                }
            }
        }
    }
}


// ====== SECTION 6: STUDY PLANNER SCREEN ======
@Composable
fun PlannerScreen(viewModel: StudyHelperViewModel) {
    val dateInput by viewModel.planExamDateInput.collectAsStateWithLifecycle()
    val subjectsInput by viewModel.planSubjectsInput.collectAsStateWithLifecycle()
    val isLoading by viewModel.planLoading.collectAsStateWithLifecycle()
    val timetablePlan by viewModel.lastPlanText.collectAsStateWithLifecycle()
    val kbController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("planner_root")
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Smart AI Study Planner 🗓️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Supply target subject lists and your upcoming exam date, and watch Gemini drafts a customized week-by-week timeline calendar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { viewModel.planExamDateInput.value = it },
                    label = { Text("Exam Date / Timeline Period") },
                    placeholder = { Text("e.g. Dec 15th, 2026 or Next 3 Weeks") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_exam_date_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subjectsInput,
                    onValueChange = { viewModel.planSubjectsInput.value = it },
                    label = { Text("Subjects list & Focus Areas") },
                    placeholder = { Text("e.g. Physics Mechanics, Chemistry Redox, Math calculus integration") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_subjects_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        kbController?.hide()
                        viewModel.submitGeneratePlan()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("planner_submit_btn"),
                    enabled = dateInput.isNotBlank() && subjectsInput.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Draft Custom Study Plan")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading) {
                LoadingSkeleton()
            } else if (timetablePlan != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Draft Study Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        MarkdownText(text = timetablePlan!!)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Custom schedule timeline displays here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}


// ====== INLINE HISTORICAL HISTORY CARDS ======

@Composable
fun QuestionHistoryCard(qa: StudyQuestion, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val formattedTime = DateUtils.getRelativeTimeSpanString(qa.timestamp).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_qa_item"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved Q&A Rule",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = qa.question,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Asked $formattedTime",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Settings else Icons.Default.List, // Arrow like toggle placeholder
                            contentDescription = "Expand answerToggle"
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isExpanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MarkdownText(text = qa.answer)
            }
        }
    }
}

@Composable
fun SummaryHistoryCard(summary: NoteSummary, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val formattedTime = DateUtils.getRelativeTimeSpanString(summary.timestamp).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_summary_item"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Condensed Study Notes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (summary.notes.length > 40) summary.notes.take(40) + "..." else summary.notes,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Summarized $formattedTime",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Settings else Icons.Default.List,
                            contentDescription = "Expand notes"
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete summary",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isExpanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MarkdownText(text = summary.summary)
            }
        }
    }
}

@Composable
fun QuizHistoryCard(quizSet: QuizSet, onPlay: () -> Unit, onDelete: () -> Unit) {
    val formattedTime = DateUtils.getRelativeTimeSpanString(quizSet.timestamp).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_quiz_item"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "10 MCQ Revision Challenge",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = quizSet.topic,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Set: $formattedTime",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (quizSet.score != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = " Best Score: ${quizSet.score}/10 ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPlay,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("play_quiz_btn_${quizSet.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = if (quizSet.score != null) "Retake" else "Play", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete quiz",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardHistoryCard(set: FlashcardSet, onPlay: () -> Unit, onDelete: () -> Unit) {
    val formattedTime = DateUtils.getRelativeTimeSpanString(set.timestamp).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_flashcards_item"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "High-Yield Active Recall Deck",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE57373)
                )
                Text(
                    text = set.topic,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Compiled $formattedTime",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPlay,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("play_cards_btn_${set.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Review cards", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Review", fontSize = 11.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete deck",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun StudyPlanHistoryCard(plan: StudyPlan, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val formattedTime = DateUtils.getRelativeTimeSpanString(plan.timestamp).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_planner_item"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Study Plan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                    Text(
                        text = "Exam Date: ${plan.examDate}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Subjects: ${plan.subjects}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Drafted $formattedTime",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Settings else Icons.Default.List,
                            contentDescription = "Expand study planner"
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete plan",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isExpanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MarkdownText(text = plan.scheduleJson)
            }
        }
    }
}


// ====== LOADING PULSE SKELETON ANIMATIONS ======
@Composable
fun LoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }
        }

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
        }
    }
}
