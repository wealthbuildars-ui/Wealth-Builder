package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Repository
import com.example.data.model.Article
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    onNavigateToArticle: (Article) -> Unit,
    modifier: Modifier = Modifier,
    wealthViewModel: com.example.ui.WealthViewModel? = null
) {
    val scrollState = rememberScrollState()
    val isFbAvailable by authViewModel.isFirebaseAvailable.collectAsState()

    var editingGoal by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf(userProfile.monthlyGoal.toInt().toString()) }

    val allBadges = listOf(
        "Pioneer" to "Joined the Wealth Builder platform",
        "Earner" to "Earned first simulated income credit",
        "Financial Strategist" to "Passed the financial education evaluation quiz",
        "Wealth Overlord" to "Fully achieved monthly earning progress goals"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User Profile Header Info
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Avatar Circle
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userProfile.displayName.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userProfile.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
        }

        // Custom Goal Configuration Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize Earning Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = {
                            if (editingGoal) {
                                val value = goalInput.toDoubleOrNull() ?: userProfile.monthlyGoal
                                authViewModel.updateProfile(
                                    displayName = userProfile.displayName,
                                    monthlyGoal = value,
                                    currentSavedBalance = userProfile.currentSavedBalance,
                                    selectedPath = userProfile.selectedPath
                                )
                            }
                            editingGoal = !editingGoal
                        },
                        modifier = Modifier.testTag("edit_goal_toggle")
                    ) {
                        Icon(
                            imageVector = if (editingGoal) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (editingGoal) "Save goal" else "Edit goal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (editingGoal) {
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Target Monthly Income ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_edit_input")
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(userProfile.monthlyGoal),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Monthly income milestone target",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        // --- AFFILIATE COMMISSIONS & PAYOUT WALLET ---
        if (wealthViewModel != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(userProfile.uid) {
                wealthViewModel.loadReferralData(userProfile.uid)
                wealthViewModel.loadMarketplaceData(userProfile.uid)
            }

            val userSales by wealthViewModel.userReferralSales.collectAsState()
            val userWithdrawals by wealthViewModel.userWithdrawals.collectAsState()
            val products by wealthViewModel.products.collectAsState()

            var showSubmitSaleDialog by remember { mutableStateOf(false) }
            var showWithdrawDialog by remember { mutableStateOf(false) }
            var salesExpanded by remember { mutableStateOf(false) }
            var withdrawalsExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Affiliate Wallet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Affiliate & Partner Wallet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // Referral Code display
                        if (userProfile.referralCode.isNotEmpty()) {
                            SuggestionChip(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Referral Code", userProfile.referralCode)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Referral Code Copied!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("Code: ${userProfile.referralCode}") },
                                icon = { Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }

                    // Balance Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(userProfile.referralBalance),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreenLight
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Rewards Earned",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(userProfile.referralRewardsEarned),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSubmitSaleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("btn_submit_sale")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Sale", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = { showWithdrawDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("btn_request_payout")
                        ) {
                            Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // Collapsible Referrals/Sales List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { salesExpanded = !salesExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Referral Sales History (${userSales.size})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                imageVector = if (salesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
                        }

                        if (salesExpanded) {
                            if (userSales.isEmpty()) {
                                Text(
                                    text = "No sales submitted yet. Find a buyer for any affiliate asset, make a sale, and submit it to get 40%+ commission!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(12.dp)
                                )
                            } else {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    userSales.take(10).forEach { sale ->
                                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(sale.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Buyer: ${sale.buyerName} (${sale.buyerEmail})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                    Text("Price: $${String.format("%.2f", sale.salePrice)} • Ref: ${sale.paymentReference}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "+$${String.format("%.2f", sale.commissionEarned)}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldGreenLight
                                                    )
                                                    
                                                    val statusColor = when (sale.status) {
                                                        "Completed" -> EmeraldGreenLight
                                                        "Rejected" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.outline
                                                    }
                                                    Text(
                                                        text = sale.status,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                            }
                                            if (sale.status == "Rejected" && sale.rejectionReason.isNotEmpty()) {
                                                Text(
                                                    text = "Reason: ${sale.rejectionReason}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            Divider(modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Collapsible Withdrawals History
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { withdrawalsExpanded = !withdrawalsExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp), tint = EmeraldGreenLight)
                                Text("Payout / Withdrawal History (${userWithdrawals.size})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                imageVector = if (withdrawalsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
                        }

                        if (withdrawalsExpanded) {
                            if (userWithdrawals.isEmpty()) {
                                Text(
                                    text = "No withdrawals requested yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(12.dp)
                                )
                            } else {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    userWithdrawals.take(10).forEach { wd ->
                                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Amount: $${String.format("%.2f", wd.amount)} via ${wd.payoutMethod}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text("Details: ${wd.payoutDetails}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                    if (wd.transactionHash.isNotEmpty()) {
                                                        Text("Tx Hash: ${wd.transactionHash}", style = MaterialTheme.typography.labelSmall, color = EmeraldGreenLight)
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    val statusColor = when (wd.status) {
                                                        "Approved" -> EmeraldGreenLight
                                                        "Rejected" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.outline
                                                    }
                                                    Text(
                                                        text = wd.status,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                            }
                                            Divider(modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SUBMIT REFERRAL SALE DIALOG ---
            if (showSubmitSaleDialog) {
                var selectedProd by remember { mutableStateOf<com.example.data.model.AffiliateProduct?>(products.firstOrNull()) }
                var buyerName by remember { mutableStateOf("") }
                var buyerEmail by remember { mutableStateOf("") }
                var inputPrice by remember { mutableStateOf(selectedProd?.price?.toString() ?: "") }
                var payRef by remember { mutableStateOf("") }
                var isSubmittingSale by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showSubmitSaleDialog = false },
                    title = { Text("Submit Referral Sale", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("Please enter the completed transaction details. Once verified by our administrators, your 40%+ commission will be credited directly to your wallet balance.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            
                            // Select Product Dropdown simulation
                            Text("Select Affiliate Product *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            if (products.isEmpty()) {
                                Text("No affiliate assets available in the marketplace currently.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    products.forEach { prod ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    selectedProd = prod
                                                    inputPrice = prod.price.toString()
                                                }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = selectedProd?.id == prod.id, onClick = { 
                                                selectedProd = prod
                                                inputPrice = prod.price.toString()
                                            })
                                            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                                Text(prod.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text("${prod.commissionPercent}% commission • Normal: $${prod.price}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = buyerName,
                                onValueChange = { buyerName = it },
                                label = { Text("Buyer Full Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("referral_buyer_name")
                            )

                            OutlinedTextField(
                                value = buyerEmail,
                                onValueChange = { buyerEmail = it },
                                label = { Text("Buyer Email Address *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("referral_buyer_email")
                            )

                            OutlinedTextField(
                                value = inputPrice,
                                onValueChange = { inputPrice = it },
                                label = { Text("Actual Transaction Price ($) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("referral_sale_price")
                            )

                            OutlinedTextField(
                                value = payRef,
                                onValueChange = { payRef = it },
                                label = { Text("Payment Reference / Proof ID *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("referral_pay_ref")
                            )

                            // Dynamic Commission Display
                            selectedProd?.let { prod ->
                                val priceDouble = inputPrice.toDoubleOrNull() ?: 0.0
                                val earned = priceDouble * (prod.commissionPercent / 100.0)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmeraldGreenLight.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Calculated Reward", style = MaterialTheme.typography.labelSmall, color = EmeraldGreenLight)
                                            Text("Commission: ${prod.commissionPercent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = NumberFormat.getCurrencyInstance(Locale.US).format(earned),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldGreenLight
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val prod = selectedProd
                                val price = inputPrice.toDoubleOrNull()
                                if (prod == null || buyerName.isBlank() || buyerEmail.isBlank() || price == null || payRef.isBlank()) {
                                    android.widget.Toast.makeText(context, "Please fill in all fields with valid details", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmittingSale = true
                                wealthViewModel.submitReferralSale(
                                    referrerUid = userProfile.uid,
                                    referrerEmail = userProfile.email,
                                    product = prod,
                                    buyerName = buyerName,
                                    buyerEmail = buyerEmail,
                                    salePrice = price,
                                    paymentReference = payRef
                                ) { success ->
                                    isSubmittingSale = false
                                    if (success) {
                                        showSubmitSaleDialog = false
                                        android.widget.Toast.makeText(context, "Referral sale submitted successfully!", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to submit referral sale.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isSubmittingSale,
                            modifier = Modifier.testTag("submit_referral_btn")
                        ) {
                            Text(if (isSubmittingSale) "Submitting..." else "Submit Sale")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubmitSaleDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // --- REQUEST WITHDRAWAL / PAYOUT DIALOG ---
            if (showWithdrawDialog) {
                var amountStr by remember { mutableStateOf("") }
                var selectedMethod by remember { mutableStateOf("Bank Transfer") }
                val methods = listOf("Bank Transfer", "PayPal", "Crypto (USDT)")
                var payoutDetails by remember { mutableStateOf("") }
                var isSubmittingWd by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showWithdrawDialog = false },
                    title = { Text("Request Withdrawal", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Transfer affiliate commission earnings to your personal account. Available balance: ${NumberFormat.getCurrencyInstance(Locale.US).format(userProfile.referralBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                label = { Text("Withdrawal Amount ($) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("withdraw_amount")
                            )

                            Text("Payout Method *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                methods.forEach { method ->
                                    val isSel = selectedMethod == method
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedMethod = method },
                                        label = { Text(method, fontSize = 11.sp) }
                                    )
                                }
                            }

                            val placeholderText = when (selectedMethod) {
                                "Bank Transfer" -> "Bank Name, Account Name, Account Number, SWIFT"
                                "PayPal" -> "PayPal email address"
                                else -> "USDT Wallet Address (TRC20)"
                            }

                            OutlinedTextField(
                                value = payoutDetails,
                                onValueChange = { payoutDetails = it },
                                label = { Text("Payout Details *") },
                                placeholder = { Text(placeholderText, fontSize = 12.sp) },
                                singleLine = false,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("withdraw_payout_details")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull()
                                if (amount == null || amount <= 0.0 || payoutDetails.isBlank()) {
                                    android.widget.Toast.makeText(context, "Please enter a valid amount and payout details", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amount > userProfile.referralBalance) {
                                    android.widget.Toast.makeText(context, "Insufficient wallet balance!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmittingWd = true
                                wealthViewModel.requestWithdrawal(
                                    userUid = userProfile.uid,
                                    userEmail = userProfile.email,
                                    amount = amount,
                                    method = selectedMethod,
                                    details = payoutDetails
                                ) { res ->
                                    isSubmittingWd = false
                                    if (res.isSuccess) {
                                        showWithdrawDialog = false
                                        // Force reload profile balance
                                        authViewModel.reloadProfile()
                                        android.widget.Toast.makeText(context, "Withdrawal request submitted for processing!", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to request withdrawal", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isSubmittingWd,
                            modifier = Modifier.testTag("submit_withdraw_btn")
                        ) {
                            Text(if (isSubmittingWd) "Processing..." else "Submit Request")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWithdrawDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // Credentials & Badges Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Professional Credentials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                allBadges.forEach { (badgeName, badgeDesc) ->
                    val isEarned = userProfile.badges.contains(badgeName)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEarned) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = if (isEarned) "Earned badge" else "Locked badge",
                            tint = if (isEarned) RichGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (isEarned) RichGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = badgeName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = badgeDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEarned) 0.7f else 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Saved Bookmarked Articles
        val savedIds by Repository.savedArticles.collectAsState()
        val bookmarkedArticles = Repository.articles.filter { savedIds.contains(it.id) }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Saved Blueprints",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (bookmarkedArticles.isEmpty()) {
                    Text(
                        text = "No saved blueprints yet. Bookmark guides in the Discover hub for easy access here.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    bookmarkedArticles.forEach { article ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToArticle(article) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Saved Article Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Blueprint",
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // System Configuration/Telemetry Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFbAvailable) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isFbAvailable) EmeraldGreenLight else RichGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INTEGRATION DIAGNOSTICS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFbAvailable) {
                        "Your account is actively synchronized with live Firebase Auth and Firestore. You can access cloud data on multiple platforms."
                    } else {
                        "Running in Simulation Mode. Data is fully isolated, encrypted, and saved locally to this application container. Full cloud connectivity triggers upon adding a valid google-services.json."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                )
            }
        }

        // Logout Button
        Button(
            onClick = { authViewModel.signOut() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out of Wealth Builder", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
