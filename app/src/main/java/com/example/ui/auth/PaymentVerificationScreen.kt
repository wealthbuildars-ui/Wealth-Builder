package com.example.ui.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentVerificationScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val adminSettings by authViewModel.adminSettings.collectAsState()
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var uploadInProgress by remember { mutableStateOf(false) }

    // Forms for profile completion step
    var formDisplayName by remember { mutableStateOf(userProfile.displayName) }
    var formSelectedPath by remember { mutableStateOf(if (userProfile.selectedPath == "All") "Affiliate" else userProfile.selectedPath) }
    var formMonthlyGoal by remember { mutableStateOf(userProfile.monthlyGoal.toInt().toString()) }

    // Launcher for selecting local payment proof (JPG, PNG, JPEG, PDF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            val path = uri.path ?: "payment_proof.png"
            selectedFileName = path.substringAfterLast("/").substringAfterLast(":")
            if (!selectedFileName.contains(".")) {
                selectedFileName += ".png"
            }
        }
    }

    // Dynamic timer ticker
    var remainingTimeStr by remember { mutableStateOf("24:00:00") }
    var isExpiredByTime by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.loadAdminSettings()
    }

    LaunchedEffect(userProfile.paymentSubmittedTime, userProfile.accountStatus, adminSettings.isTimerEnabled) {
        if (adminSettings.isTimerEnabled && userProfile.accountStatus == "Pending Verification" && userProfile.paymentSubmittedTime > 0L) {
            while (true) {
                val now = System.currentTimeMillis()
                val target = userProfile.paymentSubmittedTime + (24 * 60 * 60 * 1000L) // 24 hours
                val diff = target - now
                if (diff <= 0) {
                    remainingTimeStr = "00:00:00"
                    isExpiredByTime = true
                    authViewModel.checkUserSession()
                    break
                } else {
                    val hours = diff / (3600 * 1000)
                    val minutes = (diff % (3600 * 1000)) / (60 * 1000)
                    val seconds = (diff % (60 * 1000)) / 1000
                    remainingTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000)
            }
        }
    }

    // Determine current step based on profile details and accountStatus
    // 1: Registered (always completed)
    // 2: Complete Profile
    // 3: Pay registration fee & 4: Upload proof (combined payment state)
    // 5: Wait for verification
    val isProfileIncomplete = userProfile.displayName.isBlank() || 
            userProfile.displayName == "Wealth Builder Pioneer" || 
            userProfile.selectedPath == "All"

    val currentStep = when {
        userProfile.accountStatus == "Pending Verification" -> 5
        isProfileIncomplete -> 2
        else -> 3 // Step 3 & 4 (Payment/Upload form active)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Guided Onboarding", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = EmeraldGreenLight
                        )
                    ) 
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7FBF8))
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Step-by-Step Onboarding Progress Tracker
            OnboardingProgressTracker(currentStep = currentStep)

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 2: COMPLETE PROFILE
            if (currentStep == 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Step 2: Setup Your Business Profile ⚙️",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fulfill these settings to personalize your Wealth Builder trackers and dashboard blueprints.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = formDisplayName,
                            onValueChange = { formDisplayName = it },
                            label = { Text("Enter Custom Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGreenLight) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreenLight)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Primary Blueprint Pathway", fontWeight = FontWeight.Bold, color = EmeraldGreenLight, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val pathways = listOf(
                            "Affiliate" to "Affiliate Marketing Blueprints",
                            "Freelancing" to "Freelance Digital Business",
                            "Digital Products" to "E-Book & Software Blueprints"
                        )

                        pathways.forEach { (pathId, title) ->
                            val isSelected = formSelectedPath == pathId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { formSelectedPath = pathId },
                                border = BorderStroke(
                                    1.5.dp, 
                                    if (isSelected) EmeraldGreenLight else Color.LightGray.copy(alpha = 0.5f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) EmeraldGreenLight.copy(alpha = 0.05f) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { formSelectedPath = pathId },
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreenLight)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) EmeraldGreenLight else Color.DarkGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = formMonthlyGoal,
                            onValueChange = { formMonthlyGoal = it },
                            label = { Text("Monthly Saving / Earning Target (₦)") },
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldGreenLight) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreenLight)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (formDisplayName.isNotBlank() && formDisplayName != "Wealth Builder Pioneer" && formMonthlyGoal.isNotBlank()) {
                                    val goalVal = formMonthlyGoal.toDoubleOrNull() ?: 5000.0
                                    authViewModel.updateProfile(formDisplayName, formSelectedPath, goalVal)
                                } else {
                                    Toast.makeText(context, "Please configure a valid custom display name & monthly goal.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Profile & Advance ➔", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // STEPS 3 & 4: PAYMENT AND UPLOAD PROOF
            if (currentStep == 3) {
                // If the user's status is Rejected, display the rejection banner
                if (userProfile.accountStatus == "Rejected") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = "Rejected", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Payment Proof Rejected",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (userProfile.rejectionReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rejection Reason: ${userProfile.rejectionReason}",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Please double check your OPay transfer details and re-upload an accurate receipt.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (userProfile.accountStatus == "Expired") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Verification window expired",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your last verification took too long to audit. Please re-upload your OPay payment proof.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Step 3: Secure Membership Payment 💳",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreenLight)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To open premium modules and earn referral commissions, make a one-time activation payment of ₦${adminSettings.registrationFee.toInt()} below.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Account detail box with interactive copy helpers
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldGreenLight.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, EmeraldGreenLight.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Bank Name:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text(adminSettings.bankName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreenLight))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Account Number:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(adminSettings.accountNumber, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = RichGold))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(adminSettings.accountNumber))
                                                Toast.makeText(context, "Account Number copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EmeraldGreenLight, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Account Name:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text(adminSettings.accountName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Fee Amount:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text("₦${adminSettings.registrationFee.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = EmeraldGreenLight))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Step 4: Upload Bank Receipt / Proof 📄",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreenLight)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Take a screenshot of your transfer from your bank app and submit it here.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview of file if selected
                        if (selectedUri != null) {
                            Surface(
                                color = EmeraldGreenLight.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, EmeraldGreenLight.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = EmeraldGreenLight)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(selectedFileName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Selected and ready", color = EmeraldGreenLight, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = {
                                        selectedUri = null
                                        selectedFileName = ""
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch("image/*,application/pdf") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = Color.DarkGray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("select_file_button")
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    selectedUri = Uri.parse("content://simulated/receipt_trans_${System.currentTimeMillis()}.png")
                                    selectedFileName = "OPay_Transfer_Receipt_${System.currentTimeMillis() % 1000}.png"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RichGold.copy(alpha = 0.15f), contentColor = EmeraldGreenLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("use_template_button")
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use Template", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val uri = selectedUri
                                if (uri != null && selectedFileName.isNotEmpty()) {
                                    authViewModel.uploadProofAndSubmit(uri, selectedFileName)
                                }
                            },
                            enabled = selectedUri != null && !uploadInProgress,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_proof_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Payment Receipt", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // STEP 5: WAIT FOR ADMIN VERIFICATION
            if (currentStep == 5) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, RichGold.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step 5: Compliance Verification Queue ⌛",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldGreenLight),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We have received your payment proof file! Our audit compliance desk is reviewing the transfer records manually.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (adminSettings.isTimerEnabled) {
                            Text(
                                text = "ESTIMATED ACTIVATION TIMER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = RichGold,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = remainingTimeStr,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGreenLight,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = {
                                    val now = System.currentTimeMillis()
                                    val target = userProfile.paymentSubmittedTime + (24 * 60 * 60 * 1000L)
                                    val total = 24 * 60 * 60 * 1000L
                                    val remaining = (target - now).coerceIn(0L, total)
                                    (remaining.toFloat() / total.toFloat())
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = EmeraldGreenLight,
                                trackColor = EmeraldGreenLight.copy(alpha = 0.2f),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldGreenLight.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.OfflineBolt, contentDescription = null, tint = EmeraldGreenLight)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Flexible security review active. Your account has been securely queued.", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info badge card at bottom
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreenLight.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, EmeraldGreenLight.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldGreenLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your business profile & payments are processed under high-level SSL encryption protocols.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingProgressTracker(currentStep: Int) {
    val steps = listOf("Register", "Profile", "Payment", "Submit", "Verify")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val stepNum = index + 1
            val isActive = stepNum == currentStep
            val isCompleted = stepNum < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = when {
                                isCompleted -> EmeraldGreenLight
                                isActive -> RichGold
                                else -> Color.LightGray.copy(alpha = 0.6f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            text = stepNum.toString(),
                            color = if (isActive) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when {
                            isCompleted -> EmeraldGreenLight
                            isActive -> RichGold
                            else -> Color.Gray
                        },
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    ),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) EmeraldGreenLight else Color.LightGray.copy(alpha = 0.4f)
                        )
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}
