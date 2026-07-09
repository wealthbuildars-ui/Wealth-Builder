package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.AdCampaign
import com.example.data.model.AdPackage
import com.example.data.model.UserProfile
import com.example.ui.WealthViewModel
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdCampaignManagerSubTab(
    userProfile: UserProfile,
    wealthViewModel: WealthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val campaigns by wealthViewModel.adCampaigns.collectAsState()
    val packages by wealthViewModel.adPackages.collectAsState()
    val analyticsList by wealthViewModel.adAnalytics.collectAsState()

    var showCreateCampaignDialog by remember { mutableStateOf(false) }
    var showPaymentInstructionsCampaignId by remember { mutableStateOf<String?>(null) }
    var showPaymentConfirmDialogCampaign by remember { mutableStateOf<AdCampaign?>(null) }

    LaunchedEffect(Unit) {
        wealthViewModel.loadAdvertisingData()
    }

    // Filter campaigns for the current logged-in advertiser
    val userCampaigns = remember(campaigns, userProfile.uid) {
        campaigns.filter { it.userId == userProfile.uid }
    }

    // Calculations
    val totalViews = userCampaigns.sumOf { it.viewsCount }
    val totalClicks = userCampaigns.sumOf { it.clicksCount }
    val avgCtr = if (totalViews > 0) {
        (totalClicks.toDouble() / totalViews) * 100
    } else {
        0.0
    }

    val activeCampaigns = userCampaigns.filter { it.status == "Active" }
    val pendingCampaigns = userCampaigns.filter { it.status == "Pending Approval" || it.status == "Payment Submitted" || it.status == "Pending Verification" }
    val draftCampaigns = userCampaigns.filter { it.status == "Awaiting Payment" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- PORTAL INTRODUCTION & CREATE ACTION ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wealth Builder Promotion Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Promote your assets & services with high CTR",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Campaign Hub",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showCreateCampaignDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_create_ad_campaign")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Advertising Campaign", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- PERFORMANCE ANALYTICS DASHBOARD CARD ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Campaign Performance Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stat 1: Views
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Impressions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("%,d", totalViews),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Stat 2: Clicks
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Clicks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("%,d", totalClicks),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Stat 3: CTR
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Avg CTR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("%.2f%%", avgCtr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreenLight
                            )
                        }
                    }
                }
            }
        }

        // --- SUBMITTED CAMPAIGNS HEADER ---
        item {
            Text(
                text = "Your Promotion Campaigns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // --- LIST OF ADVERTISER CAMPAIGNS ---
        if (userCampaigns.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No campaigns registered yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Create a campaign and choose a package to put your business or affiliate links in front of the community.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(userCampaigns) { campaign ->
                AdCampaignRowCard(
                    campaign = campaign,
                    onPauseResume = { pause ->
                        wealthViewModel.pauseResumeAdCampaign(campaign.id, pause) { success ->
                            if (success) {
                                Toast.makeText(context, if (pause) "Campaign paused" else "Campaign resumed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDelete = {
                        wealthViewModel.deleteAdCampaign(campaign.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Campaign deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onPayInstructions = {
                        showPaymentInstructionsCampaignId = campaign.id
                    },
                    onConfirmPayment = {
                        showPaymentConfirmDialogCampaign = campaign
                    }
                )
            }
        }
    }

    // --- CREATE CAMPAIGN DIALOG WIZARD ---
    if (showCreateCampaignDialog) {
        var selectedPackage by remember { mutableStateOf<AdPackage?>(packages.firstOrNull()) }
        var adTitle by remember { mutableStateOf("") }
        var adDescription by remember { mutableStateOf("") }
        var adCategory by remember { mutableStateOf("Affiliate Marketing") }
        var adType by remember { mutableStateOf("Homepage Banner") }
        var destinationUrl by remember { mutableStateOf("") }
        var imageUrl by remember { mutableStateOf("") }
        var videoUrl by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        val categories = listOf("Affiliate Marketing", "Academy Education", "FinTech Services", "Crypto & Forex", "E-Commerce", "Consulting", "Software Tools")
        val adTypes = listOf("Homepage Banner", "Sidebar Banner", "Product Sponsored Ads", "Search Result Sponsored Ads", "Featured Seller Ads", "Popup Advertisements", "In-App Promotional Cards")

        // Default templates to auto-fill beautiful images for quick developer preview
        val imageTemplates = mapOf(
            "Homepage Banner" to "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=800&auto=format&fit=crop",
            "Sidebar Banner" to "https://images.unsplash.com/photo-1542744094-3a31f103e35f?q=80&w=500&auto=format&fit=crop",
            "Product Sponsored Ads" to "https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=500&auto=format&fit=crop",
            "Popup Advertisements" to "https://images.unsplash.com/photo-1557200134-90327ee9fafa?q=80&w=600&auto=format&fit=crop"
        )

        // Sync default template image if empty
        LaunchedEffect(adType) {
            imageUrl = imageTemplates[adType] ?: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=800&auto=format&fit=crop"
        }

        AlertDialog(
            onDismissRequest = { showCreateCampaignDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("New Advertising Campaign", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Set up your sponsored content to showcase your business on our educational academy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // --- CHOOSE PACKAGE ---
                    Text("1. Select Campaign Package *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        packages.forEach { pkg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPackage = pkg }
                                    .background(
                                        if (selectedPackage?.id == pkg.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    RadioButton(
                                        selected = selectedPackage?.id == pkg.id,
                                        onClick = { selectedPackage = pkg }
                                    )
                                    Column {
                                        Text(pkg.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${pkg.durationDays} Days Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                                Text(
                                    text = String.format("$%,.2f", pkg.price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // --- FORM FIELDS ---
                    Text("2. Campaign Copy & Assets *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = adTitle,
                        onValueChange = { adTitle = it },
                        label = { Text("Advertisement Title *") },
                        placeholder = { Text("e.g. Master Crypto Options Trading") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ad_title_input")
                    )

                    OutlinedTextField(
                        value = adDescription,
                        onValueChange = { adDescription = it },
                        label = { Text("Promo Description *") },
                        placeholder = { Text("e.g. Join the #1 training platform. Sign up today!") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("ad_desc_input")
                    )

                    // --- CATEGORY SPINNER ---
                    Text("Business Category *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { adCategory = cat }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = adCategory == cat, onClick = { adCategory = cat })
                                Text(cat, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    // --- AD PLACEMENT TYPE ---
                    Text("Ad Display Placement *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        adTypes.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { adType = type }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = adType == type, onClick = { adType = type })
                                Text(type, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = destinationUrl,
                        onValueChange = { destinationUrl = it },
                        label = { Text("Destination URL *") },
                        placeholder = { Text("https://wealthbuilder.academy/ref-options") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth().testTag("ad_url_input")
                    )

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Banner Image URL") },
                        placeholder = { Text("https://url-to-your-image.png") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ad_img_input")
                    )

                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Promotional Video URL (Optional)") },
                        placeholder = { Text("https://url-to-your-video.mp4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ad_video_input")
                    )

                    if (errorText.isNotEmpty()) {
                        Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adTitle.isBlank() || adDescription.isBlank() || destinationUrl.isBlank() || selectedPackage == null) {
                            errorText = "Please fill in all mandatory fields (*)"
                            return@Button
                        }
                        errorText = ""
                        isSubmitting = true

                        val pkg = selectedPackage!!
                        val calendar = Calendar.getInstance()
                        val startDate = calendar.timeInMillis
                        calendar.add(Calendar.DAY_OF_YEAR, pkg.durationDays)
                        val endDate = calendar.timeInMillis

                        val newCampaign = AdCampaign(
                            id = UUID.randomUUID().toString(),
                            userId = userProfile.uid,
                            userEmail = userProfile.email,
                            title = adTitle,
                            description = adDescription,
                            bannerUrl = imageUrl,
                            videoUrl = videoUrl,
                            destinationUrl = destinationUrl,
                            category = adCategory,
                            adType = adType,
                            startDate = startDate,
                            endDate = endDate,
                            status = "Awaiting Payment",
                            budget = pkg.price,
                            pricePaid = 0.0,
                            planName = pkg.name,
                            viewsCount = 0,
                            clicksCount = 0,
                            isFeatured = false,
                            adminNotes = ""
                        )

                        wealthViewModel.createAdCampaign(newCampaign) { success ->
                            isSubmitting = false
                            if (success) {
                                showCreateCampaignDialog = false
                                showPaymentInstructionsCampaignId = newCampaign.id
                            } else {
                                errorText = "Error saving campaign. Please retry."
                            }
                        }
                    },
                    modifier = Modifier.testTag("submit_create_ad_campaign")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save & Proceed to Payment")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCampaignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- PAYMENT INSTRUCTIONS DIALOG ---
    if (showPaymentInstructionsCampaignId != null) {
        val campId = showPaymentInstructionsCampaignId!!
        val camp = campaigns.firstOrNull { it.id == campId }

        AlertDialog(
            onDismissRequest = { showPaymentInstructionsCampaignId = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Manual Payment Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "To activate your advertising campaign, please complete a manual bank transfer of the campaign package amount.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    camp?.let { c ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Campaign Title:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(c.title, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ad Package Chosen:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(c.planName, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Amount Payable:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("$%,.2f", c.budget),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Platform Bank Account Details:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bank Name:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("OPay", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Account Number:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("9162072645", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Account Name:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("Chizaram W. Amajor", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Text(
                        text = "⚠️ Verification may take up to 24 hours after submitting payment confirmation.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetedCampaign = camp ?: campaigns.firstOrNull { it.id == campId }
                        showPaymentInstructionsCampaignId = null
                        if (targetedCampaign != null) {
                            showPaymentConfirmDialogCampaign = targetedCampaign
                        }
                    }
                ) {
                    Text("Confirm & Submit Payment Proof")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentInstructionsCampaignId = null }) {
                    Text("Close")
                }
            }
        )
    }

    // --- MANUAL PAYMENT CONFIRMATION DIALOG ---
    if (showPaymentConfirmDialogCampaign != null) {
        val targetedCamp = showPaymentConfirmDialogCampaign!!
        var referenceCode by remember { mutableStateOf("") }
        var proofImageDescription by remember { mutableStateOf("") }
        var errorConfirmText by remember { mutableStateOf("") }
        var isSubmittingConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPaymentConfirmDialogCampaign = null },
            title = { Text("Submit Payment Proof", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Please enter your manual payment transfer references to allow our administrators to audit your transfer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = referenceCode,
                        onValueChange = { referenceCode = it },
                        label = { Text("Bank Reference Code / ID *") },
                        placeholder = { Text("e.g. TXN-940321852") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ad_payment_ref_input")
                    )

                    OutlinedTextField(
                        value = proofImageDescription,
                        onValueChange = { proofImageDescription = it },
                        label = { Text("Upload / Description of Transfer Proof *") },
                        placeholder = { Text("e.g. Uploaded transfer_receipt.pdf / OPay Success Screenshot") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("ad_payment_proof_input")
                    )

                    if (errorConfirmText.isNotEmpty()) {
                        Text(errorConfirmText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (referenceCode.isBlank() || proofImageDescription.isBlank()) {
                            errorConfirmText = "Reference and description are mandatory."
                            return@Button
                        }
                        errorConfirmText = ""
                        isSubmittingConfirm = true

                        // Mark status as 'Payment Submitted' / 'Pending Verification'
                        val updatedCamp = targetedCamp.copy(
                            status = "Pending Verification",
                            adminNotes = "Ref: $referenceCode • Proof details: $proofImageDescription"
                        )

                        wealthViewModel.updateAdCampaign(updatedCamp) { success ->
                            isSubmittingConfirm = false
                            if (success) {
                                showPaymentConfirmDialogCampaign = null
                                Toast.makeText(context, "Payment proof submitted successfully! Verification takes up to 24 hrs.", Toast.LENGTH_LONG).show()
                            } else {
                                errorConfirmText = "Failed to update campaign. Try again."
                            }
                        }
                    },
                    modifier = Modifier.testTag("submit_ad_payment_proof")
                ) {
                    if (isSubmittingConfirm) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Confirm Transfer")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirmDialogCampaign = null }) {
                    Text("Back")
                }
            }
        )
    }
}

@Composable
fun AdCampaignRowCard(
    campaign: AdCampaign,
    onPauseResume: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onPayInstructions: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startStr = dateFormat.format(Date(campaign.startDate))
    val endStr = dateFormat.format(Date(campaign.endDate))

    val statusColor = when (campaign.status) {
        "Active" -> EmeraldGreenLight
        "Pending Approval", "Pending Verification" -> RichGold
        "Payment Submitted" -> MaterialTheme.colorScheme.secondary
        "Rejected" -> MaterialTheme.colorScheme.error
        "Paused" -> MaterialTheme.colorScheme.outline
        "Expired" -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title, Status, Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = campaign.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${campaign.planName} • ${campaign.adType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = campaign.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Description
            Text(
                text = campaign.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Campaign dates & price info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schedule: $startStr - $endStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Destination: ${campaign.destinationUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = String.format("$%,.2f", campaign.budget),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Media Preview (Optional banner URL check)
            if (campaign.bannerUrl.isNotEmpty()) {
                AsyncImage(
                    model = campaign.bannerUrl,
                    contentDescription = "Ad Banner Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // Analytics if campaign is Active or Expired
            if (campaign.status == "Active" || campaign.status == "Expired" || campaign.status == "Paused") {
                val ctr = if (campaign.viewsCount > 0) (campaign.clicksCount.toDouble() / campaign.viewsCount) * 100 else 0.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Impressions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("${campaign.viewsCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Clicks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("${campaign.clicksCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CTR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(String.format("%.2f%%", ctr), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreenLight)
                    }
                }
            }

            // Notes / rejection reasons from Administrator
            if (campaign.adminNotes.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text(
                            text = campaign.adminNotes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Buttons / Actions contextually available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (campaign.status == "Awaiting Payment") {
                    TextButton(onClick = onPayInstructions) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Payment Info")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirmPayment,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight)
                    ) {
                        Text("Confirm Transfer", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (campaign.status == "Active" || campaign.status == "Paused") {
                    val isPaused = campaign.status == "Paused"
                    Button(
                        onClick = { onPauseResume(!isPaused) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) EmeraldGreenLight else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPaused) "Resume" else "Pause", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_ad_campaign_${campaign.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
