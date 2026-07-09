package com.example.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.WealthViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDashboardScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    wealthViewModel: WealthViewModel,
    onNavigateToCategory: (EarningCategory) -> Unit,
    onNavigateToArticle: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Overview, 1 = Notifications, 2 = Activity, 3 = Support, 4 = Settings
    val firstName = userProfile.displayName.split(" ").firstOrNull() ?: "Pioneer"

    LaunchedEffect(Unit) {
        wealthViewModel.loadAdvertisingData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Welcome and Status Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = firstName.take(1).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hello, $firstName! 👋",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Awaiting your next big breakthrough",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Premium Approved Badge
                Surface(
                    color = EmeraldGreenLight.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldGreenLight, RichGold)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified status",
                            tint = EmeraldGreenLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userProfile.accountStatus,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight
                            )
                        )
                    }
                }
            }
        }

        // Sub Navigation Tabs Row
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "Overview" to Icons.Default.Dashboard,
                "Marketplace" to Icons.Default.ShoppingCart,
                "Seller Hub" to Icons.Default.Storefront,
                "Advertise" to Icons.Default.Campaign,
                "Invite & Earn" to Icons.Default.GroupAdd,
                "Inbox" to Icons.Default.Notifications,
                "Activities" to Icons.Default.History,
                "Help Support" to Icons.Default.ContactSupport,
                "Settings" to Icons.Default.Settings
            )
            tabs.forEachIndexed { index, (label, icon) ->
                Tab(
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index },
                    modifier = Modifier.testTag("sub_tab_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(18.dp),
                            tint = if (activeSubTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeSubTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated Screen Transitions based on Sub Tab
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = activeSubTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "sub_tab_anim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewSubTab(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel,
                        onNavigateToCategory = onNavigateToCategory,
                        onNavigateToArticle = onNavigateToArticle
                    )
                    1 -> MarketplaceSubTab(
                        viewModel = wealthViewModel,
                        userId = userProfile.uid,
                        authViewModel = authViewModel
                    )
                    2 -> SellerHubSubTab(
                        userProfile = userProfile,
                        authViewModel = authViewModel,
                        wealthViewModel = wealthViewModel
                    )
                    3 -> AdCampaignManagerSubTab(
                        userProfile = userProfile,
                        wealthViewModel = wealthViewModel
                    )
                    4 -> InviteSubTab(
                        authViewModel = authViewModel,
                        userProfile = userProfile
                    )
                    5 -> NotificationsSubTab(authViewModel = authViewModel)
                    6 -> ActivitySubTab(authViewModel = authViewModel, userId = userProfile.uid)
                    7 -> SupportSubTab(authViewModel = authViewModel, userProfile = userProfile)
                    8 -> SettingsSubTab(authViewModel = authViewModel, userProfile = userProfile, wealthViewModel = wealthViewModel, onNavigateToArticle = onNavigateToArticle)
                }
            }
        }
    }
}

