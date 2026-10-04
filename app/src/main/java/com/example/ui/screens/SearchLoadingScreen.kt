package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SearchProgressStep
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.UiState
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraViolet

@Composable
fun SearchLoadingScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val steps = listOf(
        Pair("1. Live Web Search", "Querying real-time web index, journals & databases"),
        Pair("2. Ranking & Authority", "Filtering .gov, .edu, scientific papers & official documentation"),
        Pair("3. AI Knowledge Synthesis", "Generating clear explanations, key points & factual citations"),
        Pair("4. Grounded Quiz Formulation", "Building interactive 5-question test strictly from source facts")
    )

    val currentStepIndex = when (uiState.progressStep) {
        SearchProgressStep.SEARCHING_WEB -> 0
        SearchProgressStep.ANALYZING_DOMAINS -> 1
        SearchProgressStep.SYNTHESIZING_AI -> 2
        SearchProgressStep.GENERATING_QUIZ, SearchProgressStep.COMPLETED -> 3
        else -> 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Pulsing Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(NexoraCyan.copy(alpha = pulseAlpha * 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Synthesizing",
                        tint = NexoraCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Synthesizing Knowledge",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${uiState.searchQuery}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NexoraCyan,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                // Pipeline steps
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    steps.forEachIndexed { index, (stepTitle, stepDesc) ->
                        val isFinished = index < currentStepIndex
                        val isCurrent = index == currentStepIndex
                        val isPending = index > currentStepIndex

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isFinished -> Color(0xFF10B981)
                                            isCurrent -> NexoraCyan
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFinished) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (isCurrent) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = stepTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stepDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                if (uiState.progressDetail.isNotBlank()) {
                    Text(
                        text = uiState.progressDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    modifier = Modifier.testTag("cancel_search_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel Search")
                }
            }
        }
    }
}
