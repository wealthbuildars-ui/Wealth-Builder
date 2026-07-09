package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.example.data.model.AffiliateProduct
import com.example.data.model.UserProfile
import com.example.ui.WealthViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RichGold
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerHubSubTab(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    wealthViewModel: WealthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by wealthViewModel.products.collectAsState()
    val orders by wealthViewModel.orders.collectAsState()
    
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<AffiliateProduct?>(null) }
    
    // Refresh orders and products
    LaunchedEffect(userProfile.uid) {
        wealthViewModel.loadAllOrders()
        wealthViewModel.loadMarketplaceData(userProfile.uid)
    }

    if (!userProfile.isSeller) {
        // --- SELLER ONBOARDING FORM ---
        SellerOnboardingView(
            userUid = userProfile.uid,
            wealthViewModel = wealthViewModel,
            authViewModel = authViewModel
        )
    } else {
        // --- SELLER ACTIVE DASHBOARD ---
        val sellerProducts = remember(products) {
            products.filter { it.sellerId == userProfile.uid }
        }
        val sellerOrders = remember(orders) {
            orders.filter { it.sellerId == userProfile.uid }
        }
        
        // Compute Sales Analytics
        val totalSalesCount = sellerOrders.size
        val totalCompletedSalesCount = sellerOrders.count { it.status == "Completed" }
        val totalRevenue = sellerOrders.filter { it.status == "Completed" }.sumOf { it.finalPayableAmount }
        
        var selectedFilterTab by remember { mutableStateOf(0) } // 0 = All, 1 = Approved, 2 = Pending, 3 = Rejected

        val filteredSellerProducts = remember(sellerProducts, selectedFilterTab) {
            when (selectedFilterTab) {
                1 -> sellerProducts.filter { it.status == "Approved" }
                2 -> sellerProducts.filter { it.status == "Pending Review" || it.status == "Pending" }
                3 -> sellerProducts.filter { it.status == "Rejected" }
                else -> sellerProducts
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.sellerBusinessName.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Seller Studio • Merchant Dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // --- SELLER TRUST & VERIFICATION BANNER CARD ---
            item {
                val verificationStatus = userProfile.sellerVerificationStatus.ifEmpty { "Unverified" }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (verificationStatus) {
                            "Verified" -> Color(0xFF0F9D58).copy(alpha = 0.08f)
                            "Pending Verification" -> Color(0xFFF4B400).copy(alpha = 0.08f)
                            "Rejected" -> Color.Red.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (verificationStatus) {
                            "Verified" -> Color(0xFF0F9D58).copy(alpha = 0.3f)
                            "Pending Verification" -> Color(0xFFF4B400).copy(alpha = 0.3f)
                            "Rejected" -> Color.Red.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    var showVerificationDialog by remember { mutableStateOf(false) }
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (verificationStatus) {
                                        "Verified" -> Icons.Default.Verified
                                        "Pending Verification" -> Icons.Default.PendingActions
                                        "Rejected" -> Icons.Default.Error
                                        else -> Icons.Default.GppMaybe
                                    },
                                    contentDescription = null,
                                    tint = when (verificationStatus) {
                                        "Verified" -> Color(0xFF0F9D58)
                                        "Pending Verification" -> Color(0xFFF4B400)
                                        "Rejected" -> Color.Red
                                        else -> MaterialTheme.colorScheme.secondary
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Merchant Trust Badge Status",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (verificationStatus) {
                                            "Verified" -> Color(0xFF0F9D58).copy(alpha = 0.15f)
                                            "Pending Verification" -> Color(0xFFF4B400).copy(alpha = 0.15f)
                                            "Rejected" -> Color.Red.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = verificationStatus.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (verificationStatus) {
                                        "Verified" -> Color(0xFF0F9D58)
                                        "Pending Verification" -> Color(0xFFF4B400)
                                        "Rejected" -> Color.Red
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (verificationStatus) {
                                "Verified" -> "Awesome! Your store is fully verified. Your trust badge is live next to all your listed products."
                                "Pending Verification" -> "Your verification materials have been successfully received and are under review by the platform administrator. This usually takes 24 hours."
                                "Rejected" -> "Your verification submission was declined. Please verify your phone number, National ID, or business records and re-submit."
                                else -> "Unlock higher sales conversion rates! Verify your physical storefront or professional status to earn a trusted partner badge."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (verificationStatus == "Unverified" || verificationStatus == "Rejected" || verificationStatus == "Pending Verification") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showVerificationDialog = true },
                                modifier = Modifier.testTag("submit_verification_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Submit/Update Verification Documents", fontSize = 12.sp)
                            }
                        }
                    }
                    
                    if (showVerificationDialog) {
                        var natId by remember { mutableStateOf(userProfile.sellerNationalId) }
                        var bizReg by remember { mutableStateOf(userProfile.sellerBusinessRegistration) }
                        var phoneNum by remember { mutableStateOf(userProfile.sellerPhoneNumber) }
                        var emailAddr by remember { mutableStateOf(userProfile.email) }
                        var isSubmitting by remember { mutableStateOf(false) }
                        
                        AlertDialog(
                            onDismissRequest = { showVerificationDialog = false },
                            title = { Text("Storefront Verification Profile", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Submit details to verify your seller status and get a green Trust Badge.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    OutlinedTextField(
                                        value = emailAddr,
                                        onValueChange = { emailAddr = it },
                                        label = { Text("Email Address Verification") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false
                                    )
                                    OutlinedTextField(
                                        value = phoneNum,
                                        onValueChange = { phoneNum = it },
                                        label = { Text("Phone Number Verification *") },
                                        placeholder = { Text("E.g. +1 555-0199") },
                                        modifier = Modifier.fillMaxWidth().testTag("verification_phone_input")
                                    )
                                    OutlinedTextField(
                                        value = natId,
                                        onValueChange = { natId = it },
                                        label = { Text("National ID (Optional)") },
                                        placeholder = { Text("E.g. Passport, Drivers license ID") },
                                        modifier = Modifier.fillMaxWidth().testTag("verification_natid_input")
                                    )
                                    OutlinedTextField(
                                        value = bizReg,
                                        onValueChange = { bizReg = it },
                                        label = { Text("Business Registration Number (Optional)") },
                                        placeholder = { Text("E.g. LLC / Corporation registration") },
                                        modifier = Modifier.fillMaxWidth().testTag("verification_bizreg_input")
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (phoneNum.trim().isEmpty()) {
                                            Toast.makeText(context, "Phone number is required", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isSubmitting = true
                                        wealthViewModel.submitSellerVerification(
                                            userId = userProfile.uid,
                                            nationalId = natId,
                                            bizReg = bizReg,
                                            phone = phoneNum
                                        ) { success ->
                                            isSubmitting = false
                                            showVerificationDialog = false
                                            Toast.makeText(context, "Verification requested!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isSubmitting && phoneNum.trim().isNotEmpty(),
                                    modifier = Modifier.testTag("verification_submit_confirm")
                                ) {
                                    Text("Submit Profile")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showVerificationDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            // Wallet & Analytics Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Store Performance Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Earnings KPI
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldGreenLight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sales Count", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("$totalSalesCount Orders", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("$totalCompletedSalesCount Completed", style = MaterialTheme.typography.labelSmall, color = EmeraldGreenLight)
                            }
                        }

                        // Wallet Balance KPI
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = RichGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Withdrawable Wallet", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("$${String.format("%.2f", userProfile.sellerBalance)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Pending: $${String.format("%.2f", userProfile.sellerPendingBalance)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Quick Actions Button row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddProductDialog = true },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("submit_product_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Product", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showWithdrawalDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("withdraw_earnings_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Product Catalog Filtering Status row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Product Listings Catalog", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterOptions = listOf("All", "Approved", "Pending", "Rejected")
                        filterOptions.forEachIndexed { index, option ->
                            FilterChip(
                                selected = selectedFilterTab == index,
                                onClick = { selectedFilterTab = index },
                                label = { Text(option, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Render Products List
            if (filteredSellerProducts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No products in this category", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                            Text("Click 'Add Product' above to expand your catalog.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(filteredSellerProducts, key = { it.id }) { product ->
                    SellerProductCard(
                        product = product,
                        onEditClick = { editingProduct = product },
                        onDeleteClick = {
                            wealthViewModel.deleteProduct(product.id)
                            Toast.makeText(context, "Listing deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // --- DIALOG: SUBMIT / EDIT PRODUCT FORM ---
    if (showAddProductDialog || editingProduct != null) {
        val prodToEdit = editingProduct
        AddEditProductDialog(
            productToEdit = prodToEdit,
            userUid = userProfile.uid,
            sellerName = userProfile.displayName,
            wealthViewModel = wealthViewModel,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    // --- DIALOG: REQUEST PAYOUT WITHDRAWAL ---
    if (showWithdrawalDialog) {
        RequestPayoutDialog(
            userProfile = userProfile,
            wealthViewModel = wealthViewModel,
            authViewModel = authViewModel,
            onDismiss = { showWithdrawalDialog = false }
        )
    }
}

// --- COMPOSABLE: SELLER ONBOARDING ---
@Composable
fun SellerOnboardingView(
    userUid: String,
    wealthViewModel: WealthViewModel,
    authViewModel: AuthViewModel
) {
    var businessName by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = "Onboarding storefront",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Launch Your Seller Storefront",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Register as a Merchant on the Wealth Builder Marketplace. Sell high-ticket educational resources, business services, or digital kits, and leverage our network of active affiliate marketers to explode your sales!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Store / Business Name") },
            placeholder = { Text("e.g. Apex Digital Academy") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_business_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (businessName.isBlank()) {
                    Toast.makeText(context, "Please provide your Storefront Name", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitting = true
                wealthViewModel.registerAsSeller(userUid, businessName) {
                    authViewModel.refreshUserProfile(userUid) { updatedProfile ->
                        isSubmitting = false
                        Toast.makeText(context, "Congratulations! Storefront Activated 🎉", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isSubmitting && businessName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("activate_seller_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Activate Storefront", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- COMPOSABLE: SELLER PRODUCT LISTING CARD ---
@Composable
fun SellerProductCard(
    product: AffiliateProduct,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusColor = when (product.status) {
        "Approved" -> EmeraldGreenLight
        "Rejected" -> MaterialTheme.colorScheme.error
        else -> RichGold
    }

    val statusLabel = when (product.status) {
        "Approved" -> "Live Marketplace"
        "Rejected" -> "Rejected"
        else -> "Pending Review"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Status and actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusLabel.uppercase(),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Sellers can edit or delete Pending or Rejected listings
                    if (product.status == "Pending Review" || product.status == "Pending" || product.status == "Rejected") {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp).testTag("edit_product_${product.id}")) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Listing", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp).testTag("delete_product_${product.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Listing", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    if (product.images.isNotEmpty()) {
                        AsyncImage(
                            model = product.images.first(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(product.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "$${product.price}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Qty: ${product.availableQuantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Comm: ${product.commissionPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = RichGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Display rejection feedback if status is Rejected
            if (product.status == "Rejected" && product.rejectionReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Rejection Feedback:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(product.rejectionReason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

// --- COMPOSABLE: ADD / EDIT PRODUCT DIALOG FORM ---
@Composable
fun AddEditProductDialog(
    productToEdit: AffiliateProduct?,
    userUid: String,
    sellerName: String,
    wealthViewModel: WealthViewModel,
    onDismiss: () -> Unit
) {
    val isEditing = productToEdit != null
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "E-Learning Resources") }
    var price by remember { mutableStateOf(productToEdit?.price?.toString() ?: "") }
    var quantity by remember { mutableStateOf(productToEdit?.availableQuantity?.toString() ?: "100") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var imageUrl by remember { mutableStateOf(productToEdit?.images?.firstOrNull() ?: "") }
    var commissionPercent by remember { mutableStateOf(productToEdit?.commissionPercent?.toString() ?: "40") }
    var brand by remember { mutableStateOf(productToEdit?.brand ?: "Storefront Asset") }
    var deliveryFee by remember { mutableStateOf(productToEdit?.deliveryFee?.toString() ?: "5.00") }
    var deliveryRegions by remember { mutableStateOf(productToEdit?.deliveryRegions ?: "All Regions") }
    var estimatedDeliveryTime by remember { mutableStateOf(productToEdit?.estimatedDeliveryTime ?: "3-5 business days") }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Modify Pending Product" else "Submit New Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Submitted products will appear in the Admin Review Queue before public listing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth().testTag("product_category_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Selling Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_price_input")
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_quantity_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = commissionPercent,
                        onValueChange = { commissionPercent = it },
                        label = { Text("Affiliate Commission (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_commission_input")
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand / Creator") },
                        modifier = Modifier.weight(1f).testTag("product_brand_input")
                    )
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Product Image URL (Optional)") },
                    placeholder = { Text("https://example.com/image.png") },
                    modifier = Modifier.fillMaxWidth().testTag("product_image_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Full Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("product_description_input")
                )

                Text(
                    "Delivery Configurations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deliveryFee,
                        onValueChange = { deliveryFee = it },
                        label = { Text("Delivery Fee ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_delivery_fee_input")
                    )
                    OutlinedTextField(
                        value = estimatedDeliveryTime,
                        onValueChange = { estimatedDeliveryTime = it },
                        label = { Text("Est. Delivery Time") },
                        placeholder = { Text("E.g. 3-5 business days") },
                        modifier = Modifier.weight(1.5f).testTag("product_delivery_time_input")
                    )
                }

                OutlinedTextField(
                    value = deliveryRegions,
                    onValueChange = { deliveryRegions = it },
                    label = { Text("Delivery Regions (Comma separated)") },
                    placeholder = { Text("E.g. Nationwide, US, UK, EU") },
                    modifier = Modifier.fillMaxWidth().testTag("product_delivery_regions_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val prValue = price.toDoubleOrNull() ?: 0.0
                            val qtyValue = quantity.toIntOrNull() ?: 100
                            val commValue = commissionPercent.toDoubleOrNull() ?: 40.0
                            val deliveryFeeVal = deliveryFee.toDoubleOrNull() ?: 5.0
                            
                            if (name.isBlank() || price.isBlank() || description.isBlank()) {
                                Toast.makeText(context, "Please populate all mandatory fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val product = AffiliateProduct(
                                id = productToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                category = category,
                                brand = brand,
                                images = if (imageUrl.isNotEmpty()) listOf(imageUrl) else emptyList(),
                                price = prValue,
                                currency = "USD",
                                affiliateLink = "", // local marketplace item doesn't have an external link
                                merchantName = "Storefront • $sellerName",
                                stockStatus = if (qtyValue > 0) "In Stock" else "Out of Stock",
                                commissionPercent = commValue,
                                status = "Pending Review", // resets status to Pending on edits/re-submission!
                                sellerId = userUid,
                                sellerName = sellerName,
                                availableQuantity = qtyValue,
                                dateAdded = productToEdit?.dateAdded ?: System.currentTimeMillis(),
                                lastUpdated = System.currentTimeMillis(),
                                deliveryFee = deliveryFeeVal,
                                deliveryRegions = deliveryRegions,
                                estimatedDeliveryTime = estimatedDeliveryTime
                            )

                            wealthViewModel.submitProduct(product)
                            Toast.makeText(context, "Product successfully submitted for review!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f).testTag("submit_product_confirm_btn")
                    ) {
                        Text("Submit Listing")
                    }
                }
            }
        }
    }
}

// --- COMPOSABLE: PAYOUT WITHDRAWAL REQUEST DIALOG ---
@Composable
fun RequestPayoutDialog(
    userProfile: UserProfile,
    wealthViewModel: WealthViewModel,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Bank Transfer") }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Request Earnings Payout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Request transfer from your Storefront Balance. Admin will process payments to your details within 24 hours.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Available Seller Balance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("$${String.format("%.2f", userProfile.sellerBalance)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_amount_input")
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("OPay/Bank Details or Account Info") },
                    placeholder = { Text("e.g. OPay - 9162072645 - Apex Store") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_details_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Discard")
                    }

                    Button(
                        onClick = {
                            val amtValue = amount.toDoubleOrNull() ?: 0.0
                            if (amtValue <= 0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amtValue > userProfile.sellerBalance) {
                                Toast.makeText(context, "Insufficient available seller balance", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (details.isBlank()) {
                                Toast.makeText(context, "Please provide payout destination details", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitting = true
                            wealthViewModel.requestWithdrawal(
                                userUid = userProfile.uid,
                                userEmail = userProfile.email,
                                amount = amtValue,
                                method = method,
                                details = details,
                                walletType = "Seller"
                            ) { result ->
                                isSubmitting = false
                                result.onSuccess {
                                    authViewModel.refreshUserProfile(userProfile.uid) {
                                        Toast.makeText(context, "Withdrawal requested successfully!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Failed to process request", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSubmitting && amount.isNotEmpty() && details.isNotEmpty(),
                        modifier = Modifier.weight(1.5f).testTag("submit_withdrawal_confirm_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Submit Request")
                        }
                    }
                }
            }
        }
    }
}
