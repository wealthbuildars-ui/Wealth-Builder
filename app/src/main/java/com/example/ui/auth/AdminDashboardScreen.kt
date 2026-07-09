package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.Announcement
import com.example.data.model.AdminSettings
import com.example.data.model.ReferralRecord
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*
import com.example.ui.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    wealthViewModel: WealthViewModel,
    modifier: Modifier = Modifier
) {
    val users by authViewModel.allUsers.collectAsState()
    val isTimerEnabled by authViewModel.isTimerEnabled.collectAsState()
    val announcements by authViewModel.announcements.collectAsState()
    val adminSettings by authViewModel.adminSettings.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) } // 0 = Users & Registry, 1 = Announcements, 2 = Platform Settings

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Approved", "Rejected", "Expired"
    
    var selectedUserForDetail by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForProof by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForRejection by remember { mutableStateOf<UserProfile?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    // Announcement creation dialog fields
    var showCreateAnnDialog by remember { mutableStateOf(false) }
    var selectedAnnForEdit by remember { mutableStateOf<Announcement?>(null) }
    var annTitleInput by remember { mutableStateOf("") }
    var annContentInput by remember { mutableStateOf("") }
    var annIsPinned by remember { mutableStateOf(false) }

    // Settings fields
    var feeInput by remember { mutableStateOf(adminSettings.registrationFee.toInt().toString()) }
    var bankInput by remember { mutableStateOf(adminSettings.bankName) }
    var accountNumInput by remember { mutableStateOf(adminSettings.accountNumber) }
    var accountNameInput by remember { mutableStateOf(adminSettings.accountName) }
    var siteNameInput by remember { mutableStateOf(adminSettings.siteName) }
    var siteDescInput by remember { mutableStateOf(adminSettings.siteDescription) }
    var isReferralProgramEnabled by remember { mutableStateOf(adminSettings.isReferralProgramEnabled) }
    var referralRewardAmountInput by remember { mutableStateOf(adminSettings.referralRewardAmount.toInt().toString()) }
    var grantRewardOnStatusSelection by remember { mutableStateOf(adminSettings.grantRewardOnStatus) }

    var heroTitleInput by remember { mutableStateOf(adminSettings.homepageHeroTitle) }
    var heroSubtitleInput by remember { mutableStateOf(adminSettings.homepageHeroSubtitle) }
    var aboutTextInput by remember { mutableStateOf(adminSettings.aboutText) }
    var contactEmailInput by remember { mutableStateOf(adminSettings.contactEmail) }
    var contactPhoneInput by remember { mutableStateOf(adminSettings.contactPhone) }
    var contactHoursInput by remember { mutableStateOf(adminSettings.businessHours) }
    var contactMapAddressInput by remember { mutableStateOf(adminSettings.googleMapsAddress) }
    var footerCopyrightInput by remember { mutableStateOf(adminSettings.footerCopyright) }

    // New multi-vendor settings fields
    var sellerRevenuePercentInput by remember { mutableStateOf(adminSettings.sellerRevenuePercent.toString()) }
    var affiliateRevenuePercentInput by remember { mutableStateOf(adminSettings.affiliateRevenuePercent.toString()) }
    var platformRevenuePercentInput by remember { mutableStateOf(adminSettings.platformRevenuePercent.toString()) }
    var isCustomerDiscountEnabledInput by remember { mutableStateOf(adminSettings.isCustomerDiscountEnabled) }
    var customerDiscountPercentInput by remember { mutableStateOf(adminSettings.customerDiscountPercent.toString()) }

    // Sync settings fields when load completes
    LaunchedEffect(adminSettings) {
        feeInput = adminSettings.registrationFee.toInt().toString()
        bankInput = adminSettings.bankName
        accountNumInput = adminSettings.accountNumber
        accountNameInput = adminSettings.accountName
        siteNameInput = adminSettings.siteName
        siteDescInput = adminSettings.siteDescription
        isReferralProgramEnabled = adminSettings.isReferralProgramEnabled
        referralRewardAmountInput = adminSettings.referralRewardAmount.toInt().toString()
        grantRewardOnStatusSelection = adminSettings.grantRewardOnStatus
        heroTitleInput = adminSettings.homepageHeroTitle
        heroSubtitleInput = adminSettings.homepageHeroSubtitle
        aboutTextInput = adminSettings.aboutText
        contactEmailInput = adminSettings.contactEmail
        contactPhoneInput = adminSettings.contactPhone
        contactHoursInput = adminSettings.businessHours
        contactMapAddressInput = adminSettings.googleMapsAddress
        footerCopyrightInput = adminSettings.footerCopyright
        
        sellerRevenuePercentInput = adminSettings.sellerRevenuePercent.toString()
        affiliateRevenuePercentInput = adminSettings.affiliateRevenuePercent.toString()
        platformRevenuePercentInput = adminSettings.platformRevenuePercent.toString()
        isCustomerDiscountEnabledInput = adminSettings.isCustomerDiscountEnabled
        customerDiscountPercentInput = adminSettings.customerDiscountPercent.toString()
    }

    // Stats calculations
    val totalRegistered = users.size
    val totalPending = users.count { it.accountStatus == "Pending Verification" }
    val totalApproved = users.count { it.accountStatus == "Approved" }
    val totalRejected = users.count { it.accountStatus == "Rejected" }
    val totalExpired = users.count { it.accountStatus == "Expired" }

    val calToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = calToday.timeInMillis
    val totalToday = users.count { it.dateCreated >= todayStart }

    // Chart 1 Data: User registrations by day (last 7 days)
    val registrationsByDay = remember(users) {
        val counts = mutableListOf<Pair<String, Int>>()
        val sdf = SimpleDateFormat("E", Locale.getDefault())
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCal.timeInMillis
            val end = start + 24 * 60 * 60 * 1000L
            val count = users.count { it.dateCreated in start until end }
            counts.add(sdf.format(dayCal.time) to count)
        }
        counts
    }

    // Chart 2 Data: Approval Rate
    val approvalPercent = remember(users) {
        val totalDecided = users.count { it.accountStatus == "Approved" || it.accountStatus == "Rejected" }
        if (totalDecided > 0) {
            (users.count { it.accountStatus == "Approved" } * 100) / totalDecided
        } else {
            100
        }
    }

    // Chart 3 Data: Pending verifications received trend (last 5 days)
    val pendingTrend = remember(users) {
        val counts = mutableListOf<Pair<String, Int>>()
        val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
        for (i in 4 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCal.timeInMillis
            val end = start + 24 * 60 * 60 * 1000L
            val count = users.count { it.accountStatus == "Pending Verification" && it.paymentSubmittedTime in start until end }
            counts.add(sdf.format(dayCal.time) to count)
        }
        counts
    }

    // Filtered users list
    val filteredUsers = users.filter { user ->
        val matchesSearch = user.displayName.contains(searchQuery, ignoreCase = true) || 
                            user.email.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = when (statusFilter) {
            "All" -> true
            "Pending" -> user.accountStatus == "Pending Verification"
            "Approved" -> user.accountStatus == "Approved"
            "Rejected" -> user.accountStatus == "Rejected"
            "Expired" -> user.accountStatus == "Expired"
            else -> true
        }
        
        matchesSearch && matchesFilter
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchAllUsers()
        authViewModel.loadAnnouncements()
        authViewModel.loadAdminSettings()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Wealth Builder Admin",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { 
                            authViewModel.fetchAllUsers()
                            authViewModel.loadAnnouncements()
                            authViewModel.loadAdminSettings()
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = { authViewModel.signOut() },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Five primary tabs for administrator tasks
            ScrollableTabRow(
                selectedTabIndex = activeAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = activeAdminTab == 0,
                    onClick = { activeAdminTab = 0 },
                    text = { Text("Registry & Charts", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 1,
                    onClick = { activeAdminTab = 1 },
                    text = { Text("Announcements", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 2,
                    onClick = { activeAdminTab = 2 },
                    text = { Text("Platform Settings", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 3,
                    onClick = { activeAdminTab = 3 },
                    text = { Text("Referral Program", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 4,
                    onClick = { activeAdminTab = 4 },
                    text = { Text("Support Tickets", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ContactSupport, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 5,
                    onClick = { activeAdminTab = 5 },
                    text = { Text("Affiliate Marketplace", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null) }
                )
                Tab(
                    selected = activeAdminTab == 6,
                    onClick = { activeAdminTab = 6 },
                    text = { Text("Commissions & Payouts", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Paid, contentDescription = null) }
                )
            }

            // Notifications bar for Pending reviews
            AnimatedVisibility(
                visible = totalPending > 0 && activeAdminTab == 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = RichGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = RichGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "🔔 There are $totalPending review tickets awaiting payment proof authorization!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { statusFilter = "Pending" },
                            colors = ButtonDefaults.textButtonColors(contentColor = RichGold)
                        ) {
                            Text("View Queue", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Tab Content
            when (activeAdminTab) {
                0 -> {
                    // TAB 0: Registry & Charts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Stat Cards Grid
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Platform Performance KPIs",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    StatCard(
                                        title = "Total Users",
                                        value = totalRegistered.toString(),
                                        color = MaterialTheme.colorScheme.primary,
                                        icon = Icons.Default.Group,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Approved Members",
                                        value = totalApproved.toString(),
                                        color = EmeraldGreenLight,
                                        icon = Icons.Default.Verified,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Pending Proofs",
                                        value = totalPending.toString(),
                                        color = RichGold,
                                        icon = Icons.Default.HourglassEmpty,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    StatCard(
                                        title = "Rejected Proofs",
                                        value = totalRejected.toString(),
                                        color = MaterialTheme.colorScheme.error,
                                        icon = Icons.Default.Block,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Expired Requests",
                                        value = totalExpired.toString(),
                                        color = MaterialTheme.colorScheme.outline,
                                        icon = Icons.Default.HistoryToggleOff,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Today's Enrolled",
                                        value = totalToday.toString(),
                                        color = MaterialTheme.colorScheme.tertiary,
                                        icon = Icons.Default.HowToReg,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // CHARTS SECTION
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Visual Performance Analytics",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Chart 1: Daily Registrations (Last 7 Days)
                                    Text(
                                        "1. User Enrollments by Day (Last 7 Days)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        val maxRegCount = registrationsByDay.maxOfOrNull { it.second } ?: 1
                                        val limitCeil = if (maxRegCount == 0) 1 else maxRegCount

                                        registrationsByDay.forEach { (day, count) ->
                                            val barHeightFraction = count.toFloat() / limitCeil
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight(barHeightFraction.coerceIn(0.1f, 1f))
                                                        .width(22.dp)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            if (count > 0) EmeraldGreenLight else MaterialTheme.colorScheme.outlineVariant.copy(
                                                                alpha = 0.4f
                                                            )
                                                        )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = day,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Chart 2 & 3 Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Chart 2: Approval Rate Donut Representation
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "2. Approval Rate",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.align(Alignment.Start)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.size(72.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    progress = { approvalPercent / 100f },
                                                    color = EmeraldGreenLight,
                                                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                                                    strokeWidth = 8.dp,
                                                    modifier = Modifier.size(72.dp)
                                                )
                                                Text(
                                                    text = "$approvalPercent%",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Valid proofs approved",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                            )
                                        }

                                        // Chart 3: Pending verification trend (last 5 days)
                                        Column(
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Text(
                                                "3. Pending Submissions Trend",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(72.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                val maxPendingTrend = pendingTrend.maxOfOrNull { it.second } ?: 1
                                                val maxTrendCeil = if (maxPendingTrend == 0) 1 else maxPendingTrend

                                                pendingTrend.forEach { (date, count) ->
                                                    val fraction = count.toFloat() / maxTrendCeil
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Bottom,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = count.toString(),
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxHeight(fraction.coerceIn(0.1f, 1.0f))
                                                                .width(14.dp)
                                                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                                .background(RichGold)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = date,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search and status chips
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "User Registry Management",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search by name or email...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_search_bar")
                                )

                                // Status filter chips
                                val filters = listOf("All", "Pending", "Approved", "Rejected", "Expired")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    filters.forEach { filter ->
                                        val selected = statusFilter == filter
                                        FilterChip(
                                            selected = selected,
                                            onClick = { statusFilter = filter },
                                            label = { Text(filter, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Users queue items
                        if (filteredUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No matching user records found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        } else {
                            items(filteredUsers) { user ->
                                UserAdminCard(
                                    user = user,
                                    onViewDetail = { selectedUserForDetail = user },
                                    onViewProof = { selectedUserForProof = user },
                                    onApprove = { 
                                        authViewModel.updateVerificationStatus(user.uid, "Approved")
                                    },
                                    onReject = { selectedUserForRejection = user }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Announcements Manager
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Announcements Registry",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Button(
                                    onClick = {
                                        selectedAnnForEdit = null
                                        annTitleInput = ""
                                        annContentInput = ""
                                        annIsPinned = false
                                        showCreateAnnDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Broadcast")
                                }
                            }
                        }

                        if (announcements.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No broadcast announcements published.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        } else {
                            items(announcements) { ann ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = ann.title,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    if (ann.isPinned) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Surface(
                                                            color = RichGold.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                "PINNED",
                                                                style = MaterialTheme.typography.labelSmall.copy(color = RichGold, fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(ann.timestamp))
                                                Text(
                                                    "Broadcast by ${ann.author} • $date",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                )
                                            }

                                            Row {
                                                IconButton(onClick = {
                                                    selectedAnnForEdit = ann
                                                    annTitleInput = ann.title
                                                    annContentInput = ann.content
                                                    annIsPinned = ann.isPinned
                                                    showCreateAnnDialog = true
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                IconButton(onClick = { authViewModel.deleteAnnouncement(ann.id) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = ann.content,
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Platform Settings
                    val scrollSettingsState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollSettingsState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Platform Core System Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 24 Hour expiration timer toggle card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "24-Hour Expiration Timer",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Force payment submissions to auto-expire after 24h",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                                Switch(
                                    checked = isTimerEnabled,
                                    onCheckedChange = { authViewModel.setTimerEnabled(it) }
                                )
                            }
                        }

                        // Registration Fee and Instructions Input Group
                        OutlinedTextField(
                            value = feeInput,
                            onValueChange = { feeInput = it },
                            label = { Text("Registration Activation Fee (₦)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bankInput,
                            onValueChange = { bankInput = it },
                            label = { Text("Bank Transfer - OPay instructions") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = accountNumInput,
                            onValueChange = { accountNumInput = it },
                            label = { Text("Transfer Account Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = accountNameInput,
                            onValueChange = { accountNameInput = it },
                            label = { Text("Transfer Account Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = siteNameInput,
                            onValueChange = { siteNameInput = it },
                            label = { Text("Academy Platform Title") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = siteDescInput,
                            onValueChange = { siteDescInput = it },
                            label = { Text("Academy Platforms Description Slogan") },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Expandable Card for Landing Page Editor
                        var showLandingEditor by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showLandingEditor = !showLandingEditor },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Landing Page Content Customizer 🎨", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("Customize Hero section, About information, and Contact coordinates", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                                    }
                                    Icon(
                                        imageVector = if (showLandingEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }

                                if (showLandingEditor) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Hero Header", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = heroTitleInput,
                                        onValueChange = { heroTitleInput = it },
                                        label = { Text("Hero Title Headline") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = heroSubtitleInput,
                                        onValueChange = { heroSubtitleInput = it },
                                        label = { Text("Hero Subtitle Text") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("About Section", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = aboutTextInput,
                                        onValueChange = { aboutTextInput = it },
                                        label = { Text("About Description Text") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Contact Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contactEmailInput,
                                        onValueChange = { contactEmailInput = it },
                                        label = { Text("Support Email Address") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contactPhoneInput,
                                        onValueChange = { contactPhoneInput = it },
                                        label = { Text("Support Phone Number") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contactHoursInput,
                                        onValueChange = { contactHoursInput = it },
                                        label = { Text("Business Hours") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contactMapAddressInput,
                                        onValueChange = { contactMapAddressInput = it },
                                        label = { Text("Google Map Address") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Footer Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = footerCopyrightInput,
                                        onValueChange = { footerCopyrightInput = it },
                                        label = { Text("Footer Slogan") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Expandable Card for Multi-Vendor & Revenue Settings
                        var showMultiVendorEditor by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showMultiVendorEditor = !showMultiVendorEditor },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Multi-Vendor & Discount Settings 🛒", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("Revenue sharing percentages and customer automatic discount rate", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                                    }
                                    Icon(
                                        imageVector = if (showMultiVendorEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }

                                if (showMultiVendorEditor) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Revenue Sharing Ratio (%)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Text("These must sum to 100%", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = sellerRevenuePercentInput,
                                        onValueChange = { sellerRevenuePercentInput = it },
                                        label = { Text("Seller Share (%)") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = affiliateRevenuePercentInput,
                                        onValueChange = { affiliateRevenuePercentInput = it },
                                        label = { Text("Affiliate Share (%)") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = platformRevenuePercentInput,
                                        onValueChange = { platformRevenuePercentInput = it },
                                        label = { Text("Platform Share (%)") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Automatic Customer Discount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Enable Automatic Customer Discount", style = MaterialTheme.typography.bodyMedium)
                                        Switch(
                                            checked = isCustomerDiscountEnabledInput,
                                            onCheckedChange = { isCustomerDiscountEnabledInput = it }
                                        )
                                    }

                                    if (isCustomerDiscountEnabledInput) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customerDiscountPercentInput,
                                            onValueChange = { customerDiscountPercentInput = it },
                                            label = { Text("Customer Discount (%)") },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val value = feeInput.toDoubleOrNull() ?: adminSettings.registrationFee
                                val rewardVal = referralRewardAmountInput.toDoubleOrNull() ?: adminSettings.referralRewardAmount
                                val sellerPct = sellerRevenuePercentInput.toDoubleOrNull() ?: adminSettings.sellerRevenuePercent
                                val affPct = affiliateRevenuePercentInput.toDoubleOrNull() ?: adminSettings.affiliateRevenuePercent
                                val platPct = platformRevenuePercentInput.toDoubleOrNull() ?: adminSettings.platformRevenuePercent
                                val discPct = customerDiscountPercentInput.toDoubleOrNull() ?: adminSettings.customerDiscountPercent
                                val updated = AdminSettings(
                                    registrationFee = value,
                                    isTimerEnabled = isTimerEnabled,
                                    bankName = bankInput,
                                    accountNumber = accountNumInput,
                                    accountName = accountNameInput,
                                    siteName = siteNameInput,
                                    siteDescription = siteDescInput,
                                    isReferralProgramEnabled = isReferralProgramEnabled,
                                    referralRewardAmount = rewardVal,
                                    grantRewardOnStatus = grantRewardOnStatusSelection,
                                    homepageHeroTitle = heroTitleInput,
                                    homepageHeroSubtitle = heroSubtitleInput,
                                    aboutText = aboutTextInput,
                                    contactEmail = contactEmailInput,
                                    contactPhone = contactPhoneInput,
                                    businessHours = contactHoursInput,
                                    googleMapsAddress = contactMapAddressInput,
                                    footerCopyright = footerCopyrightInput,
                                    websiteColorPrimary = adminSettings.websiteColorPrimary,
                                    websiteColorSecondary = adminSettings.websiteColorSecondary,
                                    websiteColorBackground = adminSettings.websiteColorBackground,
                                    featuresList = adminSettings.featuresList,
                                    faqsList = adminSettings.faqsList,
                                    testimonialsList = adminSettings.testimonialsList,
                                    sellerRevenuePercent = sellerPct,
                                    affiliateRevenuePercent = affPct,
                                    platformRevenuePercent = platPct,
                                    isCustomerDiscountEnabled = isCustomerDiscountEnabledInput,
                                    customerDiscountPercent = discPct
                                )
                                authViewModel.updateAdminSettings(updated)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Commit Settings Changes", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                3 -> {
                    ReferralProgramAdminPanel(
                        authViewModel = authViewModel,
                        adminSettings = adminSettings,
                        isReferralProgramEnabled = isReferralProgramEnabled,
                        onReferralProgramEnabledChange = { isReferralProgramEnabled = it },
                        referralRewardAmountInput = referralRewardAmountInput,
                        onReferralRewardAmountInputChange = { referralRewardAmountInput = it },
                        grantRewardOnStatusSelection = grantRewardOnStatusSelection,
                        onGrantRewardOnStatusSelectionChange = { grantRewardOnStatusSelection = it }
                    )
                }
                4 -> {
                    SupportTicketsAdminPanel(authViewModel = authViewModel)
                }
                5 -> {
                    MarketplaceAdminPanel(viewModel = wealthViewModel, authViewModel = authViewModel)
                }
                6 -> {
                    CommissionsAndPayoutsAdminPanel(wealthViewModel = wealthViewModel, authViewModel = authViewModel)
                }
            }
        }
    }

    // Dialog 1: User details & actions
    selectedUserForDetail?.let { user ->
        Dialog(onDismissRequest = { selectedUserForDetail = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(user.displayName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(user.email, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "User UID", value = user.uid)
                    DetailRow(label = "Enrolled On", value = formatTimestamp(user.dateCreated))
                    DetailRow(label = "Earning Track", value = user.selectedPath)
                    DetailRow(label = "Goal", value = "₦${user.monthlyGoal}")
                    DetailRow(label = "Saved simulated balance", value = "₦${user.currentSavedBalance}")
                    DetailRow(label = "Admin Status", value = if (user.isAdmin) "Administrator" else "Regular Member")
                    DetailRow(
                        label = "Account Status", 
                        value = user.accountStatus, 
                        valueColor = when (user.accountStatus) {
                            "Approved" -> EmeraldGreenLight
                            "Pending Verification" -> RichGold
                            "Rejected", "Suspended" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )

                    if (user.accountStatus == "Rejected" && user.rejectionReason.isNotEmpty()) {
                        DetailRow(label = "Rejection Feedback", value = user.rejectionReason, valueColor = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Admin actions (Suspend, Reactivate, Delete User)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (user.accountStatus == "Approved") {
                            Button(
                                onClick = { 
                                    authViewModel.suspendUser(user.uid)
                                    selectedUserForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suspend Account")
                            }
                        } else if (user.accountStatus == "Suspended") {
                            Button(
                                onClick = { 
                                    authViewModel.reactivateUser(user.uid)
                                    selectedUserForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reactivate Account")
                            }
                        }

                        // Prevent self-deletion
                        if (!user.isAdmin) {
                            OutlinedButton(
                                onClick = { 
                                    authViewModel.deleteUser(user.uid)
                                    selectedUserForDetail = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Permanently Delete User Profile")
                            }
                        }

                        Button(
                            onClick = { selectedUserForDetail = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close Details")
                        }
                    }
                }
            }
        }
    }

    // Dialog 2: View Payment Proof with simulated OPAY invoice
    selectedUserForProof?.let { user ->
        Dialog(onDismissRequest = { selectedUserForProof = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = "Proof", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Proof Ticket", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        IconButton(onClick = { selectedUserForProof = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Proof File: ${user.paymentProofName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Submitted on: ${formatTimestamp(user.paymentSubmittedTime)}", style = MaterialTheme.typography.bodySmall)
                            Text("Storage Link: ${user.paymentProofUrl}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Simulated OPay Bank Transfer", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Black))
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("OPAY BANK TRANSACTION SUCCESSFUL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF1CB477)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("₦${adminSettings.registrationFee}", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, color = Color.Black))

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptRow(label = "Receiver Name", value = adminSettings.accountName)
                            ReceiptRow(label = "Receiver Account", value = "${adminSettings.accountNumber} (${adminSettings.bankName})")
                            ReceiptRow(label = "Sender Name", value = user.displayName)
                            ReceiptRow(label = "Sender Email", value = user.email)
                            ReceiptRow(label = "Tx Ref ID", value = "REF-${user.uid.take(6).uppercase()}-${user.paymentSubmittedTime % 1000000}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                authViewModel.updateVerificationStatus(user.uid, "Approved")
                                selectedUserForProof = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }

                        Button(
                            onClick = {
                                selectedUserForRejection = user
                                selectedUserForProof = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }

    // Dialog 3: Reject reason Input dialog
    selectedUserForRejection?.let { user ->
        Dialog(onDismissRequest = { selectedUserForRejection = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Rejection Feedback Required", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Provide actionable reason details for ${user.displayName}:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        placeholder = { Text("Receipt ref name mismatch, bank transfer amount short, transfer proof unreadable...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("rejection_reason_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { 
                                selectedUserForRejection = null
                                rejectionReasonInput = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (rejectionReasonInput.isNotBlank()) {
                                    authViewModel.updateVerificationStatus(user.uid, "Rejected", rejectionReasonInput)
                                    selectedUserForRejection = null
                                    rejectionReasonInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = rejectionReasonInput.isNotBlank(),
                            modifier = Modifier.weight(1f).testTag("confirm_rejection_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Reject")
                        }
                    }
                }
            }
        }
    }

    // Dialog 4: Broadcast creation / edit dialog
    if (showCreateAnnDialog) {
        Dialog(onDismissRequest = { showCreateAnnDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (selectedAnnForEdit == null) "Broadcast New Announcement" else "Modify Broadcast Announcement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = annTitleInput,
                        onValueChange = { annTitleInput = it },
                        label = { Text("Announcement Heading") },
                        placeholder = { Text("e.g. New freelancing system blueprints available!") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ann_title_field")
                    )

                    OutlinedTextField(
                        value = annContentInput,
                        onValueChange = { annContentInput = it },
                        label = { Text("Announcement Body Text") },
                        placeholder = { Text("Write complete description logs...") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("ann_content_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pin Announcement on Dashboards", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = annIsPinned,
                            onCheckedChange = { annIsPinned = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showCreateAnnDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (annTitleInput.isNotBlank() && annContentInput.isNotBlank()) {
                                    val edit = selectedAnnForEdit
                                    if (edit != null) {
                                        authViewModel.updateAnnouncement(
                                            edit.copy(title = annTitleInput, content = annContentInput, isPinned = annIsPinned)
                                        )
                                    } else {
                                        authViewModel.createAnnouncement(annTitleInput, annContentInput, annIsPinned)
                                    }
                                    showCreateAnnDialog = false
                                }
                            },
                            enabled = annTitleInput.isNotBlank() && annContentInput.isNotBlank(),
                            modifier = Modifier.weight(1.5f).testTag("ann_save_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Publish Broadcast")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
        }
    }
}

@Composable
fun UserAdminCard(
    user: UserProfile,
    onViewDetail: () -> Unit,
    onViewProof: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.uid}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        maxLines = 1
                    )
                }

                // Colorful Status Badge
                StatusBadge(status = user.accountStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Registered: ${formatTimestamp(user.dateCreated).substringBefore(" ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (user.paymentSubmittedTime > 0L) {
                    Text(
                        "Paid: ${formatTimestamp(user.paymentSubmittedTime).substringBefore(" ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Info", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }

                if (user.accountStatus == "Pending Verification" && user.paymentProofUrl.isNotEmpty()) {
                    Button(
                        onClick = onViewProof,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Review", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Proof", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else if (user.accountStatus == "Pending Verification") {
                    // Manual override option if proof url somehow blank
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (containerColor, contentColor) = when (status) {
        "Approved" -> EmeraldGreenLight.copy(alpha = 0.15f) to EmeraldGreenLight
        "Pending Verification" -> RichGold.copy(alpha = 0.15f) to RichGold
        "Rejected" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f) to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) to MaterialTheme.colorScheme.outline
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold))
        Text(value, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, fontWeight = FontWeight.Black))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralProgramAdminPanel(
    authViewModel: AuthViewModel,
    adminSettings: AdminSettings,
    isReferralProgramEnabled: Boolean,
    onReferralProgramEnabledChange: (Boolean) -> Unit,
    referralRewardAmountInput: String,
    onReferralRewardAmountInputChange: (String) -> Unit,
    grantRewardOnStatusSelection: String,
    onGrantRewardOnStatusSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allReferrals by authViewModel.allReferrals.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Active", "Rejected"

    // Statistics calculations
    val totalReferrals = allReferrals.size
    val activeReferrals = allReferrals.count { it.status == "Active" }
    val pendingReferrals = allReferrals.count { it.status == "Pending" }
    val totalRewardsPaid = allReferrals.filter { it.isRewardGranted }.sumOf { it.rewardAmount }

    // Filter referral records
    val filteredReferrals = allReferrals.filter { record ->
        val referrerName = allUsers.firstOrNull { it.uid == record.referrerUid }?.displayName ?: ""
        val matchesSearch = record.referredDisplayName.contains(searchQuery, ignoreCase = true) ||
                            record.referredEmail.contains(searchQuery, ignoreCase = true) ||
                            referrerName.contains(searchQuery, ignoreCase = true) ||
                            record.referralCode.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (statusFilter) {
            "All" -> true
            "Pending" -> record.status == "Pending"
            "Active" -> record.status == "Active"
            "Rejected" -> record.status == "Rejected"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section 1: Referral Configurator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Referral Reward Program Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Referral Program Status", fontWeight = FontWeight.Bold)
                            Text("Enable or disable referral tracking & rewards", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isReferralProgramEnabled,
                            onCheckedChange = onReferralProgramEnabledChange,
                            modifier = Modifier.testTag("referral_program_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = referralRewardAmountInput,
                        onValueChange = onReferralRewardAmountInputChange,
                        label = { Text("Referral Reward Amount (₦)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("referral_reward_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Choose When Reward is Granted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    val triggerOptions = listOf(
                        "Registration" to "Instantly upon referral registration",
                        "Pending" to "When referred user uploads payment proof",
                        "Approved" to "When referred user's verification is Approved"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        triggerOptions.forEach { (option, description) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGrantRewardOnStatusSelectionChange(option) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = grantRewardOnStatusSelection == option,
                                    onClick = { onGrantRewardOnStatusSelectionChange(option) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(option, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val rewardVal = referralRewardAmountInput.toDoubleOrNull() ?: adminSettings.referralRewardAmount
                            val updated = adminSettings.copy(
                                isReferralProgramEnabled = isReferralProgramEnabled,
                                referralRewardAmount = rewardVal,
                                grantRewardOnStatus = grantRewardOnStatusSelection
                            )
                            authViewModel.updateAdminSettings(updated)
                            Toast.makeText(context, "Referral settings saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Referral Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Referral Analytics
        item {
            Column {
                Text(
                    text = "Referral Analytics Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Referrals",
                        value = totalReferrals.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Rewards",
                        value = activeReferrals.toString(),
                        color = EmeraldGreenLight,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pending Rewards",
                        value = pendingReferrals.toString(),
                        color = RichGold,
                        icon = Icons.Default.HourglassEmpty,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rewards Distributed",
                        value = "₦${totalRewardsPaid.toInt()}",
                        color = EmeraldGreenLight,
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 3: Referral Records Management Header & Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Referral Activity Logs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = {
                        val csv = StringBuilder("Referral ID,Referrer UID,Referrer Name,Referred UID,Referred Name,Referred Email,Status,Date,Reward Amount,Is Reward Granted\n")
                        allReferrals.forEach { r ->
                            val refName = allUsers.firstOrNull { it.uid == r.referrerUid }?.displayName ?: "Unknown"
                            csv.append("${r.id},${r.referrerUid},\"$refName\",${r.referredUid},\"${r.referredDisplayName}\",${r.referredEmail},${r.status},${formatTimestamp(r.dateCreated)},${r.rewardAmount},${r.isRewardGranted}\n")
                        }
                        clipboardManager.setText(AnnotatedString(csv.toString()))
                        Toast.makeText(context, "Exported ${allReferrals.size} records to Clipboard as CSV! Paste into Sheets.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Search and Filter controls for referral records
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, email, or code...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("referral_admin_search")
                )

                val filterOptions = listOf("All", "Pending", "Active", "Rejected")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filterOptions.forEach { filter ->
                        val selected = statusFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { statusFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Referral records listing
        if (filteredReferrals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching referral records found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        } else {
            items(filteredReferrals) { record ->
                val referrer = allUsers.firstOrNull { it.uid == record.referrerUid }
                val referrerName = referrer?.displayName ?: "Unknown Referrer"
                val dateStr = formatTimestamp(record.dateCreated).substringBefore(" ")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Referred: ${record.referredDisplayName}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "By Ambassador: $referrerName",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Referral Code Used: ${record.referralCode}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = when (record.status) {
                                        "Active" -> EmeraldGreenLight.copy(alpha = 0.15f)
                                        "Pending" -> RichGold.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.errorContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = record.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when (record.status) {
                                                "Active" -> EmeraldGreenLight
                                                "Pending" -> RichGold
                                                else -> MaterialTheme.colorScheme.error
                                            }
                                        )
                                    )
                                }
                                if (record.isRewardGranted) {
                                    Text(
                                        text = "₦${record.rewardAmount.toInt()} Paid",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldGreenLight)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Email: ${record.referredEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Enrolled: $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketsAdminPanel(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val tickets by authViewModel.allTickets.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Open", "Replied", "Closed"
    var selectedTicketForChat by remember { mutableStateOf<com.example.data.model.SupportTicket?>(null) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authViewModel.loadAllSupportTickets()
    }

    // Sync selected ticket
    LaunchedEffect(tickets) {
        selectedTicketForChat?.let { active ->
            selectedTicketForChat = tickets.firstOrNull { it.id == active.id }
        }
    }

    val filteredTickets = remember(tickets, searchQuery, statusFilter) {
        tickets.filter { ticket ->
            val matchesSearch = ticket.subject.contains(searchQuery, ignoreCase = true) || 
                                ticket.userName.contains(searchQuery, ignoreCase = true)
            val matchesStatus = statusFilter == "All" || ticket.status.equals(statusFilter, ignoreCase = true)
            matchesSearch && matchesStatus
        }
    }

    if (selectedTicketForChat != null) {
        val ticket = selectedTicketForChat!!
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTicketForChat = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(ticket.subject, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("From: ${ticket.userName} (${ticket.userEmail})", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
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

            // Ticket action row: Close/Reopen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (ticket.status != "Closed") {
                    Button(
                        onClick = { authViewModel.updateSupportTicketStatus(ticket.id, "Closed") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Close Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { authViewModel.updateSupportTicketStatus(ticket.id, "Open") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reopen Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Messages block
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Original Description
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Original Request Details", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(ticket.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                            Text(sdf.format(Date(ticket.dateCreated)), style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        }
                    }

                    // Replies
                    ticket.messages.forEach { msg ->
                        val isMe = msg.senderUid == (currentUser?.uid ?: "")
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                                            text = if (isMe) "You (Admin)" else msg.senderName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37)
                                            )
                                        )
                                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                        Text(sdf.format(Date(msg.timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(msg.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Reply footer
            if (ticket.status != "Closed") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Write advisor response here...") },
                        modifier = Modifier.weight(1f).testTag("admin_reply_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                authViewModel.replyToSupportTicket(
                                    ticketId = ticket.id,
                                    senderUid = currentUser?.uid ?: "admin",
                                    senderName = currentUser?.displayName ?: "Lead Advisor",
                                    messageText = replyText
                                )
                                replyText = ""
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp).testTag("admin_reply_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    } else {
          Column(
              modifier = modifier
                  .fillMaxSize()
                  .padding(16.dp)
          ) {
              Text("User Support Request Tickets 💬", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
              Spacer(modifier = Modifier.height(12.dp))

              // Filter Controls
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { searchQuery = it },
                      placeholder = { Text("Search by user or subject...") },
                      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                      modifier = Modifier.weight(1.3f),
                      shape = RoundedCornerShape(12.dp),
                      singleLine = true
                  )

                  var showStatusMenu by remember { mutableStateOf(false) }
                  Box(modifier = Modifier.weight(1f)) {
                      Button(
                          onClick = { showStatusMenu = true },
                          shape = RoundedCornerShape(12.dp),
                          modifier = Modifier.fillMaxWidth().height(56.dp),
                          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                      ) {
                          Text(statusFilter, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                          Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                      }
                      DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                          listOf("All", "Open", "Replied", "Closed").forEach { status ->
                              DropdownMenuItem(
                                  text = { Text(status) },
                                  onClick = {
                                      statusFilter = status
                                      showStatusMenu = false
                                  }
                              )
                          }
                      }
                  }
              }

              Spacer(modifier = Modifier.height(16.dp))

              if (filteredTickets.isEmpty()) {
                  Card(
                      modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                      colors = CardDefaults.cardColors(containerColor = Color.White)
                  ) {
                      Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                          Text("No tickets found matching the filter constraints.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                      }
                  }
              } else {
                  LazyColumn(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                      items(filteredTickets) { ticket ->
                          Card(
                              modifier = Modifier
                                  .fillMaxWidth()
                                  .clickable { selectedTicketForChat = ticket },
                              colors = CardDefaults.cardColors(containerColor = Color.White),
                              shape = RoundedCornerShape(16.dp),
                              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                          ) {
                              Row(
                                  modifier = Modifier.padding(16.dp),
                                  verticalAlignment = Alignment.CenterVertically
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
                                      Text("User: ${ticket.userName} (${ticket.userEmail})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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

// --- MARKETPLACE ADMINISTRATION CONTROL CENTER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAdminPanel(viewModel: WealthViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val products by viewModel.products.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val users by authViewModel.allUsers.collectAsState()
    val adminSettings by authViewModel.adminSettings.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAllOrders()
        viewModel.loadMarketplaceData()
    }
    
    var adminSubTab by remember { mutableStateOf(0) } // 0 = Active Offers, 1 = Review Queue, 2 = Platform Orders
    
    // Dialog control states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<com.example.data.model.AffiliateProduct?>(null) }
    var showRejectDialogForProductId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    
    // Forms states
    var pName by remember { mutableStateOf("") }
    var pBrand by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf("Tech & Gadgets") }
    var pPrice by remember { mutableStateOf("") }
    var pAffLink by remember { mutableStateOf("") }
    var pMerchant by remember { mutableStateOf("Amazon") }
    var pImageUrl by remember { mutableStateOf("") }
    var pCommission by remember { mutableStateOf("40") }

    // Partners Config States
    var selectedPartnerForConfig by remember { mutableStateOf<com.example.data.model.AffiliatePartner?>(null) }
    var configTrackingId by remember { mutableStateOf("") }
    var configCommissionRate by remember { mutableStateOf("") }
    var configApiKey by remember { mutableStateOf("") }
    var configApiSecret by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section 1: KPI Analytics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Marketplace BI Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val viewsCount = analytics?.totalProductViews ?: 124
                    val clicksCount = analytics?.totalAffiliateClicks ?: 38
                    val ctr = if (viewsCount > 0) (clicksCount.toDouble() / viewsCount * 100) else 0.0
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Views", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(viewsCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Clicks", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(clicksCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = Color(0xFF0F9D58).copy(alpha = 0.08f).let { CardDefaults.cardColors(containerColor = it) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Platform CTR", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(String.format("%.1f%%", ctr), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
                        }
                    }
                }
            }
        }

        // Section 2: Inner Panel Sub-tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = adminSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = adminSubTab == 0,
                    onClick = { adminSubTab = 0 },
                    text = { Text("Offers", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                val pendingCount = products.filter { it.status == "Pending" || it.status == "Pending Review" }.size
                Tab(
                    selected = adminSubTab == 1,
                    onClick = { adminSubTab = 1 },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Review Queue", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.background(Color.Red, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(pendingCount.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = adminSubTab == 2,
                    onClick = { adminSubTab = 2 },
                    text = { Text("Orders", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = adminSubTab == 3,
                    onClick = { adminSubTab = 3 },
                    text = { Text("Coupons", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                    icon = { Icon(Icons.Default.Discount, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = adminSubTab == 4,
                    onClick = { adminSubTab = 4 },
                    text = { Text("Verifications", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                    icon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = adminSubTab == 5,
                    onClick = { adminSubTab = 5 },
                    text = { Text("Reports Hub", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (adminSubTab == 0) {
            // --- ACTIVE OFFERINGS TAB ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Marketplace Offerings (${products.filter { it.status == "Approved" }.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Button(
                        onClick = {
                            pName = ""
                            pBrand = ""
                            pDesc = ""
                            pCategory = "Tech & Gadgets"
                            pPrice = ""
                            pAffLink = ""
                            pMerchant = "Amazon"
                            pImageUrl = ""
                            pCommission = "40"
                            showAddProductDialog = true
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Offer", fontSize = 11.sp)
                    }
                }
            }

            val approvedProducts = products.filter { it.status == "Approved" }
            if (approvedProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active approved offerings configured.", color = Color.Gray)
                    }
                }
            } else {
                items(approvedProducts) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$${prod.price} via ${prod.merchantName}", fontSize = 11.sp, color = Color.Gray)
                                    if (prod.sellerId.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                            Text("SELLER-LISTED", color = MaterialTheme.colorScheme.secondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    selectedProductForEdit = prod
                                    pName = prod.name
                                    pBrand = prod.brand
                                    pDesc = prod.description
                                    pCategory = prod.category
                                    pPrice = prod.price.toString()
                                    pAffLink = prod.affiliateLink
                                    pMerchant = prod.merchantName
                                    pImageUrl = prod.images.firstOrNull() ?: ""
                                    pCommission = prod.commissionPercent.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    viewModel.deleteProduct(prod.id)
                                    Toast.makeText(context, "Product permanently deleted", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        } else if (adminSubTab == 1) {
            // --- REVIEW QUEUE TAB ---
            item {
                Text(
                    "Review Queue & Product Moderation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val pendingProducts = products.filter { it.status == "Pending" }
            if (pendingProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("All caught up! No pending seller uploads.", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                items(pendingProducts) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prod.brand, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text("Category: ${prod.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(prod.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Selling Price", fontSize = 10.sp, color = Color.Gray)
                                    Text("$${prod.price}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("Available Quantity", fontSize = 10.sp, color = Color.Gray)
                                    Text("${prod.availableQuantity} units", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Affiliate Share", fontSize = 10.sp, color = Color.Gray)
                                    Text("${prod.commissionPercent}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFFF4B400))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.reviewProduct(prod.id, approve = true)
                                        Toast.makeText(context, "Product approved & published! 🚀", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve")
                                }
                                OutlinedButton(
                                    onClick = {
                                        showRejectDialogForProductId = prod.id
                                        rejectionReasonInput = ""
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        } else if (adminSubTab == 2) {
            // --- PLATFORM ORDERS TAB ---
            item {
                Text(
                    "All Platform Orders & Revenue Distributions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No platform orders recorded yet.", color = Color.Gray)
                    }
                }
            } else {
                items(orders.sortedByDescending { orderItem -> orderItem.dateCreated }) { order ->
                    AdminOrderCard(order = order, viewModel = viewModel)
                }
            }
        } else if (adminSubTab == 3) {
            item {
                CouponsAdminSection(viewModel = viewModel)
            }
        } else if (adminSubTab == 4) {
            item {
                SellerVerificationsAdminSection(viewModel = viewModel, authViewModel = authViewModel)
            }
        } else if (adminSubTab == 5) {
            item {
                ReportsGeneratorSection(
                    viewModel = viewModel,
                    orders = orders,
                    allUsers = users,
                    adminSettings = adminSettings
                )
            }
        }
    }

    // --- REJECTION MODAL ---
    showRejectDialogForProductId?.let { pid ->
        AlertDialog(
            onDismissRequest = { showRejectDialogForProductId = null },
            title = { Text("Rejection Reason Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Provide descriptive feedback so the seller can edit and re-submit this product.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Reason for Rejection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReasonInput.isNotEmpty()) {
                            viewModel.reviewProduct(pid, approve = false, rejectionReason = rejectionReasonInput)
                            showRejectDialogForProductId = null
                            Toast.makeText(context, "Product rejected and seller notified.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a reason.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialogForProductId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- DIALOG 1: ADD PRODUCT FORM ---
    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add New Affiliate Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth().testTag("form_name"))
                    OutlinedTextField(value = pBrand, onValueChange = { pBrand = it }, label = { Text("Brand/Owner") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = pCategory, onValueChange = { pCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Price ($)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pAffLink, onValueChange = { pAffLink = it }, label = { Text("Affiliate URL Link") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pImageUrl, onValueChange = { pImageUrl = it }, label = { Text("Image URL Path") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pMerchant, onValueChange = { pMerchant = it }, label = { Text("Merchant (e.g. Amazon)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCommission, onValueChange = { pCommission = it }, label = { Text("Commission (%)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = { showAddProductDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                val fresh = com.example.data.model.AffiliateProduct(
                                    id = "prod_" + java.util.UUID.randomUUID().toString().take(8),
                                    name = pName,
                                    brand = pBrand,
                                    description = pDesc,
                                    category = pCategory,
                                    price = pPrice.toDoubleOrNull() ?: 0.0,
                                    affiliateLink = pAffLink,
                                    images = if (pImageUrl.isNotEmpty()) listOf(pImageUrl) else emptyList(),
                                    merchantName = pMerchant,
                                    commissionPercent = pCommission.toDoubleOrNull() ?: 40.0,
                                    status = "Approved" // Admin bypass
                                )
                                viewModel.saveProduct(fresh)
                                showAddProductDialog = false
                                Toast.makeText(context, "Product saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f).testTag("save_product_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Publish Asset")
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 2: EDIT PRODUCT FORM ---
    selectedProductForEdit?.let { original ->
        Dialog(onDismissRequest = { selectedProductForEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Modify Affiliate Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth().testTag("edit_form_name"))
                    OutlinedTextField(value = pBrand, onValueChange = { pBrand = it }, label = { Text("Brand/Owner") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = pCategory, onValueChange = { pCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Price ($)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pAffLink, onValueChange = { pAffLink = it }, label = { Text("Affiliate URL Link") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pImageUrl, onValueChange = { pImageUrl = it }, label = { Text("Image URL Path") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pMerchant, onValueChange = { pMerchant = it }, label = { Text("Merchant") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCommission, onValueChange = { pCommission = it }, label = { Text("Commission (%)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = { selectedProductForEdit = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val updated = original.copy(
                                    name = pName,
                                    brand = pBrand,
                                    description = pDesc,
                                    category = pCategory,
                                    price = pPrice.toDoubleOrNull() ?: original.price,
                                    affiliateLink = pAffLink,
                                    images = if (pImageUrl.isNotEmpty()) listOf(pImageUrl) else original.images,
                                    merchantName = pMerchant,
                                    commissionPercent = pCommission.toDoubleOrNull() ?: original.commissionPercent
                                )
                                viewModel.saveProduct(updated)
                                selectedProductForEdit = null
                                Toast.makeText(context, "Product successfully edited!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 3: CONFIGURE PARTNER DIALOG ---
    selectedPartnerForConfig?.let { originalPartner ->
        Dialog(onDismissRequest = { selectedPartnerForConfig = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configure ${originalPartner.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Provide secure API tracking tags.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = configTrackingId,
                        onValueChange = { configTrackingId = it },
                        label = { Text("Tracking Tag (e.g. tracking-20)") },
                        modifier = Modifier.fillMaxWidth().testTag("partner_tracking_id")
                    )
                    OutlinedTextField(
                        value = configCommissionRate,
                        onValueChange = { configCommissionRate = it },
                        label = { Text("Default Commission Rate (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = configApiKey,
                        onValueChange = { configApiKey = it },
                        label = { Text("API Access Key ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = configApiSecret,
                        onValueChange = { configApiSecret = it },
                        label = { Text("API Secret Key") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedPartnerForConfig = null }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                val updated = originalPartner.copy(
                                    trackingId = configTrackingId,
                                    commissionRate = configCommissionRate.toDoubleOrNull() ?: originalPartner.commissionRate,
                                    apiKey = configApiKey,
                                    apiSecret = configApiSecret
                                )
                                viewModel.savePartner(updated)
                                selectedPartnerForConfig = null
                                Toast.makeText(context, "Partner settings committed!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f).testTag("save_partner_config")
                        ) {
                            Text("Commit Settings")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAdminPanelOld(viewModel: WealthViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val products by viewModel.products.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val orders by viewModel.orders.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAllOrders()
        viewModel.loadMarketplaceData()
    }
    
    var adminSubTab by remember { mutableStateOf(0) } // 0 = Offerings, 1 = Pending Reviews, 2 = Platform Orders
    var showRejectDialogForProductId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    
    // Dialog control states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<com.example.data.model.AffiliateProduct?>(null) }
    
    // Importer states
    var importAsin by remember { mutableStateOf("") }
    var importCategory by remember { mutableStateOf("Tech & Gadgets") }
    var isImporting by remember { mutableStateOf(false) }

    // Forms states
    var pName by remember { mutableStateOf("") }
    var pBrand by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf("Tech & Gadgets") }
    var pSubcategory by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pDiscPrice by remember { mutableStateOf("") }
    var pAffLink by remember { mutableStateOf("") }
    var pMerchant by remember { mutableStateOf("Amazon") }
    var pStock by remember { mutableStateOf("In Stock") }
    var pPartnerId by remember { mutableStateOf("amazon_associates") }
    var pFeatured by remember { mutableStateOf(false) }
    var pTrending by remember { mutableStateOf(false) }
    var pRecommended by remember { mutableStateOf(false) }
    var pImageUrl by remember { mutableStateOf("") }
    var pCommission by remember { mutableStateOf("40") }

    // Partners Config States
    var selectedPartnerForConfig by remember { mutableStateOf<com.example.data.model.AffiliatePartner?>(null) }
    var configTrackingId by remember { mutableStateOf("") }
    var configCommissionRate by remember { mutableStateOf("") }
    var configApiKey by remember { mutableStateOf("") }
    var configApiSecret by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section 1: Marketplace KPI Performance Dashboard
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Marketplace BI Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val viewsCount = analytics?.totalProductViews ?: 124
                    val clicksCount = analytics?.totalAffiliateClicks ?: 38
                    val ctr = if (viewsCount > 0) (clicksCount.toDouble() / viewsCount * 100) else 0.0
                    
                    StatCard(
                        title = "Catalog Assets",
                        value = products.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.Inventory,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Click-Throughs",
                        value = clicksCount.toString(),
                        color = Color(0xFF0F9D58), // Green
                        icon = Icons.Default.AdsClick,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Conversion CTR",
                        value = String.format("%.1f%%", ctr),
                        color = Color(0xFFF4B400), // Gold
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 2: Amazon Product Advertising API Instant Secure Importer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SystemUpdateAlt, contentDescription = null, tint = Color(0xFFF4B400))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Amazon PA-API Real-time Importer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Secure server-side handshake. Automatically embeds affiliate tracking tags into imported product metadata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = importAsin,
                        onValueChange = { importAsin = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amazon_asin_input"),
                        label = { Text("Amazon ASIN (Standard Identification Number)") },
                        placeholder = { Text("e.g. B09XS7JWHH or B0CM5N1ZNW") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (importAsin.isBlank()) {
                                    Toast.makeText(context, "Please enter an ASIN first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isImporting = true
                                viewModel.importAmazonProduct(
                                    asin = importAsin,
                                    category = importCategory,
                                    onSuccess = { msg ->
                                        isImporting = false
                                        importAsin = ""
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    },
                                    onFailure = { err ->
                                        isImporting = false
                                        Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isImporting,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("import_asin_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Handshake & Import")
                            }
                        }
                        
                        // Presets help
                        IconButton(
                            onClick = {
                                importAsin = listOf("B09XS7JWHH", "B0CM5N1ZNW", "B0C78F7229", "B0CV181Z8B").random()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Randomize ASIN template")
                        }
                    }
                }
            }
        }

        // Section 3: Partner Network Configuration Hub
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Affiliate Partner Credentials Manager",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                partners.forEach { partner ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partner.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Tracking Tag: ${partner.trackingId.ifEmpty { "Default: wealthb-20" }}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            
                            Button(
                                onClick = {
                                    selectedPartnerForConfig = partner
                                    configTrackingId = partner.trackingId
                                    configCommissionRate = partner.commissionRate.toString()
                                    configApiKey = partner.apiKey
                                    configApiSecret = partner.apiSecret
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.SettingsSuggest, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Configure", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Physical & Digital Assets Inventory Catalog
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Active Affiliate Offerings (${products.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Button(
                    onClick = {
                        pName = ""
                        pBrand = ""
                        pDesc = ""
                        pCategory = "Tech & Gadgets"
                        pSubcategory = ""
                        pPrice = ""
                        pDiscPrice = ""
                        pAffLink = ""
                        pMerchant = "Amazon"
                        pStock = "In Stock"
                        pFeatured = false
                        pTrending = false
                        pRecommended = false
                        pImageUrl = ""
                        showAddProductDialog = true
                    },
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Offer")
                }
            }
        }

        // Product Rows Table list
        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No offerings configured in the database yet.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(products) { prod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (prod.isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                if (prod.isFeatured) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.background(Color(0xFFF4B400), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text("FEATURED", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (prod.isArchived) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.background(Color.Gray, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text("ARCHIVED", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("$${prod.price} via ${prod.merchantName} (${prod.category})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Edit
                            IconButton(onClick = {
                                selectedProductForEdit = prod
                                pName = prod.name
                                pBrand = prod.brand
                                pDesc = prod.description
                                pCategory = prod.category
                                pSubcategory = prod.subcategory
                                pPrice = prod.price.toString()
                                pDiscPrice = prod.discountPrice?.toString() ?: ""
                                pAffLink = prod.affiliateLink
                                pMerchant = prod.merchantName
                                pStock = prod.stockStatus
                                pFeatured = prod.isFeatured
                                pTrending = prod.isTrending
                                pRecommended = prod.isRecommended
                                pImageUrl = prod.images.firstOrNull() ?: ""
                                pCommission = prod.commissionPercent.toString()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Asset", tint = MaterialTheme.colorScheme.primary)
                            }
                            
                            // Duplicate
                            IconButton(onClick = {
                                viewModel.duplicateProduct(prod)
                                Toast.makeText(context, "Successfully duplicated: ${prod.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Asset", tint = Color(0xFFF4B400))
                            }

                            // Archive Toggle
                            IconButton(onClick = {
                                val updated = prod.copy(isArchived = !prod.isArchived)
                                viewModel.saveProduct(updated)
                                Toast.makeText(context, if (updated.isArchived) "Asset Archived" else "Asset Activated", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = if (prod.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                    contentDescription = "Archive Toggle",
                                    tint = Color.DarkGray
                                )
                            }
                            
                            // Delete
                            IconButton(onClick = {
                                viewModel.deleteProduct(prod.id)
                                Toast.makeText(context, "Asset permanently deleted", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Asset", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 1: ADD PRODUCT FORM ---
    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add New Affiliate Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth().testTag("form_name"))
                    OutlinedTextField(value = pBrand, onValueChange = { pBrand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(value = pCategory, onValueChange = { pCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Regular Price ($)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDiscPrice, onValueChange = { pDiscPrice = it }, label = { Text("Discount Price ($) (Optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCommission, onValueChange = { pCommission = it }, label = { Text("Affiliate/Referral Commission (%)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("40") })
                    OutlinedTextField(value = pAffLink, onValueChange = { pAffLink = it }, label = { Text("Affiliate URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pMerchant, onValueChange = { pMerchant = it }, label = { Text("Merchant Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pImageUrl, onValueChange = { pImageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Featured Offer", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pFeatured, onCheckedChange = { pFeatured = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Trending Asset", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pTrending, onCheckedChange = { pTrending = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recommended for Users", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pRecommended, onCheckedChange = { pRecommended = it })
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showAddProductDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (pName.isBlank() || pPrice.toDoubleOrNull() == null || pAffLink.isBlank()) {
                                    Toast.makeText(context, "Fill in valid Name, Price, and Link", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val fresh = com.example.data.model.AffiliateProduct(
                                    id = UUID.randomUUID().toString(),
                                    name = pName,
                                    brand = pBrand,
                                    description = pDesc,
                                    category = pCategory,
                                    price = pPrice.toDoubleOrNull() ?: 10.0,
                                    discountPrice = pDiscPrice.toDoubleOrNull(),
                                    affiliateLink = pAffLink,
                                    merchantName = pMerchant,
                                    stockStatus = pStock,
                                    images = if (pImageUrl.isNotEmpty()) listOf(pImageUrl) else listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"),
                                    isFeatured = pFeatured,
                                    isTrending = pTrending,
                                    isRecommended = pRecommended,
                                    dateAdded = System.currentTimeMillis(),
                                    lastUpdated = System.currentTimeMillis(),
                                    commissionPercent = pCommission.toDoubleOrNull() ?: 40.0
                                )
                                viewModel.saveProduct(fresh)
                                showAddProductDialog = false
                                Toast.makeText(context, "Product saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f).testTag("save_product_btn")
                        ) {
                            Text("Save Offer")
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 2: EDIT PRODUCT FORM ---
    selectedProductForEdit?.let { original ->
        Dialog(onDismissRequest = { selectedProductForEdit = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Modify Affiliate Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth().testTag("edit_form_name"))
                    OutlinedTextField(value = pBrand, onValueChange = { pBrand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(value = pCategory, onValueChange = { pCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Regular Price ($)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDiscPrice, onValueChange = { pDiscPrice = it }, label = { Text("Discount Price ($) (Optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCommission, onValueChange = { pCommission = it }, label = { Text("Affiliate/Referral Commission (%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pAffLink, onValueChange = { pAffLink = it }, label = { Text("Affiliate URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pMerchant, onValueChange = { pMerchant = it }, label = { Text("Merchant Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pImageUrl, onValueChange = { pImageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Featured Offer", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pFeatured, onCheckedChange = { pFeatured = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Trending Asset", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pTrending, onCheckedChange = { pTrending = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recommended for Users", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = pRecommended, onCheckedChange = { pRecommended = it })
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedProductForEdit = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (pName.isBlank() || pPrice.toDoubleOrNull() == null || pAffLink.isBlank()) {
                                    Toast.makeText(context, "Fill in valid Name, Price, and Link", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val updated = original.copy(
                                    name = pName,
                                    brand = pBrand,
                                    description = pDesc,
                                    category = pCategory,
                                    price = pPrice.toDoubleOrNull() ?: original.price,
                                    discountPrice = pDiscPrice.toDoubleOrNull(),
                                    affiliateLink = pAffLink,
                                    merchantName = pMerchant,
                                    stockStatus = pStock,
                                    images = if (pImageUrl.isNotEmpty()) listOf(pImageUrl) else original.images,
                                    isFeatured = pFeatured,
                                    isTrending = pTrending,
                                    isRecommended = pRecommended,
                                    lastUpdated = System.currentTimeMillis(),
                                    commissionPercent = pCommission.toDoubleOrNull() ?: original.commissionPercent
                                )
                                viewModel.saveProduct(updated)
                                selectedProductForEdit = null
                                Toast.makeText(context, "Product successfully edited!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f).testTag("edit_save_btn")
                        ) {
                            Text("Commit Changes")
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 3: CONFIGURE PARTNER DIALOG ---
    selectedPartnerForConfig?.let { originalPartner ->
        Dialog(onDismissRequest = { selectedPartnerForConfig = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configure ${originalPartner.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Provide secure API tracking tags and credential handshakes.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = configTrackingId,
                        onValueChange = { configTrackingId = it },
                        label = { Text("Tracking Tag (e.g. tracking-20)") },
                        modifier = Modifier.fillMaxWidth().testTag("partner_tracking_id")
                    )
                    OutlinedTextField(
                        value = configCommissionRate,
                        onValueChange = { configCommissionRate = it },
                        label = { Text("Default Commission Rate (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = configApiKey,
                        onValueChange = { configApiKey = it },
                        label = { Text("API Access Key ID (PA-API 5.0)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = configApiSecret,
                        onValueChange = { configApiSecret = it },
                        label = { Text("API Secret Key") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedPartnerForConfig = null }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                val updated = originalPartner.copy(
                                    trackingId = configTrackingId,
                                    commissionRate = configCommissionRate.toDoubleOrNull() ?: originalPartner.commissionRate,
                                    apiKey = configApiKey,
                                    apiSecret = configApiSecret
                                )
                                viewModel.savePartner(updated)
                                selectedPartnerForConfig = null
                                Toast.makeText(context, "Partner settings securely committed!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.5f).testTag("save_partner_config")
                        ) {
                            Text("Commit Settings")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionsAndPayoutsAdminPanel(
    wealthViewModel: WealthViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        wealthViewModel.loadAdminReferralsAndWithdrawals()
    }

    val allSales by wealthViewModel.allReferralSales.collectAsState()
    val allWithdrawals by wealthViewModel.allWithdrawals.collectAsState()

    var selectedSection by remember { mutableStateOf(0) } // 0 = Referral Sales, 1 = Payout/Withdrawal Requests

    var rejectionSaleId by remember { mutableStateOf<String?>(null) }
    var rejectionSaleReason by remember { mutableStateOf("") }

    var approvalWdId by remember { mutableStateOf<String?>(null) }
    var approvalWdTxHash by remember { mutableStateOf("") }

    var rejectionWdId by remember { mutableStateOf<String?>(null) }
    var rejectionWdReason by remember { mutableStateOf("") }

    val pendingSales = allSales.filter { it.status == "Pending Approval" }
    val processedSales = allSales.filter { it.status != "Pending Approval" }

    val pendingWds = allWithdrawals.filter { it.status == "Pending Approval" }
    val processedWds = allWithdrawals.filter { it.status != "Pending Approval" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Commissions & Payout Center",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Section Selector Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Referral Sales (${pendingSales.size} Pending)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Withdrawals (${pendingWds.size} Pending)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedSection == 0) {
            // --- REFERRAL SALES SECTION ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Pending Sales Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (pendingSales.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "No pending referral sales currently require validation.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(pendingSales) { sale ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sale.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Referrer: ${sale.referrerEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Text("Buyer: ${sale.buyerName} (${sale.buyerEmail})", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+$${String.format("%.2f", sale.commissionEarned)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldGreenLight
                                        )
                                        Text(
                                            text = "Sale price: $${String.format("%.2f", sale.salePrice)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Proof Reference: ${sale.paymentReference}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            wealthViewModel.approveReferralSale(sale.id) { success ->
                                                if (success) {
                                                    authViewModel.reloadProfile()
                                                    Toast.makeText(context, "Referral sale approved and commission awarded!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to approve sale", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                                        modifier = Modifier.weight(1f).testTag("approve_sale_${sale.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            rejectionSaleId = sale.id
                                            rejectionSaleReason = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f).testTag("reject_sale_${sale.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Processed Referral History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (processedSales.isEmpty()) {
                    item {
                        Text(
                            text = "No previously processed referral transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(processedSales) { sale ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(sale.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Referrer: ${sale.referrerEmail}", style = MaterialTheme.typography.bodySmall)
                                        Text("Buyer: ${sale.buyerName}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        val statusColor = when (sale.status) {
                                            "Completed" -> EmeraldGreenLight
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                        Text(
                                            text = sale.status,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", sale.commissionEarned)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }
                                if (sale.status == "Rejected" && sale.rejectionReason.isNotEmpty()) {
                                    Text(
                                        text = "Reason: ${sale.rejectionReason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- WITHDRAWALS SECTION ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Pending Withdrawal Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (pendingWds.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "No pending payouts requested.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(pendingWds) { wd ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text("Payout via ${wd.payoutMethod}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("User: ${wd.userEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Text("Payout Account Details:\n${wd.payoutDetails}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    Text(
                                        text = "$${String.format("%.2f", wd.amount)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            approvalWdId = wd.id
                                            approvalWdTxHash = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                                        modifier = Modifier.weight(1f).testTag("approve_wd_${wd.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Complete Payout", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            rejectionWdId = wd.id
                                            rejectionWdReason = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f).testTag("reject_wd_${wd.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject & Refund", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Processed Payouts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (processedWds.isEmpty()) {
                    item {
                        Text(
                            text = "No processed payouts in history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(processedWds) { wd ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Amount: $${String.format("%.2f", wd.amount)} via ${wd.payoutMethod}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Details: ${wd.payoutDetails}", style = MaterialTheme.typography.bodySmall)
                                        if (wd.transactionHash.isNotEmpty()) {
                                            Text("Tx Hash / Note: ${wd.transactionHash}", style = MaterialTheme.typography.labelSmall, color = EmeraldGreenLight, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    val statusColor = when (wd.status) {
                                        "Approved" -> EmeraldGreenLight
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                    Text(
                                        text = wd.status,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. REJECT REFERRAL SALE DIALOG
    rejectionSaleId?.let { saleId ->
        AlertDialog(
            onDismissRequest = { rejectionSaleId = null },
            title = { Text("Reject Referral Sale", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please specify why you are rejecting this referral transaction. The user will see this reason in their wallet dashboard.")
                    OutlinedTextField(
                        value = rejectionSaleReason,
                        onValueChange = { rejectionSaleReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth().testTag("reject_sale_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionSaleReason.isBlank()) {
                            Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        wealthViewModel.rejectReferralSale(saleId, rejectionSaleReason) { success ->
                            if (success) {
                                rejectionSaleId = null
                                Toast.makeText(context, "Referral sale rejected successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to reject sale", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_reject_sale_btn")
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectionSaleId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. APPROVE PAYOUT (COMPLETE WITHDRAWAL) DIALOG
    approvalWdId?.let { wdId ->
        AlertDialog(
            onDismissRequest = { approvalWdId = null },
            title = { Text("Complete Withdrawal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter transaction hash, reference, or payment note as proof of payout to the user:")
                    OutlinedTextField(
                        value = approvalWdTxHash,
                        onValueChange = { approvalWdTxHash = it },
                        label = { Text("Transaction Hash / Proof Details") },
                        modifier = Modifier.fillMaxWidth().testTag("approve_wd_tx_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (approvalWdTxHash.isBlank()) {
                            Toast.makeText(context, "Please enter payout reference", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        wealthViewModel.approveWithdrawal(wdId, approvalWdTxHash) { success ->
                            if (success) {
                                approvalWdId = null
                                authViewModel.reloadProfile()
                                Toast.makeText(context, "Withdrawal approved and marked complete!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to approve withdrawal", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_approve_wd_btn")
                ) {
                    Text("Mark as Paid")
                }
            },
            dismissButton = {
                TextButton(onClick = { approvalWdId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. REJECT PAYOUT DIALOG
    rejectionWdId?.let { wdId ->
        AlertDialog(
            onDismissRequest = { rejectionWdId = null },
            title = { Text("Reject Payout & Refund User", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to reject this payout? This will automatically refund the full requested withdrawal amount back to the user's affiliate balance.")
                    OutlinedTextField(
                        value = rejectionWdReason,
                        onValueChange = { rejectionWdReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth().testTag("reject_wd_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionWdReason.isBlank()) {
                            Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        wealthViewModel.rejectWithdrawal(wdId, rejectionWdReason) { success ->
                            if (success) {
                                rejectionWdId = null
                                authViewModel.reloadProfile()
                                Toast.makeText(context, "Payout rejected and balance refunded!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to reject withdrawal", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_reject_wd_btn")
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectionWdId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- SUB-PANEL COMPOSABLES FOR MARKETPLACE ADMIN TAB ---

@Composable
fun CouponsAdminSection(viewModel: WealthViewModel) {
    val context = LocalContext.current
    val coupons by viewModel.coupons.collectAsState()
    
    var code by remember { mutableStateOf("") }
    var discountVal by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("Percentage") }
    var limit by remember { mutableStateOf("50") }
    var expiryDays by remember { mutableStateOf("7") }
    
    LaunchedEffect(Unit) {
        viewModel.loadCoupons()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Create Promotional Coupon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Coupon Code") },
                placeholder = { Text("e.g. LAUNCH50") },
                modifier = Modifier.fillMaxWidth().testTag("coupon_code_input")
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = discountVal,
                    onValueChange = { discountVal = it },
                    label = { Text("Discount Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("coupon_value_input")
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)
                    ) {
                        Text(discountType)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Percentage (%)") }, onClick = { discountType = "Percentage"; expanded = false })
                        DropdownMenuItem(text = { Text("Fixed Amount ($)") }, onClick = { discountType = "Fixed"; expanded = false })
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Usage Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("coupon_limit_input")
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it },
                    label = { Text("Expiry (Days from now)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("coupon_expiry_days_input")
                )
            }
            
            Button(
                onClick = {
                    val dv = discountVal.toDoubleOrNull() ?: 0.0
                    val lim = limit.toIntOrNull() ?: 50
                    val days = expiryDays.toIntOrNull() ?: 7
                    
                    if (code.isBlank() || dv <= 0.0) {
                        Toast.makeText(context, "Please specify valid Code and Discount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val expiryTime = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
                    val newCoupon = com.example.data.model.Coupon(
                        code = code.trim(),
                        discountType = discountType,
                        discountValue = dv,
                        expiryDate = expiryTime,
                        usageLimit = lim,
                        usageCount = 0
                    )
                    viewModel.createCoupon(newCoupon) {
                        Toast.makeText(context, "Coupon created successfully! 🎉", Toast.LENGTH_SHORT).show()
                        code = ""
                        discountVal = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("create_coupon_btn")
            ) {
                Text("Generate Coupon Code")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Active Coupon Promotions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    
    if (coupons.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No active coupons found.", color = Color.Gray)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            coupons.forEach { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(coupon.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (coupon.discountType == "Percentage") "${coupon.discountValue}% OFF" else "$${coupon.discountValue} OFF",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F9D58)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Usage: ${coupon.usageCount}/${coupon.usageLimit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            val expDateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(coupon.expiryDate))
                            Text("Expires: $expDateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        
                        IconButton(
                            onClick = {
                                viewModel.deleteCoupon(coupon.code) {
                                    Toast.makeText(context, "Coupon removed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("delete_coupon_${coupon.code}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Coupon", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerVerificationsAdminSection(viewModel: WealthViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val users by authViewModel.allUsers.collectAsState()
    
    val pendingSellers = remember(users) {
        users.filter { it.isSeller && it.sellerVerificationStatus == "Pending Verification" }
    }
    
    Text(
        text = "Pending Storefront Verifications (${pendingSellers.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    if (pendingSellers.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Verification queue is empty!", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            pendingSellers.forEach { seller ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(seller.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("Business: ${seller.sellerBusinessName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF4B400).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("PENDING", color = Color(0xFFF4B400), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        
                        Text("Phone: ${seller.sellerPhoneNumber}", style = MaterialTheme.typography.bodySmall)
                        Text("National ID: ${seller.sellerNationalId.ifEmpty { "Not Provided" }}", style = MaterialTheme.typography.bodySmall)
                        Text("Business Reg: ${seller.sellerBusinessRegistration.ifEmpty { "Not Provided" }}", style = MaterialTheme.typography.bodySmall)
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateSellerVerificationStatus(seller.uid, "Rejected") {
                                        authViewModel.fetchAllUsers()
                                        Toast.makeText(context, "Seller verification rejected.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("reject_seller_${seller.uid}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject")
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.updateSellerVerificationStatus(seller.uid, "Verified") {
                                        authViewModel.fetchAllUsers()
                                        Toast.makeText(context, "Seller verified! Verified Badge active. 🌟", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("approve_seller_${seller.uid}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsGeneratorSection(
    viewModel: WealthViewModel,
    orders: List<com.example.data.model.Order>,
    allUsers: List<com.example.data.model.UserProfile>,
    adminSettings: com.example.data.model.AdminSettings
) {
    val context = LocalContext.current
    
    val totalSales = orders.sumOf { it.finalPayableAmount }
    val totalOriginal = orders.sumOf { it.originalPrice }
    
    val totalSellerEarnings = orders.sumOf { order ->
        if (order.sellerId.isNotEmpty()) order.finalPayableAmount * (adminSettings.sellerRevenuePercent / 100.0) else 0.0
    }
    val totalAffiliateEarnings = orders.sumOf { order ->
        if (order.affiliateId.isNotEmpty()) order.finalPayableAmount * (adminSettings.affiliateRevenuePercent / 100.0) else 0.0
    }
    val totalPlatformFees = orders.sumOf { order ->
        val baseAmount = order.finalPayableAmount
        val sellerShare = if (order.sellerId.isNotEmpty()) baseAmount * (adminSettings.sellerRevenuePercent / 100.0) else 0.0
        val affiliateShare = if (order.affiliateId.isNotEmpty()) baseAmount * (adminSettings.affiliateRevenuePercent / 100.0) else 0.0
        baseAmount - sellerShare - affiliateShare
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Business Intelligence Report Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gross Merchandise Value (GMV)", fontSize = 10.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", totalSales)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Platform Royalty", fontSize = 10.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", totalPlatformFees)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Seller Store Profits", fontSize = 10.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", totalSellerEarnings)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Affiliate Marketer Commissions", fontSize = 10.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", totalAffiliateEarnings)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Export Analytical Datasets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportDownloadRow(
            title = "Sales Volume & Transaction History (CSV)",
            description = "Detailed audit trail of all orders, buyer data, coupons used, and delivery tracking details.",
            onDownload = {
                val csvContent = StringBuilder("Order ID,Product,Quantity,Price,Paid Amount,Seller ID,Buyer Email,Status\n")
                orders.forEach {
                    csvContent.append("${it.id},${it.productName.replace(",", " ")},${it.quantity},${it.originalPrice},${it.finalPayableAmount},${it.sellerId},${it.buyerEmail},${it.status}\n")
                }
                shareReportFile(context, "sales_audit_trail.csv", csvContent.toString())
            }
        )
        
        ReportDownloadRow(
            title = "Revenue Share Ledger (CSV)",
            description = "Financial report outlining platform margins, seller revenues, and affiliate commission distributions.",
            onDownload = {
                val csvContent = StringBuilder("Order ID,GMV,Platform Share,Seller Share,Affiliate Commission,Coupon Used\n")
                orders.forEach { order ->
                    val baseAmount = order.finalPayableAmount
                    val sellerShare = if (order.sellerId.isNotEmpty()) baseAmount * (adminSettings.sellerRevenuePercent / 100.0) else 0.0
                    val affiliateShare = if (order.affiliateId.isNotEmpty()) baseAmount * (adminSettings.affiliateRevenuePercent / 100.0) else 0.0
                    val platformShare = baseAmount - sellerShare - affiliateShare
                    csvContent.append("${order.id},${order.finalPayableAmount},$platformShare,$sellerShare,$affiliateShare,${order.couponCode}\n")
                }
                shareReportFile(context, "revenue_ledger.csv", csvContent.toString())
            }
        )
        
        ReportDownloadRow(
            title = "Affiliate Payout Summary (CSV)",
            description = "A listing of commissions earned by marketers, pending approvals, and payment ledger details.",
            onDownload = {
                val csvContent = StringBuilder("Order ID,Product,Affiliate Marketer,Affiliate ID,Commission Earned,Date\n")
                orders.filter { it.affiliateId.isNotEmpty() }.forEach { order ->
                    val baseAmount = order.finalPayableAmount
                    val affiliateShare = baseAmount * (adminSettings.affiliateRevenuePercent / 100.0)
                    if (affiliateShare > 0.0) {
                        val affiliateName = allUsers.firstOrNull { it.uid == order.affiliateId }?.displayName ?: "Affiliate Marketer"
                        csvContent.append("${order.id},${order.productName.replace(",", " ")},$affiliateName,${order.affiliateId},$affiliateShare,${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(order.dateCreated))}\n")
                    }
                }
                shareReportFile(context, "affiliate_commissions_summary.csv", csvContent.toString())
            }
        )
    }
}

fun shareReportFile(context: android.content.Context, filename: String, content: String) {
    try {
        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Wealth Builder Report", content)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, "📊 Report compiled! CSV copied to your Clipboard successfully.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Compiling report: " + e.message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ReportDownloadRow(title: String, description: String, onDownload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDownload,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download Report", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AdminOrderCard(
    order: com.example.data.model.Order,
    viewModel: WealthViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var adminNotesInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val statusColor = when (order.status) {
                    "Completed" -> Color(0xFF0F9D58) // Green
                    "Awaiting Payment" -> Color(0xFFF4B400) // Gold
                    "Pending Verification" -> Color(0xFF8E24AA) // Purple
                    "Processing" -> MaterialTheme.colorScheme.secondary
                    "Cancelled" -> Color.Red
                    else -> Color.Gray
                }
                Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(order.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Product: ${order.productName} (x${order.quantity})", fontWeight = FontWeight.SemiBold)
            Text("Buyer: ${order.buyerName} (${order.buyerEmail})", fontSize = 11.sp, color = Color.Gray)
            Text("Address: ${order.shippingAddress}", fontSize = 11.sp, color = Color.Gray)
            
            // Paid / Distribution Info
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Original Price", fontSize = 9.sp, color = Color.Gray)
                    Text("₦${String.format("%,.2f", order.originalPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Auto Discount", fontSize = 9.sp, color = Color.Gray)
                    Text("-₦${String.format("%,.2f", order.discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
                }
                Column {
                    Text("Paid Amount", fontSize = 9.sp, color = Color.Gray)
                    Text("₦${String.format("%,.2f", order.finalPayableAmount)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Manual Payment details (Proof & Reference)
            if (order.paymentProofUrl.isNotEmpty() || order.paymentReference.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                ) {
                    Column {
                        Text("Submitted Manual Transfer Details", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (order.paymentReference.isNotEmpty()) {
                            Text("Payment Reference: ${order.paymentReference}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        if (order.paymentProofUrl.isNotEmpty()) {
                            Text("Proof URL / Image Name: ${order.paymentProofUrl}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.clickable {
                                Toast.makeText(context, "Proof: ${order.paymentProofUrl}", Toast.LENGTH_LONG).show()
                            })
                            // If it starts with http, show a tiny receipt thumbnail
                            if (order.paymentProofUrl.startsWith("http")) {
                                Spacer(modifier = Modifier.height(6.dp))
                                coil.compose.AsyncImage(
                                    model = order.paymentProofUrl,
                                    contentDescription = "Payment Proof receipt",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            if (order.adminNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current Feedback Notes: ${order.adminNotes}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
            }

            // Admin Actions for Verification
            if (order.status == "Pending Verification") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Verification Decisions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                
                OutlinedTextField(
                    value = adminNotesInput,
                    onValueChange = { adminNotesInput = it },
                    label = { Text("Admin Feedback / Notes") },
                    placeholder = { Text("Reason for approval or why rejection is needed...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.verifyOrderPayment(
                                orderId = order.id,
                                approve = false,
                                adminNotes = adminNotesInput,
                                requestAnotherProof = true
                            ) { success ->
                                isProcessing = false
                                if (success) {
                                    Toast.makeText(context, "Payment Rejected. Buyer requested to upload new proof.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(36.dp),
                        enabled = !isProcessing,
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Request New Proof", fontSize = 11.sp, color = MaterialTheme.colorScheme.onError)
                    }
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.verifyOrderPayment(
                                orderId = order.id,
                                approve = true,
                                adminNotes = adminNotesInput,
                                requestAnotherProof = false
                            ) { success ->
                                isProcessing = false
                                if (success) {
                                    Toast.makeText(context, "Payment Approved! Order is now Processing.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                        modifier = Modifier.weight(1f).height(36.dp),
                        enabled = !isProcessing,
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Approve Payment", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Standard Processing/Shipping/Payout distributions (only if already approved/processing/shipped)
            if (order.status != "Pending Verification" && order.status != "Awaiting Payment" && order.status != "Completed" && order.status != "Cancelled") {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Update Delivery & Distribution Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateOrderStatus(order.id, "Processing") {}
                            Toast.makeText(context, "Order is now Processing", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Process", fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.updateOrderStatus(order.id, "Shipped") {}
                            Toast.makeText(context, "Order is now Shipped", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Ship", fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.updateOrderStatus(order.id, "Completed") {}
                            Toast.makeText(context, "Order Completed & Funds Distributed! 🎉", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.2f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Complete & Pay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
