package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.QuizSet
import com.example.data.StudiedTopic
import com.example.ui.StudyHelperViewModel

@Composable
fun ProgressTrackerSection(
    viewModel: StudyHelperViewModel,
    modifier: Modifier = Modifier
) {
    val topics by viewModel.studiedTopicsList.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzesList.collectAsStateWithLifecycle()

    val completedQuizzes = remember(quizzes) {
        quizzes.filter { it.score != null }.sortedBy { it.timestamp }
    }

    val totalTopics = topics.size
    val studiedTopics = topics.count { it.isStudied }
    val progressPercent = if (totalTopics > 0) studiedTopics.toFloat() / totalTopics else 0f

    val keyboardController = LocalSoftwareKeyboardController.current
    var newTopicText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_tracker_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. OVERALL PROGRESS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Course Completion Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$studiedTopics of $totalTopics goals mastered",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Progress Percentage Text
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fancy Progress Bar
                val animatedProgress by animateFloatAsState(
                    targetValue = progressPercent,
                    label = "ProgressBarAnimation"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (animatedProgress > 0f) animatedProgress else 0.01f)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Friendly feedback
                Text(
                    text = getProgressFeedback(progressPercent),
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 2. INSTANT TOPIC MUTATOR (STUDY GOALS LIST) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Study Subject Goals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Goal Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTopicText,
                        onValueChange = { newTopicText = it },
                        placeholder = { Text("Add study goal (e.g. Astrophysics)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_topic_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newTopicText.isNotBlank()) {
                                viewModel.markTopicStudied(newTopicText.trim())
                                newTopicText = ""
                                keyboardController?.hide()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    IconButton(
                        onClick = {
                            if (newTopicText.isNotBlank()) {
                                viewModel.markTopicStudied(newTopicText.trim())
                                newTopicText = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("add_topic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Goal",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Topic Checklists
                if (topics.isEmpty()) {
                    Text(
                        text = "No custom study goals declared. Type above to add subjects!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    topics.forEach { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleTopicStudiation(topic) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = topic.isStudied,
                                onCheckedChange = { viewModel.toggleTopicStudiation(topic) },
                                modifier = Modifier.testTag("topic_checkbox_${topic.id}")
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = topic.topicName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (topic.isStudied) FontWeight.Normal else FontWeight.Medium
                                ),
                                textDecoration = if (topic.isStudied) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (topic.isStudied) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { viewModel.deleteStudiedTopicItem(topic.id) },
                                modifier = Modifier.testTag("delete_topic_${topic.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete topic goal",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. QUIZ HISTORIC SCORE PERFORMANCE OVER TIME (CUSTOM VECTOR CANVAS GRAPH) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quiz Scores progression curves (scale 0-10)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (completedQuizzes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Take academic quizzes to plot performance metrics!",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    QuizPerformanceCanvasChart(quizzesList = completedQuizzes)
                }
            }
        }
    }
}

@Composable
fun QuizPerformanceCanvasChart(
    quizzesList: List<QuizSet>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val paddingLeft = 36.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom

            // Draw horizontal Grid lines (representing score points 0, 2, 4, 6, 8, 10)
            val verticalStep = chartHeight / 5
            for (i in 0..5) {
                val y = paddingTop + i * verticalStep
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw graph points
            val totalPoints = quizzesList.size
            if (totalPoints > 0) {
                val path = Path()
                val points = mutableListOf<Offset>()

                for (idx in quizzesList.indices) {
                    val quiz = quizzesList[idx]
                    val score = quiz.score?.toFloat() ?: 0f

                    // Calculate Coordinates
                    val x = if (totalPoints == 1) {
                        paddingLeft + chartWidth / 2
                    } else {
                        paddingLeft + (idx.toFloat() / (totalPoints - 1)) * chartWidth
                    }
                    val scoreRatio = score / 10f
                    val y = paddingTop + chartHeight * (1f - scoreRatio)

                    val pointOffset = Offset(x, y)
                    points.add(pointOffset)

                    if (idx == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw solid connecting spline line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Fill gradient beneath curve
                if (totalPoints > 1 && points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(points.last().x, paddingTop + chartHeight)
                        lineTo(points.first().x, paddingTop + chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                }

                // Draw circles at data points
                points.forEachIndexed { i, offset ->
                    drawCircle(
                        color = secondaryColor,
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 2.5.dp.toPx(),
                        center = offset
                    )
                }
            }
        }

        // Horizontal Dates list row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp, top = 4.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val limit = quizzesList.size
            if (limit > 0) {
                val datesSubset = when {
                    limit <= 3 -> quizzesList
                    else -> listOf(quizzesList.first(), quizzesList[limit / 2], quizzesList.last())
                }

                datesSubset.forEachIndexed { idx, quiz ->
                    val dateFormatted = DateFormat.format("MMM dd", quiz.timestamp).toString()
                    Text(
                        text = "${quiz.topic.take(8)}.. ($dateFormatted)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = onSurfaceVariantColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = if (idx == 0) TextAlign.Start else if (idx == datesSubset.lastIndex) TextAlign.End else TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun getProgressFeedback(progress: Float): String {
    return when {
        progress <= 0f -> "Start compiling topics to trigger your interactive progress meters! Add some subjects and mark them complete."
        progress < 0.40f -> "Formidable start! Slow steady progression builds heavy retention. Complete some more topics to bump your percent. 💪"
        progress < 0.70f -> "Spectacular progression study! You've compiled intermediate knowledge. Let's study more parameters! 🚀"
        progress < 0.99f -> "Excellent score! You are almost fully prepared for upcoming examination sets! Final rush! 🧠"
        else -> "Perfect mastery! 🏆 All targeted topics studies are completed. Take quizzes to verify retention curves!"
    }
}
