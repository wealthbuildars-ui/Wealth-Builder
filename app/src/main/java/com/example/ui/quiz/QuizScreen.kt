package com.example.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.ui.WealthViewModel
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.MintGreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    viewModel: WealthViewModel,
    onAwardBadge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeQuestions = viewModel.activeQuizQuestions
    val currentIndex = viewModel.currentQuestionIndex
    val isCompleted = viewModel.isQuizCompleted

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (activeQuestions.isEmpty()) {
            // Welcome / Initial state
            QuizWelcomeView(onStartQuiz = { viewModel.startNewQuiz() })
        } else if (isCompleted) {
            // Results View
            QuizResultsView(
                score = viewModel.quizScore,
                total = activeQuestions.size,
                onRestart = { viewModel.startNewQuiz() },
                onAwardBadge = onAwardBadge
            )
        } else {
            // Active Question
            val currentQuestion = activeQuestions[currentIndex]
            ActiveQuizView(
                question = currentQuestion,
                currentIndex = currentIndex,
                totalQuestions = activeQuestions.size,
                selectedOption = viewModel.selectedOptionIndex,
                isSubmitted = viewModel.isAnswerSubmitted,
                onSelectOption = { viewModel.selectQuizOption(it) },
                onSubmit = { viewModel.submitQuizAnswer() },
                onNext = { viewModel.advanceQuizQuestion() }
            )
        }
    }
}

@Composable
fun QuizWelcomeView(onStartQuiz: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("quiz_welcome_card")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wealth & Earning Assessment",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Test your legitimate business knowledge in affiliate networks, freelance pricing models, digital templates, and compound growth compounding. Pass with 80%+ to unlock the 'Financial Strategist' credential!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_quiz_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Evaluation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun ActiveQuizView(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    isSubmitted: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_quiz_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUESTION ${currentIndex + 1} OF $totalQuestions",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "${(progress * 100).toInt()}% Done",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )

        // Question Statement
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                ),
                modifier = Modifier.padding(20.dp)
            )
        }

        // Answers options list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrectAnswer = index == question.correctAnswerIndex

                val cardColor = when {
                    isSubmitted && isCorrectAnswer -> MaterialTheme.colorScheme.primaryContainer
                    isSubmitted && isSelected && !isCorrectAnswer -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val border = when {
                    isSubmitted && isCorrectAnswer -> BorderStroke(2.dp, EmeraldGreenLight)
                    isSubmitted && isSelected && !isCorrectAnswer -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitted) { onSelectOption(index) }
                        .testTag("option_card_$index"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = border
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected || (isSubmitted && isCorrectAnswer)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A'.code + index).toChar().toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected || (isSubmitted && isCorrectAnswer)) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )

                        if (isSubmitted) {
                            if (isCorrectAnswer) {
                                Icon(Icons.Default.CheckCircle, "Correct Answer", tint = EmeraldGreenLight)
                            } else if (isSelected) {
                                Icon(Icons.Default.Cancel, "Incorrect Answer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Explanations Card (Visible when submitted)
        AnimatedVisibility(
            visible = isSubmitted,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ANALYSIS BLUEPRINT:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp)
                        )
                    }
                }
            }
        }

        // Action control button
        Button(
            onClick = {
                if (!isSubmitted) {
                    onSubmit()
                } else {
                    onNext()
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("action_quiz_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            val label = if (!isSubmitted) "Verify Strategic Selection" else {
                if (currentIndex + 1 == totalQuestions) "See My Assessment Outcome" else "Advance to Next Question"
            }
            Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun QuizResultsView(
    score: Int,
    total: Int,
    onRestart: () -> Unit,
    onAwardBadge: (String) -> Unit
) {
    val passed = score >= 4
    val percent = (score.toFloat() / total.toFloat() * 100).toInt()

    // Award badge if passed
    LaunchedEffect(passed) {
        if (passed) {
            onAwardBadge("Financial Strategist")
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("quiz_results_card")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.MilitaryTech else Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        color = if (passed) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (passed) "Evaluation Passed!" else "Evaluation Incomplete",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$percent% SCORE ($score OF $total QUESTIONS CORRECT)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (passed) {
                    "Outstanding. Your mastery over sustainable money pathways matches professional digital builders. You have unlocked the 'Financial Strategist' credential on your profile!"
                } else {
                    "Not quite there. Review the blueprints on Affiliate Marketing, Freelance pricing, and Compound Growth, and try again when you feel ready."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("restart_quiz_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (passed) "Test Knowledge Again" else "Re-evaluate Knowledge",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
