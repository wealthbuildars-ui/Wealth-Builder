package com.example.ui.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReferralRecord
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSubTab(
    authViewModel: AuthViewModel,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val userReferrals by authViewModel.userReferrals.collectAsState()
    val leaderboard by authViewModel.leaderboard.collectAsState()
    val adminSettings by authViewModel.adminSettings.collectAsState()
    
    var activeInviteTab by remember { mutableStateOf(0) } // 0 = Share & Track, 1 = Leaderboard, 2 = Tips & Rewards

    // Load user referrals and leaderboard
    LaunchedEffect(userProfile.uid) {
        authViewModel.loadUserReferrals(userProfile.uid)
        authViewModel.loadLeaderboard()
        authViewModel.loadAdminSettings()
    }

    val referralLink = "https://wealthbuilder.app/ref?code=${userProfile.referralCode}"
    val shareMessage = "Hey! Join Wealth Builder, pay the activation fee, and start learning legitimate online wealth creation models. Sign up using my referral link: $referralLink"

    val totalReferrals = userReferrals.size
    val activeReferrals = userReferrals.count { it.status == "Active" }
    val pendingReferrals = userReferrals.count { it.status == "Pending" }
    val rewardAmountEarned = userProfile.referralRewardsEarned

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Switcher Tab Row inside Invite friends
        TabRow(
            selectedTabIndex = activeInviteTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = activeInviteTab == 0,
                onClick = { activeInviteTab = 0 },
                text = { Text("Share & Track", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeInviteTab == 1,
                onClick = { activeInviteTab = 1 },
                text = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeInviteTab == 2,
                onClick = { activeInviteTab = 2 },
                text = { Text("Tips & Rewards", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (activeInviteTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Referral Code and Link Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("referral_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "YOUR REFERRAL CODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Beautiful Code Badge
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(userProfile.referralCode))
                                        Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = userProfile.referralCode,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 2.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Code",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "REFERRAL LINK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                OutlinedTextField(
                                    value = referralLink,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(referralLink))
                                            Toast.makeText(context, "Referral link copied!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, "Copy link", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Custom Social Share Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ShareIconButton(
                                        icon = Icons.Default.Share,
                                        label = "WhatsApp",
                                        color = Color(0xFF25D366)
                                    ) {
                                        val pm = context.packageManager
                                        val waIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            setPackage("com.whatsapp")
                                        }
                                        try {
                                            context.startActivity(waIntent)
                                        } catch (e: Exception) {
                                            // Fallback
                                            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            }, "Share with")
                                            context.startActivity(chooser)
                                        }
                                    }

                                    ShareIconButton(
                                        icon = Icons.Default.Share,
                                        label = "Facebook",
                                        color = Color(0xFF1877F2)
                                    ) {
                                        val fbIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            setPackage("com.facebook.katana")
                                        }
                                        try {
                                            context.startActivity(fbIntent)
                                        } catch (e: Exception) {
                                            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            }, "Share with")
                                            context.startActivity(chooser)
                                        }
                                    }

                                    ShareIconButton(
                                        icon = Icons.Default.Share,
                                        label = "Telegram",
                                        color = Color(0xFF0088CC)
                                    ) {
                                        val tgIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            setPackage("org.telegram.messenger")
                                        }
                                        try {
                                            context.startActivity(tgIntent)
                                        } catch (e: Exception) {
                                            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            }, "Share with")
                                            context.startActivity(chooser)
                                        }
                                    }

                                    ShareIconButton(
                                        icon = Icons.Default.Share,
                                        label = "X",
                                        color = Color.Black
                                    ) {
                                        val xIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            setPackage("com.twitter.android")
                                        }
                                        try {
                                            context.startActivity(xIntent)
                                        } catch (e: Exception) {
                                            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                            }, "Share with")
                                            context.startActivity(chooser)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stats Dashboard Grid
                    item {
                        Column {
                            Text(
                                text = "Referral Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MiniInviteStatCard(
                                    title = "Total Invites",
                                    value = totalReferrals.toString(),
                                    icon = Icons.Default.People,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniInviteStatCard(
                                    title = "Active (Approved)",
                                    value = activeReferrals.toString(),
                                    icon = Icons.Default.CheckCircle,
                                    color = EmeraldGreenLight,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MiniInviteStatCard(
                                    title = "Pending Status",
                                    value = pendingReferrals.toString(),
                                    icon = Icons.Default.Pending,
                                    color = RichGold,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniInviteStatCard(
                                    title = "Rewards Earned",
                                    value = "₦${rewardAmountEarned.toInt()}",
                                    icon = Icons.Default.Payments,
                                    color = EmeraldGreenLight,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Click Counter Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Referral Link Clicks",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Number of unique visitors who clicked your link",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                                Text(
                                    text = userProfile.referralLinkClicks.toString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                        }
                    }

                    // Referral History Title
                    item {
                        Text(
                            text = "Referral History Logs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    if (userReferrals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PeopleOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Referrals Registered Yet",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Share your code/link with friends to get started!",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(userReferrals) { record ->
                            ReferralRowItem(record = record)
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            1 -> {
                // Leaderboard view
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = RichGold,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Top Referrers Leaderboard",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = "Rankings of elite ambassadors based on active invites.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    if (leaderboard.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Leaderboard calculations in progress...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(leaderboard) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (entry.displayName == userProfile.displayName) {
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Rank Number / Badge
                                        Surface(
                                            color = when (entry.rank) {
                                                1 -> RichGold
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = CircleShape,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (entry.rank <= 3) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = entry.rank.toString(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column {
                                            Text(
                                                text = entry.displayName,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (entry.displayName == userProfile.displayName) {
                                                        MaterialTheme.colorScheme.secondary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            )
                                            Text(
                                                text = "${entry.referralsCount} active referrals",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = "₦${entry.totalRewards.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldGreenLight
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            2 -> {
                // Tips & Rewards Program Details
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Reward System Configuration",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Referral Program Status", fontWeight = FontWeight.Medium)
                                    Surface(
                                        color = if (adminSettings.isReferralProgramEnabled) EmeraldGreenLight.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (adminSettings.isReferralProgramEnabled) "ACTIVE" else "DISABLED",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (adminSettings.isReferralProgramEnabled) EmeraldGreenLight else MaterialTheme.colorScheme.error
                                            )
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reward Per Approved Referral", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "₦${adminSettings.referralRewardAmount.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGreenLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reward Granted Trigger", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Upon ${adminSettings.grantRewardOnStatus}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    // Strategic Tips Header
                    item {
                        Text(
                            text = "Ambassador Growth Blueprint",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    item {
                        TipItem(
                            step = "1",
                            title = "Share on WhatsApp Status & Groups",
                            desc = "Post your referral link with a screenshot of the courses or earnings. Status updates convert highly since people trust contacts!"
                        )
                    }

                    item {
                        TipItem(
                            step = "2",
                            title = "Highlight the Low Entry Cost",
                            desc = "Mention that the registration activation fee is only ₦5,000 for high-quality, practical business classes that ordinarily cost ₦50,000+."
                        )
                    }

                    item {
                        TipItem(
                            step = "3",
                            title = "Follow Up with Referrals",
                            desc = "Your dashboard shows 'Pending Status' for friends who joined but haven't uploaded receipt proofs. Politely nudge them to complete verification."
                        )
                    }

                    item {
                        TipItem(
                            step = "4",
                            title = "Build an Educational Thread",
                            desc = "Write thread reviews about educational sections of Wealth Builder on X (Twitter) or Facebook to attract organic, high-intent referrals."
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ShareIconButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            border = BorderStroke(1.dp, color)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun MiniInviteStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
                )
            }
        }
    }
}

@Composable
fun ReferralRowItem(record: ReferralRecord) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.dateCreated))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = record.referredDisplayName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = record.referredDisplayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Joined $dateStr",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                // Status Badge
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
                        text = "+₦${record.rewardAmount.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenLight
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TipItem(
    step: String,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}