@Composable
fun OverviewSubTab(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    wealthViewModel: WealthViewModel,
    onNavigateToCategory: (EarningCategory) -> Unit,
    onNavigateToArticle: (Article) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    val announcements by authViewModel.announcements.collectAsState()

    val campaigns by wealthViewModel.adCampaigns.collectAsState()
    val activeAds = remember(campaigns) { campaigns.filter { it.status == "Active" } }

    var popupAdToShow by remember { mutableStateOf<com.example.data.model.AdCampaign?>(null) }
    var popupDismissed by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(activeAds) {
        if (!popupDismissed) {
            popupAdToShow = activeAds.firstOrNull { it.adType == "Popup Advertisements" }
        }
    }

    if (popupAdToShow != null) {
        val pad = popupAdToShow!!
        LaunchedEffect(pad.id) {
            wealthViewModel.recordAdView(pad.id)
        }
        AlertDialog(
            onDismissRequest = {
                popupAdToShow = null
                popupDismissed = true
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Featured Special Offer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (pad.bannerUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = pad.bannerUrl,
                            contentDescription = pad.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Text(pad.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(pad.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        wealthViewModel.recordAdClick(pad.id)
                        popupAdToShow = null
                        popupDismissed = true
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pad.destinationUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Opening: ${pad.destinationUrl}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Learn More")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        popupAdToShow = null
                        popupDismissed = true
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Format registration date
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val regDateStr = dateFormat.format(Date(userProfile.dateCreated))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HOMEPAGE BANNER ADS ---
        val homepageBanners = remember(activeAds) { activeAds.filter { it.adType == "Homepage Banner" } }
        homepageBanners.forEach { ad ->
            SponsoredAdCard(ad = ad, viewModel = wealthViewModel)
        }

        // --- SIDEBAR BANNER ADS ---
        val sidebarBanners = remember(activeAds) { activeAds.filter { it.adType == "Sidebar Banner" } }
        sidebarBanners.forEach { ad ->
            SponsoredAdCard(ad = ad, viewModel = wealthViewModel)
        }
        // Stats Cards Grid
        Text(
            text = "Academy Membership Analytics",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Account Status
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldGreenLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    )
                    Text(
                        text = userProfile.accountStatus,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = EmeraldGreenLight)
                    )
                }
            }

            // Card 2: Registration Date
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = RichGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enrolled",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    )
                    Text(
                        text = regDateStr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            // Card 3: Membership Tier
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tier",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    )
                    Text(
                        text = "Elite",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Latest Announcements
        if (announcements.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Announcements",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Latest Academy Announcement",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PINNED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val pin = announcements.firstOrNull { it.isPinned } ?: announcements.first()
                    Text(
                        text = pin.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pin.content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Published by: ${pin.author}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                        val formattedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(pin.timestamp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // Hero Asset Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_wealth_hero),
                    contentDescription = "Wealth growth illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.75f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Build Scalable Systems",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unlock high-paying contracts, optimize your affiliate funnels, and build premium templates.",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Earning Goal Tracking Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Target Earning Progress",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Goal: ${currencyFormatter.format(userProfile.monthlyGoal)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val progressFraction = if (userProfile.monthlyGoal > 0) {
                    (userProfile.currentSavedBalance / userProfile.monthlyGoal).toFloat().coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currencyFormatter.format(userProfile.currentSavedBalance)} simulated",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% Met",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (progressFraction >= 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Perform simulated actions inside the platform to credit your progress balance:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { authViewModel.earnMockCredits(150.0) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Task (+$150)", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { authViewModel.earnMockCredits(750.0) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deal (+$750)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Core Earning Categories
        Text(
            text = "Earning Core Foundations",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCard(
                title = "Affiliate",
                icon = Icons.Default.Share,
                subtitle = "Monetize niche traffic",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToCategory(EarningCategory.AFFILIATE_MARKETING) }
            )
            CategoryCard(
                title = "Freelance",
                icon = Icons.Default.Work,
                subtitle = "High-income strategic skillsets",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToCategory(EarningCategory.FREELANCING) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCard(
                title = "Products",
                icon = Icons.Default.ShoppingBag,
                subtitle = "Publish templates",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToCategory(EarningCategory.DIGITAL_PRODUCTS) }
            )
            CategoryCard(
                title = "Education",
                icon = Icons.Default.ShowChart,
                subtitle = "Compound earnings",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToCategory(EarningCategory.FINANCIAL_EDUCATION) }
            )
        }

        // Featured Guides
        Text(
            text = "Highly Curated Blueprints",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(wealthViewModel.featuredArticles) { article ->
                FeaturedArticleCard(
                    article = article,
                    onClick = { onNavigateToArticle(article) }
                )
            }
        }

        // --- IN-APP PROMOTIONAL CARDS ---
        val promoCards = remember(activeAds) { activeAds.filter { it.adType == "In-App Promotional Cards" } }
        promoCards.forEach { ad ->
            SponsoredAdCard(ad = ad, viewModel = wealthViewModel)
        }

        // --- FEATURED SELLER ADS ---
        val featuredSellers = remember(activeAds) { activeAds.filter { it.adType == "Featured Seller Ads" } }
        if (featuredSellers.isNotEmpty()) {
            Text(
                text = "Featured Businesses & Partners 🌟",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
            featuredSellers.forEach { ad ->
                SponsoredAdCard(ad = ad, viewModel = wealthViewModel)
            }
        }
    }
}

@Composable
fun NotificationsSubTab(authViewModel: AuthViewModel) {
    val notifications by authViewModel.notifications.collectAsState()

    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AllInbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Academy Inbox is Empty",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Important notifications, verification receipts, and private admin updates will appear here.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Academy Alerts Inbox",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${notifications.count { !it.isRead }} Unread",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            items(notifications) { item ->
                val icon = when (item.type) {
                    "approval" -> Icons.Default.Verified
                    "rejection" -> Icons.Default.ErrorOutline
                    "payment" -> Icons.Default.ReceiptLong
                    "announcement" -> Icons.Default.Campaign
                    else -> Icons.Default.Info
                }
                val color = when (item.type) {
                    "approval" -> EmeraldGreenLight
                    "rejection" -> MaterialTheme.colorScheme.error
                    "payment" -> RichGold
                    "announcement" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { authViewModel.markNotificationAsRead(item.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (!item.isRead) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(color.copy(alpha = 0.3f), color))) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = color.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (item.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                                        color = if (item.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                                    )
                                )
                                if (!item.isRead) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (item.isRead) 0.7f else 0.9f),
                                    lineHeight = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val formattedTime = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                                IconButton(
                                    onClick = { authViewModel.deleteNotification(item.id) },
                                    modifier = Modifier.size(24.dp).testTag("delete_notif_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Notification",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySubTab(authViewModel: AuthViewModel, userId: String) {
    val activities by authViewModel.recentActivities.collectAsState()

    LaunchedEffect(userId) {
        authViewModel.loadRecentActivities(userId)
    }

    if (activities.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Activity Logs Yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Perform activities like passing evaluation quizzes, earning balance credits, or updating your profile to seed this log.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Real-time Operations Ledger",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(activities) { act ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Timeline bubble
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                        // line representation
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = act.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = act.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedTime = SimpleDateFormat("MMM dd, yyyy - HH:mm:ss", Locale.getDefault()).format(Date(act.timestamp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportSubTab(authViewModel: AuthViewModel, userProfile: UserProfile) {
    val tickets by authViewModel.userTickets.collectAsState()
    var activeTicketForChat by remember { mutableStateOf<com.example.data.model.SupportTicket?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    
    var subjectInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(userProfile.uid) {
        authViewModel.loadUserSupportTickets(userProfile.uid)
    }

    // Keep active chat in sync with updated list
    LaunchedEffect(tickets) {
        activeTicketForChat?.let { active ->
            activeTicketForChat = tickets.firstOrNull { it.id == active.id }
        }
    }

    AnimatedContent(
        targetState = when {
            activeTicketForChat != null -> "chat"
            showCreateForm -> "create"
            else -> "list"
        },
        label = "support_subtab_anim"
    ) { mode ->
        when (mode) {
            "chat" -> {
                val ticket = activeTicketForChat!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Chat Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeTicketForChat = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = ticket.subject,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (ticket.status) {
                                "Open" -> Color(0xFFF39C12).copy(alpha = 0.15f)
                                "Replied" -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                "Closed" -> Color.Gray.copy(alpha = 0.15f)
                                else -> Color.LightGray
                            }
                        ) {
                            Text(
                                text = ticket.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = when (ticket.status) {
                                    "Open" -> Color(0xFFD35400)
                                    "Replied" -> Color(0xFF27AE60)
                                    "Closed" -> Color.DarkGray
                                    else -> Color.Black
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message lists
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Display Original Description as the first bubble
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Original Request - ${ticket.userName}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = ticket.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                                    Text(
                                        text = sdf.format(Date(ticket.dateCreated)),
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }

                            // Dynamic Replies List
                            ticket.messages.forEach { msg ->
                                val isMe = msg.senderUid == userProfile.uid
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 16.dp
                                        ),
                                        color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White,
                                        border = BorderStroke(1.dp, if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (isMe) "You" else msg.senderName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37)
                                                    )
                                                )
                                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                                Text(
                                                    text = sdf.format(Date(msg.timestamp)),
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = msg.message,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input bar to reply
                    if (ticket.status != "Closed") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Write your reply here...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ticket_reply_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                            )
                            Button(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        authViewModel.replyToSupportTicket(
                                            ticketId = ticket.id,
                                            senderUid = userProfile.uid,
                                            senderName = userProfile.displayName,
                                            messageText = replyText
                                        )
                                        replyText = ""
                                    }
                                },
                                enabled = replyText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(52.dp).testTag("ticket_reply_send_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f))
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("This ticket is marked Closed by Admin compliance team.", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            "create" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showCreateForm = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text("Create Support Request 💬", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }

                    Text("Describe your concern, question, or payment issue in detail. An expert financial advisor will audit and respond.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject / Main Issue") },
                        placeholder = { Text("e.g. Freelancing video tutorial isn't loading") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("support_subject"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Detailed Description") },
                        placeholder = { Text("Please describe the issue or question as thoroughly as possible...") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("support_details"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Button(
                        onClick = {
                            if (subjectInput.isNotBlank() && descriptionInput.isNotBlank()) {
                                authViewModel.createSupportTicket(subjectInput, descriptionInput, userProfile)
                                subjectInput = ""
                                descriptionInput = ""
                                showCreateForm = false
                            }
                        },
                        enabled = subjectInput.isNotBlank() && descriptionInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_submit_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Ticket to Academy", fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Support Tickets 💬",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        )
                        Button(
                            onClick = { showCreateForm = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("support_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Ticket", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (tickets.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ContactSupport, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Active Support Tickets", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Got questions or tech issues? Submit a secure support ticket to consult our lead advisors.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            tickets.forEach { ticket ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeTicketForChat = ticket },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = when (ticket.status) {
                                                        "Open" -> Color(0xFFF39C12).copy(alpha = 0.12f)
                                                        "Replied" -> Color(0xFF2ECC71).copy(alpha = 0.12f)
                                                        "Closed" -> Color.Gray.copy(alpha = 0.12f)
                                                        else -> Color.LightGray
                                                    }
                                                ) {
                                                    Text(
                                                        text = ticket.status,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = when (ticket.status) {
                                                            "Open" -> Color(0xFFD35400)
                                                            "Replied" -> Color(0xFF27AE60)
                                                            "Closed" -> Color.DarkGray
                                                            else -> Color.Black
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                                                Text(sdf.format(Date(ticket.lastUpdated)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(ticket.subject, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(ticket.description, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSubTab(
    authViewModel: AuthViewModel,
    userProfile: UserProfile,
    wealthViewModel: com.example.ui.WealthViewModel,
    onNavigateToArticle: (Article) -> Unit
) {
    ProfileScreen(
        userProfile = userProfile,
        authViewModel = authViewModel,
        wealthViewModel = wealthViewModel,
        onNavigateToArticle = onNavigateToArticle
    )
}

@Composable
fun SponsoredAdCard(
    ad: com.example.data.model.AdCampaign,
    viewModel: com.example.ui.WealthViewModel,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(ad.id) {
        viewModel.recordAdView(ad.id)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                viewModel.recordAdClick(ad.id)
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(ad.destinationUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Opening: ${ad.destinationUrl}", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("SPONSORED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.primary)
                }
                Text(ad.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            if (ad.bannerUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = ad.bannerUrl,
                    contentDescription = ad.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(ad.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(ad.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
