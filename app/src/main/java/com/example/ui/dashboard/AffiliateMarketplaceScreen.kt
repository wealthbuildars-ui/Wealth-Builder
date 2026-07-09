package com.example.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AffiliateProduct
import com.example.data.model.AffiliatePartner
import com.example.ui.WealthViewModel
import com.example.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceSubTab(
    viewModel: WealthViewModel,
    userId: String,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val products by viewModel.products.collectAsState()
    val partners by viewModel.partners.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()

    val adCampaigns by viewModel.adCampaigns.collectAsState()
    val activeAds = remember(adCampaigns) { adCampaigns.filter { it.status == "Active" } }
    
    val adminSettings by authViewModel.adminSettings.collectAsState()
    val currentUserProfile by authViewModel.currentUser.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()
    var checkoutProduct by remember { mutableStateOf<AffiliateProduct?>(null) }
    
    var showFilters by remember { mutableStateOf(false) }
    var selectedTabByViewType by remember { mutableStateOf(0) } // 0 = Explore, 1 = Wishlist & Saved
    
    // Set active userId on ViewModel
    LaunchedEffect(userId) {
        viewModel.loadMarketplaceData(userId)
    }

    // Filtered list computed reactively
    val filteredProducts = remember(products, viewModel.searchQuery, viewModel.searchCategory, viewModel.searchBrand, viewModel.minPrice, viewModel.maxPrice, viewModel.sortOption) {
        var list = products.filter { !it.isArchived }
        
        // Search filter
        if (viewModel.searchQuery.isNotEmpty()) {
            list = list.filter { 
                it.name.contains(viewModel.searchQuery, ignoreCase = true) || 
                it.description.contains(viewModel.searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(viewModel.searchQuery, ignoreCase = true) }
            }
        }
        
        // Category filter
        if (viewModel.searchCategory != "All") {
            list = list.filter { it.category == viewModel.searchCategory }
        }
        
        // Brand filter
        if (viewModel.searchBrand != "All") {
            list = list.filter { it.brand == viewModel.searchBrand }
        }
        
        // Price filters
        viewModel.minPrice?.let { min -> list = list.filter { it.price >= min } }
        viewModel.maxPrice?.let { max -> list = list.filter { it.price <= max } }
        
        // Sorting
        list = when (viewModel.sortOption) {
            "Newest" -> list.sortedByDescending { it.dateAdded }
            "Popularity" -> list.sortedByDescending { it.reviewCount }
            "Price Low to High" -> list.sortedBy { it.price }
            "Price High to Low" -> list.sortedByDescending { it.price }
            "Rating" -> list.sortedByDescending { it.rating }
            else -> list
        }
        
        list
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Wealth Marketplace",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Affiliate wealth engines & high-commission gear",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    
                    // Explore / Wishlist / Chat / Orders Tab Selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTabByViewType == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedTabByViewType = 0 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Explore",
                                color = if (selectedTabByViewType == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTabByViewType == 1) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .clickable { selectedTabByViewType = 1 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (selectedTabByViewType == 1) MaterialTheme.colorScheme.onSecondary else Color.Red,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Saved (${wishlist.size})",
                                    color = if (selectedTabByViewType == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTabByViewType == 2) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                                .clickable { selectedTabByViewType = 2 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = if (selectedTabByViewType == 2) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Chat",
                                    color = if (selectedTabByViewType == 2) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTabByViewType == 3) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedTabByViewType = 3 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = if (selectedTabByViewType == 3) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Orders",
                                    color = if (selectedTabByViewType == 3) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar with Filter Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("marketplace_search"),
                        placeholder = { Text("Search products, brands, or tools...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilledIconButton(
                        onClick = { showFilters = !showFilters },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (showFilters || viewModel.searchCategory != "All" || viewModel.searchBrand != "All") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (showFilters || viewModel.searchCategory != "All" || viewModel.searchBrand != "All") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expanded filter configuration
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Filters & Sorting", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category Scroll Row
                        Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val availableCategories = listOf("All") + products.map { it.category }.distinct().filter { it.isNotEmpty() }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableCategories) { cat ->
                                val isSelected = viewModel.searchCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.searchCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Brand Scroll Row
                        Text("Brand", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val availableBrands = listOf("All") + products.map { it.brand }.distinct().filter { it.isNotEmpty() }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableBrands) { b ->
                                val isSelected = viewModel.searchBrand == b
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.searchBrand = b },
                                    label = { Text(b) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sorting Options Scroll Row
                        Text("Sort By", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val sorts = listOf("Newest", "Popularity", "Price Low to High", "Price High to Low", "Rating")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sorts) { opt ->
                                val isSelected = viewModel.sortOption == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable { viewModel.sortOption = opt }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        opt,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clear filters button
                        TextButton(
                            onClick = {
                                viewModel.searchCategory = "All"
                                viewModel.searchBrand = "All"
                                viewModel.minPrice = null
                                viewModel.maxPrice = null
                                viewModel.sortOption = "Newest"
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Filters")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (selectedTabByViewType == 1) {
            // Wishlist View
            val wishlistProducts = products.filter { wishlist.contains(it.id) }
            if (wishlistProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your Wishlist is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Save products by clicking the heart badge to build your affiliate portfolio tracking catalog.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wishlistProducts) { prod ->
                        SavedProductRowItem(
                            product = prod,
                            onProductClick = {
                                viewModel.selectedProductDetail = prod
                                viewModel.recordProductView(prod.id)
                            },
                            onRemoveClick = { viewModel.toggleWishlist(prod.id) }
                        )
                    }
                }
            }
        } else if (selectedTabByViewType == 2) {
            // Live Chat View
            LiveChatMarketplacePanel(
                viewModel = viewModel,
                innerPadding = innerPadding,
                userId = userId,
                currentUserProfile = currentUserProfile,
                allUsers = allUsers
            )
        } else if (selectedTabByViewType == 3) {
            // My Purchases View
            MyPurchasesTab(
                viewModel = viewModel,
                userId = userId,
                innerPadding = innerPadding
            )
        } else {
            val searchAds = remember(activeAds) { activeAds.filter { it.adType == "Search Result Sponsored Ads" } }
            val productAds = remember(activeAds) { activeAds.filter { it.adType == "Product Sponsored Ads" } }

            // Explore Hub View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Check if user is actively searching
                if (viewModel.searchQuery.isNotEmpty() || viewModel.searchCategory != "All" || viewModel.searchBrand != "All") {
                    // --- SEARCH RESULT SPONSORED ADS ---
                    if (searchAds.isNotEmpty()) {
                        item {
                            Text(
                                text = "Sponsored Offers",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(searchAds) { ad ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                SponsoredMarketplaceAdCard(ad = ad, viewModel = viewModel)
                            }
                        }
                    }
                    item {
                        Text(
                            text = "Search Results (${filteredProducts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    
                    if (filteredProducts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No matching products found", fontWeight = FontWeight.SemiBold)
                                    Text("Try adjusting your keyword or category filter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(filteredProducts) { prod ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                ProductWideCard(
                                    product = prod,
                                    isSaved = wishlist.contains(prod.id),
                                    onSaveToggle = { viewModel.toggleWishlist(prod.id) },
                                    onClick = {
                                        viewModel.selectedProductDetail = prod
                                        viewModel.recordProductView(prod.id)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Standard Home Dashboard Explorer layout

                    // --- PRODUCT SPONSORED ADS ---
                    if (productAds.isNotEmpty()) {
                        item {
                            Text(
                                text = "Sponsored Financial Opportunities 🚀",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        items(productAds) { ad ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                SponsoredMarketplaceAdCard(ad = ad, viewModel = viewModel)
                            }
                        }
                    }

                    // 1. Hero Promo Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color(0xFF0F9D58),
                                            Color(0xFFF4B400) // Gold
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PREMIUM AFFILIATE ENGINES", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Build Wealth Ethically",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "Promote verified, high-commission digital courses & physical workstations to your network instantly.",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // 2. Featured Section
                    val featured = products.filter { it.isFeatured }
                    if (featured.isNotEmpty()) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF4B400))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Featured Powerhouses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(featured) { prod ->
                                        ProductFeaturedCard(
                                            product = prod,
                                            isSaved = wishlist.contains(prod.id),
                                            onSaveToggle = { viewModel.toggleWishlist(prod.id) },
                                            onClick = {
                                                viewModel.selectedProductDetail = prod
                                                viewModel.recordProductView(prod.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Trending Products Section
                    val trending = products.filter { it.isTrending }
                    if (trending.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trending Hotspots", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(trending) { prod ->
                                        ProductCompactCard(
                                            product = prod,
                                            isSaved = wishlist.contains(prod.id),
                                            onSaveToggle = { viewModel.toggleWishlist(prod.id) },
                                            onClick = {
                                                viewModel.selectedProductDetail = prod
                                                viewModel.recordProductView(prod.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Partner Platforms
                    if (partners.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text("Affiliate Networks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Configured partners powering marketplace payouts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                partners.forEach { p ->
                                    PartnerCommissionCard(partner = p)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    // 5. Recommended / All Products
                    val recommended = products.filter { it.isRecommended }
                    if (recommended.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Recommend, contentDescription = null, tint = Color(0xFFF4B400))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recommended for You", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                
                                recommended.forEach { prod ->
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                        ProductWideCard(
                                            product = prod,
                                            isSaved = wishlist.contains(prod.id),
                                            onSaveToggle = { viewModel.toggleWishlist(prod.id) },
                                            onClick = {
                                                viewModel.selectedProductDetail = prod
                                                viewModel.recordProductView(prod.id)
                                            }
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

    // --- PRODUCT DETAIL SHEET DIALOG ---
    viewModel.selectedProductDetail?.let { product ->
        ProductDetailsDialog(
            product = product,
            isSaved = wishlist.contains(product.id),
            allProducts = products,
            onSaveToggle = { viewModel.toggleWishlist(product.id) },
            onBuyNow = {
                viewModel.recordProductClick(product.id)
                if (product.sellerId.isNotEmpty() || product.affiliateLink.isEmpty()) {
                    checkoutProduct = product
                    viewModel.selectedProductDetail = null
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.affiliateLink))
                    context.startActivity(intent)
                }
            },
            onClose = { viewModel.selectedProductDetail = null },
            onRelatedProductClick = { relatedProd ->
                viewModel.selectedProductDetail = relatedProd
                viewModel.recordProductView(relatedProd.id)
            },
            allUsers = allUsers,
            viewModel = viewModel,
            userId = userId
        )
    }

    // --- CHECKOUT ORDER DIALOG ---
    checkoutProduct?.let { product ->
        val buyerProfile = currentUserProfile ?: com.example.data.model.UserProfile(uid = userId)
        CheckoutOrderDialog(
            product = product,
            buyerProfile = buyerProfile,
            adminSettings = adminSettings,
            onDismiss = { checkoutProduct = null },
            onPlaceOrder = { quantity, shippingAddress, affiliateCode ->
                val affiliateUser = allUsers.find { it.referralCode.trim().equals(affiliateCode.trim(), ignoreCase = true) }
                val affiliateId = affiliateUser?.uid ?: ""
                
                viewModel.placeOrder(
                    buyerId = userId,
                    buyerName = buyerProfile.displayName,
                    buyerEmail = buyerProfile.email,
                    product = product,
                    quantity = quantity,
                    shippingAddress = shippingAddress,
                    affiliateId = affiliateId
                ) { success ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Order placed successfully! 📦", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "Failed to place order. Please try again.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    checkoutProduct = null
                }
            }
        )
    }
}

// --- CORE PRODUCT CARDS & COMPONENTS ---

@Composable
fun ProductFeaturedCard(
    product: AffiliateProduct,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .height(310.dp)
            .testTag("featured_product_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Product Image Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.LightGray)
                ) {
                    if (product.images.isNotEmpty()) {
                        AsyncImage(
                            model = product.images.first(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Gold commission discount banner if any
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color(0xFFF4B400), RoundedCornerShape(topEnd = 8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (product.partnerId == "wealth_builder_direct") "40% RECURRING" else "TOP COMMISSION",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.brand,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF4B400), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(product.rating.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Price & Merchant
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (product.discountPrice != null) {
                                Text(
                                    text = "$${product.price}",
                                    style = MaterialTheme.typography.labelSmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "$${product.discountPrice}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F9D58) // Gold/Green Wealth color
                                )
                            } else {
                                Text(
                                    text = "$${product.price}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Small merchant badge
                        Text(
                            text = "via ${product.merchantName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Save Favorite Button Badge
            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save Product",
                    tint = if (isSaved) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProductCompactCard(
    product: AffiliateProduct,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.LightGray)
                ) {
                    if (product.images.isNotEmpty()) {
                        AsyncImage(
                            model = product.images.first(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${product.discountPrice ?: product.price}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF4B400), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(product.rating.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Wishlist heart overlay
            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save Product",
                    tint = if (isSaved) Color.Red else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ProductWideCard(
    product: AffiliateProduct,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray)
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF4B400), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(product.rating.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.discountPrice ?: product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedProductRowItem(
    product: AffiliateProduct,
    onProductClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        onClick = onProductClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.images.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${product.discountPrice ?: product.price} via ${product.merchantName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = "Remove from wishlist", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun PartnerCommissionCard(partner: AffiliatePartner) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (partner.id.contains("amazon")) Icons.Default.ShoppingCart else Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(partner.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(partner.description, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Box(
                modifier = Modifier
                    .background(Color(0xFFF4B400).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${partner.commissionRate}% Payout",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD4AF37) // Golden text
                )
            }
        }
    }
}

// --- PRODUCT DETAILS DIALOG ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsDialog(
    product: AffiliateProduct,
    isSaved: Boolean,
    allProducts: List<AffiliateProduct>,
    onSaveToggle: () -> Unit,
    onBuyNow: () -> Unit,
    onClose: () -> Unit,
    onRelatedProductClick: (AffiliateProduct) -> Unit,
    allUsers: List<com.example.data.model.UserProfile>,
    viewModel: WealthViewModel,
    userId: String
) {
    val context = LocalContext.current
    
    val sellerUser = remember(product.sellerId, allUsers) {
        allUsers.firstOrNull { it.uid == product.sellerId }
    }
    val isSellerVerified = sellerUser?.isVerifiedSeller == true
    
    val currentUserProfile = remember(userId, allUsers) {
        allUsers.firstOrNull { it.uid == userId }
    }
    
    val reviewsState by viewModel.productReviews.collectAsState()
    val reviews = reviewsState[product.id] ?: emptyList()
    
    var userRatingInput by remember { mutableStateOf(5) }
    var userReviewInputText by remember { mutableStateOf("") }
    
    LaunchedEffect(product.id) {
        viewModel.loadReviewsForProduct(product.id)
    }
    
    AlertDialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(product.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (isSellerVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Seller",
                                        tint = Color(0xFF0F9D58),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            // Wishlist button
                            IconButton(onClick = onSaveToggle) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Share button
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Check out this amazing wealth building asset: ${product.name} at ${product.affiliateLink}")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Product")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Image banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(Color.LightGray)
                        ) {
                            if (product.images.isNotEmpty()) {
                                AsyncImage(
                                    model = product.images.first(),
                                    contentDescription = product.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // Specifications & Main specs list
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Category & Stock Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        product.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (product.stockStatus == "In Stock") Color(0xFF0F9D58).copy(alpha = 0.15f) else Color(0xFFF4B400).copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        product.stockStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (product.stockStatus == "In Stock") Color(0xFF0F9D58) else Color(0xFFF4B400)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Name
                            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Pricing Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val finalPrice = product.discountPrice ?: product.price
                                Text(
                                    text = "$$finalPrice",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (product.discountPrice != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$${product.price}",
                                        style = MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Action: Buy Now link opener
                            Button(
                                onClick = onBuyNow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("buy_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)), // Gold Green theme button
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Acquire via ${product.merchantName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Disclosure: As an approved affiliate partner, Wealth Builder may receive commissions on purchases made through these links at no additional expense to you.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Description
                            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(product.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Delivery Configuration
                            Text("Delivery & Shipping Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Fee", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Text(
                                            text = if (product.deliveryFee > 0.0) "$${String.format("%.2f", product.deliveryFee)}" else "FREE",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Regions", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Text(
                                            text = product.deliveryRegions.ifEmpty { "Worldwide" },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Est. Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Text(
                                            text = product.estimatedDeliveryTime.ifEmpty { "3-5 Business Days" },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Customer Reviews Section
                            Text("Customer Reviews & Ratings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (reviews.isEmpty()) {
                                Text("No reviews yet. Be the first to write a review!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    reviews.forEach { rev ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(rev.buyerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        repeat(rev.rating) {
                                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF4B400), modifier = Modifier.size(12.dp))
                                                        }
                                                        repeat(5 - rev.rating) {
                                                            Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(rev.reviewText, style = MaterialTheme.typography.bodySmall)
                                                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(rev.dateCreated))
                                                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Add Review Form
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Write a Review", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    
                                    // Rating Selector (1-5 stars)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Your Rating: ", style = MaterialTheme.typography.bodySmall)
                                        repeat(5) { index ->
                                            val starValue = index + 1
                                            IconButton(
                                                onClick = { userRatingInput = starValue },
                                                modifier = Modifier.size(24.dp).testTag("star_select_$starValue")
                                            ) {
                                                Icon(
                                                    imageVector = if (userRatingInput >= starValue) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = null,
                                                    tint = if (userRatingInput >= starValue) Color(0xFFF4B400) else Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = userReviewInputText,
                                        onValueChange = { userReviewInputText = it },
                                        placeholder = { Text("Describe your experience with this product...", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("user_review_text_field"),
                                        maxLines = 3
                                    )
                                    
                                    Button(
                                        onClick = {
                                            if (userReviewInputText.isBlank()) {
                                                android.widget.Toast.makeText(context, "Please enter review text", android.widget.Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.submitProductReview(
                                                productId = product.id,
                                                buyerId = userId,
                                                buyerName = currentUserProfile?.displayName ?: "Verified Buyer",
                                                rating = userRatingInput,
                                                reviewText = userReviewInputText.trim(),
                                                imageUrl = null
                                            ) { success ->
                                                if (success) {
                                                    android.widget.Toast.makeText(context, "Review submitted! Thank you.", android.widget.Toast.LENGTH_SHORT).show()
                                                    userReviewInputText = ""
                                                    userRatingInput = 5
                                                } else {
                                                    android.widget.Toast.makeText(context, "Failed to submit review", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End).testTag("submit_review_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Submit Review")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Technical Specifications Map Table
                            if (product.specifications.isNotEmpty()) {
                                Text("Technical Specifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    product.specifications.forEach { (key, valString) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(key, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            Text(valString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
                                        }
                                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }

                    // 6. Related Products Section
                    val related = allProducts.filter { it.category == product.category && it.id != product.id }.take(4)
                    if (related.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Column {
                                Text(
                                    text = "Related Products",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(related) { rel ->
                                        ProductCompactCard(
                                            product = rel,
                                            isSaved = false, // simple inside popup
                                            onSaveToggle = {},
                                            onClick = { onRelatedProductClick(rel) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CheckoutOrderDialog(
    product: AffiliateProduct,
    buyerProfile: com.example.data.model.UserProfile,
    adminSettings: com.example.data.model.AdminSettings,
    onDismiss: () -> Unit,
    onPlaceOrder: (quantity: Int, shippingAddress: String, affiliateId: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var shippingAddress by remember { mutableStateOf("") }
    var affiliateCodeInput by remember { mutableStateOf(buyerProfile.referredByCode) }
    
    // Automatically calculate the discount and shipping fee during checkout
    val originalPrice = product.price * quantity
    val discountPercent = if (adminSettings.isCustomerDiscountEnabled) adminSettings.customerDiscountPercent else 0.0
    val discountAmount = originalPrice * (discountPercent / 100.0)
    val shippingFee = product.deliveryFee
    val finalAmount = originalPrice - discountAmount + shippingFee

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Order Checkout 🛍️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.images.firstOrNull()?.ifEmpty { "https://via.placeholder.com/150" } ?: "https://via.placeholder.com/150",
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Category: ${product.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Seller: ${product.merchantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (quantity < product.availableQuantity) quantity++ },
                            enabled = quantity < product.availableQuantity
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                // Shipping details
                OutlinedTextField(
                    value = shippingAddress,
                    onValueChange = { shippingAddress = it },
                    label = { Text("Delivery Shipping Address") },
                    placeholder = { Text("Enter your complete delivery address") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional affiliate referral code
                OutlinedTextField(
                    value = affiliateCodeInput,
                    onValueChange = { affiliateCodeInput = it },
                    label = { Text("Affiliate Referral Code (Optional)") },
                    placeholder = { Text("Enter code to credit an affiliate") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(4.dp))

                // Price Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Original Price", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(
                        text = "$${String.format("%.2f", originalPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (adminSettings.isCustomerDiscountEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Customer Member Discount (${adminSettings.customerDiscountPercent.toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0F9D58)
                        )
                        Text(
                            text = "-$${String.format("%.2f", discountAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9D58)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Shipping & Delivery Fee", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(
                        text = if (shippingFee > 0) "$${String.format("%.2f", shippingFee)}" else "FREE",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (shippingFee > 0) MaterialTheme.colorScheme.onSurface else Color(0xFF0F9D58)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Delivery Time", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(
                        text = product.estimatedDeliveryTime.ifEmpty { "3-5 Business Days" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Final Payable Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${String.format("%.2f", finalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shippingAddress.trim().isEmpty()) {
                        return@Button
                    }
                    onPlaceOrder(quantity, shippingAddress, affiliateCodeInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                shape = RoundedCornerShape(12.dp),
                enabled = shippingAddress.trim().isNotEmpty()
            ) {
                Text("Confirm & Pay", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LiveChatMarketplacePanel(
    viewModel: WealthViewModel,
    innerPadding: PaddingValues,
    userId: String,
    currentUserProfile: com.example.data.model.UserProfile?,
    allUsers: List<com.example.data.model.UserProfile>
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    
    var activeReceiverId by remember { mutableStateOf("admin") }
    var chatInputText by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadChatMessages()
    }
    
    val activeReceiverName = remember(activeReceiverId, allUsers) {
        if (activeReceiverId == "admin") "Wealth Support Admin"
        else allUsers.find { it.uid == activeReceiverId }?.displayName ?: "Merchant Partner"
    }
    
    val filteredMessages = remember(messages, userId, activeReceiverId) {
        messages.filter {
            (it.senderId == userId && it.receiverId == activeReceiverId) ||
            (it.senderId == activeReceiverId && it.receiverId == userId)
        }.sortedBy { it.timestamp }
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Left Column: Channels Selector
        Column(
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Channels",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            // Channel 1: Platform Admin Support
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeReceiverId = "admin" },
                colors = CardDefaults.cardColors(
                    containerColor = if (activeReceiverId == "admin") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SupportAgent,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (activeReceiverId == "admin") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Support Admin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeReceiverId == "admin") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Text(
                "Active Store Sellers",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            val sellersList = remember(allUsers) { allUsers.filter { it.isSeller && it.uid != userId } }
            if (sellersList.isEmpty()) {
                Text(
                    "No active sellers registered.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                sellersList.forEach { seller ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeReceiverId = seller.uid },
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeReceiverId == seller.uid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (activeReceiverId == seller.uid) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                seller.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeReceiverId == seller.uid) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(1.dp))
        
        // Right Column: Chat Window and Input
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            // Chat Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (activeReceiverId == "admin") Icons.Default.VerifiedUser else Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(activeReceiverName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(if (activeReceiverId == "admin") "Secure support ticket chat" else "Direct message with merchant", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            
            // Messages Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No messages yet. Send a friendly Hello!", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(filteredMessages) { msg ->
                        val isMe = msg.senderId == userId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    if (!isMe) {
                                        Text(msg.senderName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        msg.message,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Input Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInputText,
                    onValueChange = { chatInputText = it },
                    placeholder = { Text("Type your message here...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                IconButton(
                    onClick = {
                        if (chatInputText.isNotBlank()) {
                            viewModel.sendChatMessage(
                                senderId = userId,
                                senderName = currentUserProfile?.displayName ?: "User",
                                receiverId = activeReceiverId,
                                messageText = chatInputText.trim()
                            ) { success ->
                                if (success) {
                                    chatInputText = ""
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to send message", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(40.dp)
                        .testTag("send_chat_message_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesTab(
    viewModel: WealthViewModel,
    userId: String,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val myOrders = remember(orders) { orders.filter { it.buyerId == userId } }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.loadAllOrders()
    }

    if (myOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Purchases Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "When you buy products from the marketplace, your manual orders will show up here for payment verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(myOrders.sortedByDescending { it.dateCreated }) { order ->
                var proofUrlInput by remember { mutableStateOf("") }
                var referenceInput by remember { mutableStateOf("") }
                var isSubmitting by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_order_card_${order.id}"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Order #${order.id}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val statusColor = when (order.status) {
                                "Awaiting Payment" -> Color(0xFFF4B400) // Gold
                                "Pending Verification" -> Color(0xFF8E24AA) // Purple
                                "Processing" -> Color(0xFF0F9D58) // Green
                                "Completed" -> Color(0xFF00796B) // Teal
                                "Cancelled" -> Color(0xFFD32F2F) // Red
                                else -> Color.Gray
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(order.status) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = statusColor,
                                    containerColor = statusColor.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Product Info
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(order.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Quantity: ${order.quantity} | Total: ₦${String.format("%,.2f", order.finalPayableAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Date Placed: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(order.dateCreated))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        // Display Admin Notes if present
                        if (order.adminNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Feedback from Administrator:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text(order.adminNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }

                        // Display Submissions if Pending Verification or Paid
                        if (order.status == "Pending Verification" || order.paymentProofUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Submitted Proof details:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    if (order.paymentReference.isNotEmpty()) {
                                        Text("Reference: ${order.paymentReference}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (order.paymentProofUrl.isNotEmpty()) {
                                        Text("Proof URL/File: ${order.paymentProofUrl}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                                            android.widget.Toast.makeText(context, "Proof source: ${order.paymentProofUrl}", android.widget.Toast.LENGTH_LONG).show()
                                        })
                                    }
                                }
                            }
                        }

                        // Action Panel for Manual Payment
                        if (order.status == "Awaiting Payment") {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Platform Payment Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Platform Payment Details 💳", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Please make a manual bank transfer of EXACTLY ₦${String.format("%,.2f", order.finalPayableAmount)} to:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Bank Name Row
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Bank: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("OPay", style = MaterialTheme.typography.bodySmall)
                                    }
                                    
                                    // Account Name Row
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Account Name: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("Chizaram W. Amajor", style = MaterialTheme.typography.bodySmall)
                                    }

                                    // Account Number Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Account Number: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("9162072645", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("9162072645"))
                                                android.widget.Toast.makeText(context, "Account number copied!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Upload Proof of Payment 📄", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Reference input
                            OutlinedTextField(
                                value = referenceInput,
                                onValueChange = { referenceInput = it },
                                label = { Text("Payment Reference (Optional)") },
                                placeholder = { Text("E.g., OPay Transfer Reference") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Proof Source input / Selector
                            OutlinedTextField(
                                value = proofUrlInput,
                                onValueChange = { proofUrlInput = it },
                                label = { Text("Proof of Payment (Image URL or File Name) *") },
                                placeholder = { Text("E.g., receipt_image.png or screenshot URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Presets for easy user testing
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Presets:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                AssistChip(
                                    onClick = { proofUrlInput = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?q=80&w=600" },
                                    label = { Text("Mock Receipt 1") }
                                )
                                AssistChip(
                                    onClick = { proofUrlInput = "receipt_reference_91823.png" },
                                    label = { Text("Mock Image 2") }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (proofUrlInput.isBlank()) {
                                        android.widget.Toast.makeText(context, "Proof of payment is required", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSubmitting = true
                                    viewModel.submitOrderPaymentProof(
                                        orderId = order.id,
                                        proofUrl = proofUrlInput,
                                        reference = referenceInput
                                    ) { success ->
                                        isSubmitting = false
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Payment submitted for verification!", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to submit payment. Try again.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_order_proof"),
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Payment for Verification", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verification is processed manually and can take up to 24 hours.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        if (order.status == "Pending Verification") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Payment verification in progress. Our team will verify your transfer and update the status within 24 hours.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
fun SponsoredMarketplaceAdCard(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ad.bannerUrl.isNotEmpty()) {
                AsyncImage(
                    model = ad.bannerUrl,
                    contentDescription = ad.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Column(modifier = androidx.compose.ui.Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SPONSORED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(ad.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text(ad.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(ad.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            
            Icon(Icons.Default.OpenInNew, contentDescription = "Open Link", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
}
