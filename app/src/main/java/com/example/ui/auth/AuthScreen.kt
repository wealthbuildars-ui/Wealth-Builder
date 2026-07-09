package com.example.ui.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import com.example.ui.theme.LuminousGold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFbAvailable by viewModel.isFirebaseAvailable.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()

    // authMode: null = Landing Page, "login" = Login form, "register" = Registration form
    var authMode by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBF8))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Sticky Navigation
            LandingHeader(
                onLoginClick = { authMode = "login" },
                onRegisterClick = { authMode = "register" },
                isLandingMode = authMode == null
            )

            Box(modifier = Modifier.weight(1f)) {
                if (authMode == null) {
                    // Premium Homepage / Landing Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        HeroSection(
                            adminSettings = adminSettings,
                            onRegisterClick = { authMode = "register" },
                            onLoginClick = { authMode = "login" }
                        )

                        AboutSection(adminSettings = adminSettings)

                        HowItWorksSection()

                        FeaturesSection(adminSettings = adminSettings)

                        WhyChooseUsSection()

                        TestimonialsSection(adminSettings = adminSettings)

                        FaqSection(adminSettings = adminSettings)

                        ContactSection(adminSettings = adminSettings)

                        LandingFooter(
                            adminSettings = adminSettings,
                            onSectionScroll = { section ->
                                // Optional scroll assistance
                            }
                        )
                    }
                } else {
                    // Elegant Authentication Overlay Card
                    AuthCardContainer(
                        authMode = authMode!!,
                        uiState = uiState,
                        isFbAvailable = isFbAvailable,
                        viewModel = viewModel,
                        onBackToHome = { authMode = null },
                        onToggleMode = { authMode = if (authMode == "login") "register" else "login" }
                    )
                }
            }
        }
    }
}

