package com.example.ui.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FreelanceRateResult
import com.example.data.InterestYearData
import com.example.data.ProductEvaluationResult
import com.example.ui.WealthViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(
    viewModel: WealthViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Compound Interest, 1 = Freelance Rate, 2 = Product Score

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Headers
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Compounder", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Freelance Rate", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                icon = { Icon(Icons.Default.Work, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Product Score", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) }
            )
        }

        // Expanded Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            when (selectedTab) {
                0 -> CompoundInterestCalculator(viewModel)
                1 -> FreelanceRateCalculator(viewModel)
                2 -> DigitalProductEvaluator(viewModel)
            }
        }
    }
}

@Composable
fun CompoundInterestCalculator(viewModel: WealthViewModel) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Compound Wealth Engine",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calculate the potential of automatic passive index investing over time.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = viewModel.interestPrincipal,
                    onValueChange = { viewModel.interestPrincipal = it },
                    label = { Text("Initial Deposit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("interest_principal_input")
                )

                OutlinedTextField(
                    value = viewModel.interestMonthlyContribution,
                    onValueChange = { viewModel.interestMonthlyContribution = it },
                    label = { Text("Monthly Contribution ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("interest_monthly_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.interestRate,
                        onValueChange = { viewModel.interestRate = it },
                        label = { Text("Annual Return (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("interest_rate_input")
                    )

                    OutlinedTextField(
                        value = viewModel.interestYears,
                        onValueChange = { viewModel.interestYears = it },
                        label = { Text("Duration (Years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("interest_years_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.calculateInterest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("interest_calc_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Project Wealth Compound", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display results
        val results = viewModel.interestResult
        val finalYear = results.lastOrNull()
        if (finalYear != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Projected Wealth Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MetricRow(
                        label = "Total Contributions",
                        value = currencyFormatter.format(finalYear.contributions),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow(
                        label = "Compound Interest Earned",
                        value = currencyFormatter.format(finalYear.interestEarned),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    MetricRow(
                        label = "Total Future Balance",
                        value = currencyFormatter.format(finalYear.balance),
                        color = MaterialTheme.colorScheme.primary,
                        isLarge = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Micro annual growth table representation
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Growth Progression Milestones",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Pick milestone years: year 1, halfway, final year
                    val filterYears = listOf(1, viewModel.interestYears.toIntOrNull()?.div(2) ?: 5, viewModel.interestYears.toIntOrNull() ?: 10)
                    
                    filterYears.distinct().forEach { targetYear ->
                        val data = results.firstOrNull { it.year == targetYear }
                        if (data != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Year $targetYear", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }
                                Text(
                                    text = currencyFormatter.format(data.balance),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FreelanceRateCalculator(viewModel: WealthViewModel) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Freelance Rate Blueprint",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calculate the hourly rate you must charge to cover taxes, vacations, and expenses while hitting your net goal.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = viewModel.freelanceTargetNet,
                    onValueChange = { viewModel.freelanceTargetNet = it },
                    label = { Text("Target Net Monthly Income ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("rate_target_input")
                )

                OutlinedTextField(
                    value = viewModel.freelanceExpenses,
                    onValueChange = { viewModel.freelanceExpenses = it },
                    label = { Text("Monthly Business Expenses ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("rate_expenses_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.freelanceTaxRate,
                        onValueChange = { viewModel.freelanceTaxRate = it },
                        label = { Text("Tax Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rate_tax_input")
                    )

                    OutlinedTextField(
                        value = viewModel.freelanceBillableHours,
                        onValueChange = { viewModel.freelanceBillableHours = it },
                        label = { Text("Billable Hours/Week") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rate_hours_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.freelanceVacationWeeks,
                    onValueChange = { viewModel.freelanceVacationWeeks = it },
                    label = { Text("Annual Vacation (Weeks)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("rate_vacation_input")
                )

                Button(
                    onClick = { viewModel.calculateFreelanceRate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rate_calc_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PriceChange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculate Minimum Hourly Rate", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display results
        val result = viewModel.freelanceRateResult
        if (result != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your Target Hourly Rate",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currencyFormatter.format(result.hourlyRate) + "/hr",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "Minimum billing rate to sustain your desired lifestyle",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    MetricRow(
                        label = "Gross Annual Revenue Required",
                        value = currencyFormatter.format(result.annualGrossRevenueNeeded),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow(
                        label = "Estimated Income Tax",
                        value = currencyFormatter.format(result.annualTaxAmount),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow(
                        label = "Total Billable Hours/Year",
                        value = "${result.hoursPerYear.toInt()} hrs",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun DigitalProductEvaluator(viewModel: WealthViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Digital Product Idea Matrix",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Evaluate your digital ebook, templates, or courses against standard business validity indicators.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Criterion 1: Pain severity
                Text(
                    text = "1. Customer Pain Severity (${viewModel.prodPainSeverity.toInt()}/10)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "How urgently does the audience want to solve this? (0 = minor annoyance, 10 = high cost/time burden)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )
                Slider(
                    value = viewModel.prodPainSeverity,
                    onValueChange = { viewModel.prodPainSeverity = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.testTag("prod_pain_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Criterion 2: Market Size
                Text(
                    text = "2. Market Demand & Size (${viewModel.prodMarketSize.toInt()}/10)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Are there existing buyers willing to pay? (0 = completely unproven/tiny, 10 = large proven buying audience)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )
                Slider(
                    value = viewModel.prodMarketSize,
                    onValueChange = { viewModel.prodMarketSize = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.testTag("prod_market_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Criterion 3: Ease of production
                Text(
                    text = "3. Ease of Production (${viewModel.prodProductionEase.toInt()}/10)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Can you create an MVP quickly? (0 = requires advanced custom coding/months, 10 = simple PDF/Notion template in 24 hours)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )
                Slider(
                    value = viewModel.prodProductionEase,
                    onValueChange = { viewModel.prodProductionEase = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.testTag("prod_ease_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Criterion 4: Margin/Scalability
                Text(
                    text = "4. Margin & Delivery Scalability (${viewModel.prodMarginPotential.toInt()}/10)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Are sales automatically fulfilled? (0 = manual consulting/heavy support, 10 = 100% digital instant download)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )
                Slider(
                    value = viewModel.prodMarginPotential,
                    onValueChange = { viewModel.prodMarginPotential = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.testTag("prod_scalability_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.evaluateProduct() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("evaluate_calc_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Evaluate Idea Score", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display results
        val result = viewModel.productEvaluationResult
        if (result != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Venture Validation Analysis",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Idea Health Score:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${result.score.toInt()}/100",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (result.score >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "VERDICT:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.rating,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Metric Strengths:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    result.breakdown.forEach { (metric, valScore) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = metric, style = MaterialTheme.typography.bodySmall)
                                Text(text = "${valScore.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            LinearProgressIndicator(
                                progress = { valScore / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color, isLarge: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold) else MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
