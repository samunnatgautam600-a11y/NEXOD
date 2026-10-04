package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.UiState
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.TOPIC_DETAIL)
    }

    val questions = uiState.quizQuestions
    val currentIndex = uiState.currentQuestionIndex.coerceIn(0, maxOf(0, questions.size - 1))
    val currentQuestion = questions.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Interactive Knowledge Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Topic: ${uiState.currentTopic?.query ?: "Overview"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.TOPIC_DETAIL) },
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Topic")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No quiz questions available for this topic.")
            }
            return@Scaffold
        }

        if (uiState.isQuizSubmitted) {
            // Quiz Results Celebration Screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    val percent = (uiState.quizScore * 100) / questions.size
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(if (percent >= 80) Color(0xFF10B981).copy(alpha = 0.2f) else NexoraViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (percent >= 80) Icons.Default.EmojiEvents else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (percent >= 80) Color(0xFF10B981) else NexoraViolet,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Text(
                                text = if (percent >= 80) "Exceptional Mastery!" else if (percent >= 60) "Great Progress!" else "Learning in Progress",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "You scored ${uiState.quizScore} out of ${questions.size} ($percent%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = NexoraCyan
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (percent >= 80) "Badge Earned: NEXUS SCHOLAR" else "Badge: EXPLORER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.retakeQuiz() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("retake_quiz_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Retake Quiz")
                                }

                                Button(
                                    onClick = { viewModel.navigateTo(Screen.TOPIC_DETAIL) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("back_to_topic_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Return to Topic")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Question Review & Citations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Question by question review
                items(questions.size) { idx ->
                    val q = questions[idx]
                    val userChoice = uiState.selectedAnswers[q.id]
                    val isCorrect = userChoice == q.correctIndex

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, if (isCorrect) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${idx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Text(
                                    text = if (isCorrect) "✓ Correct" else "✗ Incorrect",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 12.sp
                                )
                            }
                            Text(text = q.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = "Correct Answer: ${q.options.getOrNull(q.correctIndex) ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Source: ${q.sourceReference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = q.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else if (currentQuestion != null) {
            // Active Quiz Flow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Progress & Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NexoraCyan
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentQuestion.difficulty,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NexoraCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Question Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentQuestion.question,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Grounded from: ${currentQuestion.sourceReference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Options List
                    val selectedOption = uiState.selectedAnswers[currentQuestion.id]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = selectedOption == optIndex
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectQuizAnswer(currentQuestion.id, optIndex)
                                    }
                                    .testTag("quiz_option_${optIndex}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) NexoraCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NexoraCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) NexoraCyan else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + optIndex).toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuizQuestion() },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("prev_question_button")
                    ) {
                        Text("Previous")
                    }

                    if (currentIndex == questions.size - 1) {
                        Button(
                            onClick = { viewModel.submitQuiz() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraViolet),
                            modifier = Modifier.testTag("submit_quiz_button")
                        ) {
                            Text("Submit Quiz", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("next_question_button")
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}