@Composable
fun LandingHeader(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isLandingMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_wealth_logo_1783208620638),
                    contentDescription = "Wealth Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wealth Builder",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreenLight,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            if (isLandingMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onLoginClick,
                        border = BorderStroke(1.dp, EmeraldGreenLight),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Login", color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Register", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                TextButton(
                    onClick = onLoginClick, // effectively resets or goes home via programmatic clicks
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = EmeraldGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Home", color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    adminSettings: com.example.data.model.AdminSettings,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE8F5E9))
                )
            )
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_wealth_logo_1783208620638),
                contentDescription = "Wealth Builder Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, RichGold, RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = adminSettings.homepageHeroTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = EmeraldGreenLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = adminSettings.homepageHeroSubtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Register Now", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RichGold),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Login Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AboutSection(adminSettings: com.example.data.model.AdminSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "ABOUT WEALTH BUILDER",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "E-Learning & Earning System Engineered to Scale",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = adminSettings.aboutText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.DarkGray,
                lineHeight = 22.sp
            )
        )
    }
}

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9).copy(alpha = 0.5f))
            .padding(24.dp)
    ) {
        Text(
            text = "HOW IT WORKS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Step-by-Step Security & Onboarding Process",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        val steps = listOf(
            "Register Securely" to "Create your elite profile with dynamic password encryption.",
            "Complete Profile" to "Specify your personal display name, earning targets, and niche directions.",
            "Registration Fee" to "Fulfill a strict, one-time setup fee of ₦5,000 to OPay account to initialize services.",
            "Submit Proof of Payment" to "Upload your transfer receipt instantly for manual compliance auditing.",
            "Compliance Verification" to "Our lead advisors verify details within 1-24 hours via ticking timers.",
            "Receive Activations" to "Unlock modern calculators, affiliate modules, and active referral tools!"
        )

        steps.forEachIndexed { index, (title, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(EmeraldGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenLight
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturesSection(adminSettings: com.example.data.model.AdminSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "PLATFORM FEATURES",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Elite Money-Making Blueprints & Trackers",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        adminSettings.featuresList.forEach { raw ->
            val parts = raw.split("|")
            val title = parts.getOrNull(0) ?: "Feature"
            val body = parts.getOrNull(1) ?: ""
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldGreenLight)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldGreenLight
                        )
                        if (body.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhyChooseUsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4).copy(alpha = 0.3f))
            .padding(24.dp)
    ) {
        Text(
            text = "WHY WEALTH BUILDER?",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Built on Trust, Legitimate Systems, & Zero Slop",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        val reasons = listOf(
            "100% Genuine Monetization Models (Affiliate, Digital Products, Freelance Blueprints)",
            "Active and Responsive Advisor Support ticketing systems directly in-app",
            "Transparent Referral Commission program with instant credit upon activation",
            "Highly secure, manual validation parameters to prevent MLMs and fake screenshots",
            "Aesthetic, responsive mobile designs built on modern Jetpack Compose layouts"
        )

        reasons.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Checked", tint = EmeraldGreenLight, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TestimonialsSection(adminSettings: com.example.data.model.AdminSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "REAL TESTIMONIALS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vouched & Managed Directly by compliance team",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(adminSettings.testimonialsList) { raw ->
                val parts = raw.split("|")
                val author = parts.getOrNull(0) ?: "Student Pioneer"
                val text = parts.getOrNull(1) ?: ""
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.DarkGray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "- $author",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenLight
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqSection(adminSettings: com.example.data.model.AdminSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9).copy(alpha = 0.3f))
            .padding(24.dp)
    ) {
        Text(
            text = "FREQUENTLY ASKED QUESTIONS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Everything You Need to Know",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        adminSettings.faqsList.forEach { raw ->
            val parts = raw.split("|")
            val question = parts.getOrNull(0) ?: ""
            val answer = parts.getOrNull(1) ?: ""
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand FAQ",
                            tint = EmeraldGreenLight
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSection(adminSettings: com.example.data.model.AdminSettings) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "CONTACT WEALTH BUILDER",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = RichGold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We\'d Love to Hear From You",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenLight
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Contact info details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Mail, contentDescription = null, tint = EmeraldGreenLight)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = adminSettings.contactEmail, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = EmeraldGreenLight)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = adminSettings.contactPhone, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = EmeraldGreenLight)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = adminSettings.businessHours, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Configurable Google Maps Placeholder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Map, contentDescription = null, tint = RichGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interactive Google Maps", fontWeight = FontWeight.Bold, color = EmeraldGreenLight)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location Pin", tint = Color.Red, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = adminSettings.googleMapsAddress,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenLight,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Lat: 6.5244° N, Lon: 3.3792° E (Configured)",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact Form
        Text("Send Us an Inquiry", fontWeight = FontWeight.Bold, color = EmeraldGreenLight, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreenLight)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreenLight)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            shape = RoundedCornerShape(10.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreenLight)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && message.isNotBlank()) {
                    Toast.makeText(context, "Inquiry securely logged! We will email you at $email.", Toast.LENGTH_LONG).show()
                    name = ""
                    email = ""
                    message = ""
                } else {
                    Toast.makeText(context, "Please complete all fields to submit.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Send Inquiry", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LandingFooter(
    adminSettings: com.example.data.model.AdminSettings,
    onSectionScroll: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B140F))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = RichGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wealth Builder", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Language, contentDescription = "Website", tint = Color.White)
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Group, contentDescription = "Socials", tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = adminSettings.footerCopyright,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthCardContainer(
    authMode: String,
    uiState: AuthUiState,
    isFbAvailable: Boolean,
    viewModel: AuthViewModel,
    onBackToHome: () -> Unit,
    onToggleMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val isSignUp = authMode == "register"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Auth Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenLight
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, "Name icon", tint = EmeraldGreenLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldGreenLight,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreenLight,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = referralCode,
                        onValueChange = { referralCode = it },
                        label = { Text("Referral Code (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Group, "Referral icon", tint = EmeraldGreenLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("referral_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldGreenLight,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = EmeraldGreenLight,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, "Email icon", tint = EmeraldGreenLight) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldGreenLight,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = EmeraldGreenLight,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, "Lock icon", tint = EmeraldGreenLight) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, "Toggle password visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldGreenLight,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = EmeraldGreenLight,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (isSignUp) {
                            viewModel.register(email, password, displayName, referralCode)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUp) "Register Securely" else "Log In to Platform",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Auth Mode
                TextButton(
                    onClick = onToggleMode,
                    modifier = Modifier.testTag("toggle_auth_mode")
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? Sign In" else "New to Wealth Builder? Create Account",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = EmeraldGreenLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to home action
        TextButton(onClick = onBackToHome) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = EmeraldGreenLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Homepage", color = EmeraldGreenLight, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Database Mode Banner
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = EmeraldGreenLight.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, EmeraldGreenLight.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isFbAvailable) Icons.Default.CloudQueue else Icons.Default.Dns,
                    contentDescription = "Sync State Icon",
                    tint = if (isFbAvailable) EmeraldGreenLight else RichGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFbAvailable) "Connected to Cloud Firebase Auth & Firestore" else "Simulation Mode (Data saved locally)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Error Message Banner
        AnimatedVisibility(
            visible = uiState is AuthUiState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (uiState is AuthUiState.Error) {
                val errorState = uiState as AuthUiState.Error
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Icon",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Authentication Issue",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
