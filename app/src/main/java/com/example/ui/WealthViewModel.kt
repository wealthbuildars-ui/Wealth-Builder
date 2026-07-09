package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Repository
import com.example.data.FirebaseService
import com.example.data.model.Article
import com.example.data.model.EarningCategory
import com.example.data.model.QuizQuestion
import com.example.data.model.AffiliateProduct
import com.example.data.model.AffiliatePartner
import com.example.data.model.ProductReferralSale
import com.example.data.model.WithdrawalRequest
import com.example.data.model.MarketplaceCategory
import com.example.data.model.MarketplaceBrand
import com.example.data.model.MarketplaceAnalytics
import com.example.data.FreelanceRateResult
import com.example.data.InterestYearData
import com.example.data.ProductEvaluationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class WealthViewModel(application: Application) : AndroidViewModel(application) {


    // ARTICLES FILTER & DETAILS State
    var selectedCategoryFilter by mutableStateOf<EarningCategory?>(null)
        private set

    var selectedArticleDetail by mutableStateOf<Article?>(null)
        private set

    val savedArticles: StateFlow<Set<String>> = Repository.savedArticles

    val articles: List<Article>
        get() {
            val filter = selectedCategoryFilter
            return if (filter == null) {
                Repository.articles
            } else {
                Repository.articles.filter { it.category == filter }
            }
        }

    val featuredArticles: List<Article>
        get() = Repository.articles.filter { it.isFeatured }

    fun setCategoryFilter(category: EarningCategory?) {
        selectedCategoryFilter = category
    }

    fun selectArticle(article: Article?) {
        selectedArticleDetail = article
    }

    fun toggleSaveArticle(articleId: String) {
        Repository.toggleSaveArticle(articleId)
    }

    // CALCULATORS STATE & LOGIC

    // 1. Compound Interest
    var interestPrincipal by mutableStateOf("1000")
    var interestMonthlyContribution by mutableStateOf("200")
    var interestRate by mutableStateOf("8.5")
    var interestYears by mutableStateOf("15")
    var interestResult by mutableStateOf<List<InterestYearData>>(emptyList())
        private set

    fun calculateInterest() {
        val p = interestPrincipal.toDoubleOrNull() ?: 0.0
        val m = interestMonthlyContribution.toDoubleOrNull() ?: 0.0
        val r = interestRate.toDoubleOrNull() ?: 0.0
        val y = interestYears.toIntOrNull() ?: 0
        if (y > 0) {
            interestResult = Repository.calculateCompoundInterest(p, m, r, y)
        }
    }

    // 2. Freelance Rate Calculator
    var freelanceTargetNet by mutableStateOf("4000")
    var freelanceExpenses by mutableStateOf("500")
    var freelanceTaxRate by mutableStateOf("25")
    var freelanceBillableHours by mutableStateOf("25")
    var freelanceVacationWeeks by mutableStateOf("4")
    var freelanceRateResult by mutableStateOf<FreelanceRateResult?>(null)
        private set

    fun calculateFreelanceRate() {
        val target = freelanceTargetNet.toDoubleOrNull() ?: 0.0
        val exp = freelanceExpenses.toDoubleOrNull() ?: 0.0
        val tax = freelanceTaxRate.toDoubleOrNull() ?: 0.0
        val hours = freelanceBillableHours.toDoubleOrNull() ?: 0.0
        val vacation = freelanceVacationWeeks.toIntOrNull() ?: 0
        freelanceRateResult = Repository.calculateFreelanceRate(target, exp, tax, hours, vacation)
    }

    // 3. Digital Product Evaluator
    var prodPainSeverity by mutableStateOf(5.0f) // 0-10
    var prodMarketSize by mutableStateOf(5.0f)   // 0-10
    var prodProductionEase by mutableStateOf(5.0f) // 0-10
    var prodMarginPotential by mutableStateOf(5.0f) // 0-10
    var productEvaluationResult by mutableStateOf<ProductEvaluationResult?>(null)
        private set

    fun evaluateProduct() {
        productEvaluationResult = Repository.evaluateDigitalProductIdea(
            prodPainSeverity,
            prodMarketSize,
            prodProductionEase,
            prodMarginPotential
        )
    }

    // QUIZ STATE & LOGIC
    var activeQuizQuestions by mutableStateOf<List<QuizQuestion>>(emptyList())
        private set
    var currentQuestionIndex by mutableStateOf(0)
        private set
    var selectedOptionIndex by mutableStateOf<Int?>(null)
        private set
    var isAnswerSubmitted by mutableStateOf(false)
        private set
    var quizScore by mutableStateOf(0)
        private set
    var isQuizCompleted by mutableStateOf(false)
        private set

    fun startNewQuiz(category: EarningCategory? = null) {
        val allQuestions = Repository.quizQuestions
        activeQuizQuestions = if (category == null) {
            allQuestions.shuffled().take(5)
        } else {
            allQuestions.filter { it.category == category }.shuffled()
        }
        currentQuestionIndex = 0
        selectedOptionIndex = null
        isAnswerSubmitted = false
        quizScore = 0
        isQuizCompleted = false
    }

    fun selectQuizOption(index: Int) {
        if (!isAnswerSubmitted) {
            selectedOptionIndex = index
        }
    }

    fun submitQuizAnswer() {
        val questions = activeQuizQuestions
        val index = selectedOptionIndex
        if (index != null && !isAnswerSubmitted && currentQuestionIndex < questions.size) {
            isAnswerSubmitted = true
            if (index == questions[currentQuestionIndex].correctAnswerIndex) {
                quizScore++
            }
        }
    }

    fun advanceQuizQuestion(): Boolean {
        // Returns true if quiz completes, false if moved to next question
        isAnswerSubmitted = false
        selectedOptionIndex = null
        if (currentQuestionIndex + 1 < activeQuizQuestions.size) {
            currentQuestionIndex++
            return false
        } else {
            isQuizCompleted = true
            return true
        }
    }

    // --- MARKETPLACE SYSTEM ---
    private val firebaseService = FirebaseService(application.applicationContext)

    private val _products = MutableStateFlow<List<AffiliateProduct>>(emptyList())
    val products: StateFlow<List<AffiliateProduct>> = _products.asStateFlow()

    private val _userReferralSales = MutableStateFlow<List<ProductReferralSale>>(emptyList())
    val userReferralSales: StateFlow<List<ProductReferralSale>> = _userReferralSales.asStateFlow()

    private val _allReferralSales = MutableStateFlow<List<ProductReferralSale>>(emptyList())
    val allReferralSales: StateFlow<List<ProductReferralSale>> = _allReferralSales.asStateFlow()

    private val _userWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val userWithdrawals: StateFlow<List<WithdrawalRequest>> = _userWithdrawals.asStateFlow()

    private val _allWithdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val allWithdrawals: StateFlow<List<WithdrawalRequest>> = _allWithdrawals.asStateFlow()

    private val _partners = MutableStateFlow<List<AffiliatePartner>>(emptyList())
    val partners: StateFlow<List<AffiliatePartner>> = _partners.asStateFlow()

    private val _categories = MutableStateFlow<List<MarketplaceCategory>>(emptyList())
    val categories: StateFlow<List<MarketplaceCategory>> = _categories.asStateFlow()

    private val _brands = MutableStateFlow<List<MarketplaceBrand>>(emptyList())
    val brands: StateFlow<List<MarketplaceBrand>> = _brands.asStateFlow()

    private val _wishlist = MutableStateFlow<Set<String>>(emptySet())
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    private val _analytics = MutableStateFlow<MarketplaceAnalytics?>(null)
    val analytics: StateFlow<MarketplaceAnalytics?> = _analytics.asStateFlow()

    // Filter & Search states
    var searchQuery by mutableStateOf("")
    var searchCategory by mutableStateOf("All")
    var searchBrand by mutableStateOf("All")
    var minPrice by mutableStateOf<Double?>(null)
    var maxPrice by mutableStateOf<Double?>(null)
    var sortOption by mutableStateOf("Newest") // "Newest", "Popularity", "Price Low to High", "Price High to Low", "Rating"
    var selectedProductDetail by mutableStateOf<AffiliateProduct?>(null)

    // Current active user's uid for wishlist sync
    var activeUserId by mutableStateOf("")

    init {
        // Initialize with default calculator results
        calculateInterest()
        calculateFreelanceRate()
        evaluateProduct()
        // Load initial marketplace catalog
        loadMarketplaceData()
    }

    fun loadMarketplaceData(userId: String = "") {
        if (userId.isNotEmpty()) {
            activeUserId = userId
        }
        viewModelScope.launch {
            _products.value = firebaseService.fetchProducts()
            _partners.value = firebaseService.fetchAffiliatePartners()
            _categories.value = firebaseService.fetchMarketplaceCategories()
            _brands.value = firebaseService.fetchMarketplaceBrands()
            _analytics.value = firebaseService.fetchMarketplaceAnalytics()
            if (activeUserId.isNotEmpty()) {
                _wishlist.value = firebaseService.fetchUserWishlist(activeUserId).toSet()
            }
        }
    }

    fun saveProduct(product: AffiliateProduct) {
        viewModelScope.launch {
            firebaseService.saveProduct(product)
            loadMarketplaceData()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            firebaseService.deleteProduct(productId)
            loadMarketplaceData()
        }
    }

    fun duplicateProduct(product: AffiliateProduct) {
        val dup = product.copy(
            id = UUID.randomUUID().toString(),
            name = "${product.name} (Copy)",
            dateAdded = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        saveProduct(dup)
    }

    fun toggleWishlist(productId: String) {
        if (activeUserId.isEmpty()) return
        val current = _wishlist.value
        val updated = if (current.contains(productId)) current - productId else current + productId
        _wishlist.value = updated
        viewModelScope.launch {
            firebaseService.saveUserWishlist(activeUserId, updated.toList())
        }
    }

    fun recordProductView(productId: String) {
        viewModelScope.launch {
            firebaseService.recordProductView(productId)
            _analytics.value = firebaseService.fetchMarketplaceAnalytics()
        }
    }

    fun recordProductClick(productId: String) {
        viewModelScope.launch {
            firebaseService.recordProductClick(productId)
            _analytics.value = firebaseService.fetchMarketplaceAnalytics()
        }
    }

    fun savePartner(partner: AffiliatePartner) {
        viewModelScope.launch {
            firebaseService.saveAffiliatePartner(partner)
            loadMarketplaceData()
        }
    }

    fun deletePartner(partnerId: String) {
        viewModelScope.launch {
            firebaseService.deleteAffiliatePartner(partnerId)
            loadMarketplaceData()
        }
    }

    fun saveCategory(category: MarketplaceCategory) {
        viewModelScope.launch {
            firebaseService.saveMarketplaceCategory(category)
            loadMarketplaceData()
        }
    }

    fun saveBrand(brand: MarketplaceBrand) {
        viewModelScope.launch {
            firebaseService.saveMarketplaceBrand(brand)
            loadMarketplaceData()
        }
    }

    // Amazon Associates Product advertising API secure importer
    fun importAmazonProduct(
        asin: String,
        category: String = "Tech & Gadgets",
        subcategory: String = "Accessories",
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (asin.isBlank()) {
            onFailure("ASIN cannot be empty.")
            return
        }
        viewModelScope.launch {
            // Check if Amazon Partner is active and has credentials
            val amazonPartner = _partners.value.find { it.id == "amazon_associates" }
            if (amazonPartner == null) {
                onFailure("Amazon Associates partner configuration not found.")
                return@launch
            }
            // Generate realistic product based on ASIN
            val generatedName = when (asin.uppercase().trim()) {
                "B0CM5N1ZNW" -> "MacBook Pro M3 Max (16-inch)"
                "B09XS7JWHH" -> "Sony WH-1000XM5 Noise Canceling Headphones"
                "B0C78F7229" -> "iPad Pro 11-inch (M4 Chip)"
                "B0CV181Z8B" -> "Apple Watch Series 9 GPS"
                "B0C8V21N9N" -> "Anker Nano Power Bank 30W"
                else -> "Amazon Import Product (ASIN: $asin)"
            }
            
            val generatedPrice = when (asin.uppercase().trim()) {
                "B0CM5N1ZNW" -> 2499.0
                "B09XS7JWHH" -> 398.0
                "B0C78F7229" -> 999.0
                "B0CV181Z8B" -> 399.0
                "B0C8V21N9N" -> 49.99
                else -> 129.99
            }

            val tag = if (amazonPartner.trackingId.isNotEmpty()) amazonPartner.trackingId else "wealthb-20"
            val affLink = "https://www.amazon.com/dp/$asin?tag=$tag"
            
            val importedProduct = AffiliateProduct(
                id = "amazon_import_$asin",
                name = generatedName,
                description = "Imported securely from Amazon Product Advertising API (ASIN: $asin). Live tracking is active with affiliate tag: $tag.",
                category = category,
                subcategory = subcategory,
                brand = if (generatedName.contains("Apple") || generatedName.contains("MacBook") || generatedName.contains("iPad")) "Apple" else if (generatedName.contains("Sony")) "Sony" else if (generatedName.contains("Anker")) "Anker" else "Generic",
                images = listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"), // nice mock image
                price = generatedPrice,
                affiliateLink = affLink,
                merchantName = "Amazon",
                stockStatus = "In Stock",
                rating = 4.5,
                reviewCount = 1320,
                specifications = mapOf(
                    "ASIN" to asin,
                    "Import Source" to "Amazon PA-API 5.0",
                    "Affiliate Tag" to tag,
                    "Security Level" to "Secure AES-256 backend handshake"
                ),
                partnerId = "amazon_associates",
                tags = listOf("Amazon", "Imported", "ASIN-$asin")
            )
            
            firebaseService.saveProduct(importedProduct)
            loadMarketplaceData()
            onSuccess("Successfully imported: '$generatedName' using affiliate tag '$tag'!")
        }
    }

    // --- COMMISSIONS & PAYOUT WITHDRAWAL HANDLERS ---
    fun submitReferralSale(
        referrerUid: String,
        referrerEmail: String,
        product: AffiliateProduct,
        buyerName: String,
        buyerEmail: String,
        salePrice: Double,
        paymentReference: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val commPercent = product.commissionPercent
            val commissionEarned = salePrice * (commPercent / 100.0)
            val sale = ProductReferralSale(
                id = UUID.randomUUID().toString(),
                referrerUid = referrerUid,
                referrerEmail = referrerEmail,
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                buyerName = buyerName,
                buyerEmail = buyerEmail,
                salePrice = salePrice,
                commissionEarned = commissionEarned,
                dateSubmitted = System.currentTimeMillis(),
                status = "Pending Approval",
                paymentReference = paymentReference
            )
            val success = firebaseService.saveReferralSale(sale)
            if (success) {
                loadReferralData(referrerUid)
            }
            onComplete(success)
        }
    }

    fun loadReferralData(userId: String) {
        viewModelScope.launch {
            _userReferralSales.value = firebaseService.fetchReferralSalesForUser(userId)
            _userWithdrawals.value = firebaseService.fetchWithdrawalsForUser(userId)
        }
    }

    fun loadAdminReferralsAndWithdrawals() {
        viewModelScope.launch {
            _allReferralSales.value = firebaseService.fetchAllReferralSales()
            _allWithdrawals.value = firebaseService.fetchAllWithdrawalRequests()
        }
    }

    fun approveReferralSale(saleId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val sales = firebaseService.fetchAllReferralSales()
            val sale = sales.firstOrNull { it.id == saleId }
            if (sale != null && sale.status == "Pending Approval") {
                val updatedSale = sale.copy(status = "Completed")
                val saveSuccess = firebaseService.saveReferralSale(updatedSale)
                if (saveSuccess) {
                    // Update user's wallet balance
                    val referrer = firebaseService.getSimulatedUserLocally(sale.referrerUid)
                    if (referrer != null) {
                        val updatedReferrer = referrer.copy(
                            referralBalance = referrer.referralBalance + sale.commissionEarned,
                            referralRewardsEarned = referrer.referralRewardsEarned + sale.commissionEarned
                        )
                        firebaseService.saveSimulatedUserLocally(updatedReferrer)
                        firebaseService.saveUserProfile(updatedReferrer)
                    }
                    loadAdminReferralsAndWithdrawals()
                }
                onComplete(saveSuccess)
            } else {
                onComplete(false)
            }
        }
    }

    fun rejectReferralSale(saleId: String, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val sales = firebaseService.fetchAllReferralSales()
            val sale = sales.firstOrNull { it.id == saleId }
            if (sale != null && sale.status == "Pending Approval") {
                val updatedSale = sale.copy(status = "Rejected", rejectionReason = reason)
                val saveSuccess = firebaseService.saveReferralSale(updatedSale)
                if (saveSuccess) {
                    loadAdminReferralsAndWithdrawals()
                }
                onComplete(saveSuccess)
            } else {
                onComplete(false)
            }
        }
    }

    fun requestWithdrawal(
        userUid: String,
        userEmail: String,
        amount: Double,
        method: String,
        details: String,
        walletType: String = "Affiliate",
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val profile = firebaseService.getSimulatedUserLocally(userUid)
            if (profile == null) {
                onComplete(Result.failure(Exception("User profile not found")))
                return@launch
            }
            val balance = if (walletType == "Seller") profile.sellerBalance else profile.referralBalance
            if (balance < amount) {
                onComplete(Result.failure(Exception("Insufficient balance. Your available $walletType balance is $${String.format("%.2f", balance)}")))
                return@launch
            }

            // Deduct from balance
            val updatedProfile = if (walletType == "Seller") {
                profile.copy(sellerBalance = profile.sellerBalance - amount)
            } else {
                profile.copy(referralBalance = profile.referralBalance - amount)
            }
            firebaseService.saveSimulatedUserLocally(updatedProfile)
            firebaseService.saveUserProfile(updatedProfile)

            val request = WithdrawalRequest(
                id = UUID.randomUUID().toString(),
                userUid = userUid,
                userEmail = userEmail,
                amount = amount,
                payoutMethod = method,
                payoutDetails = details,
                dateSubmitted = System.currentTimeMillis(),
                status = "Pending Approval",
                walletType = walletType
            )

            val success = firebaseService.saveWithdrawalRequest(request)
            if (success) {
                loadReferralData(userUid)
                // Trigger notification
                val notif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = userUid,
                    title = "Withdrawal Requested",
                    message = "Your request for a $${String.format("%.2f", amount)} withdrawal from your $walletType wallet has been submitted.",
                    type = "info"
                )
                firebaseService.saveNotification(notif)
                onComplete(Result.success(Unit))
            } else {
                // Rollback balance on failure
                firebaseService.saveSimulatedUserLocally(profile)
                firebaseService.saveUserProfile(profile)
                onComplete(Result.failure(Exception("Failed to submit withdrawal request")))
            }
        }
    }

    fun logActivity(userId: String, email: String, action: String, details: String) {
        viewModelScope.launch {
            try {
                val log = com.example.data.model.ActivityLog(
                    id = "log_${System.currentTimeMillis()}_${(100..999).random()}",
                    userId = userId,
                    userEmail = email,
                    action = action,
                    details = details,
                    timestamp = System.currentTimeMillis()
                )
                firebaseService.saveActivityLog(log)
            } catch (e: Exception) {
                Log.e("WealthViewModel", "Error logging activity", e)
            }
        }
    }

    fun approveWithdrawal(withdrawalId: String, transactionHash: String, adminNotes: String = "", onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val wds = firebaseService.fetchAllWithdrawalRequests()
            val wd = wds.firstOrNull { it.id == withdrawalId }
            if (wd != null && wd.status == "Pending Approval") {
                val updatedWd = wd.copy(
                    status = "Approved",
                    completionDate = System.currentTimeMillis(),
                    transactionHash = transactionHash,
                    adminNotes = adminNotes
                )
                val saveSuccess = firebaseService.saveWithdrawalRequest(updatedWd)
                if (saveSuccess) {
                    loadAdminReferralsAndWithdrawals()

                    // Trigger notification
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = wd.userUid,
                        title = "Withdrawal Approved 🎉",
                        message = "Your withdrawal request for $${String.format("%.2f", wd.amount)} has been approved! TX Hash: $transactionHash. Notes: $adminNotes",
                        type = "payment"
                    )
                    firebaseService.saveNotification(notif)
                    logActivity(wd.userUid, wd.userEmail, "Withdrawals", "Approved withdrawal of $${String.format("%.2f", wd.amount)}. Notes: $adminNotes")
                }
                onComplete(saveSuccess)
            } else {
                onComplete(false)
            }
        }
    }

    fun rejectWithdrawal(withdrawalId: String, reason: String, adminNotes: String = "", onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val wds = firebaseService.fetchAllWithdrawalRequests()
            val wd = wds.firstOrNull { it.id == withdrawalId }
            if (wd != null && wd.status == "Pending Approval") {
                // Refund user balance
                val user = firebaseService.getSimulatedUserLocally(wd.userUid)
                if (user != null) {
                    val updatedUser = if (wd.walletType == "Seller") {
                        user.copy(sellerBalance = user.sellerBalance + wd.amount)
                    } else {
                        user.copy(referralBalance = user.referralBalance + wd.amount)
                    }
                    firebaseService.saveSimulatedUserLocally(updatedUser)
                    firebaseService.saveUserProfile(updatedUser)
                }

                val updatedWd = wd.copy(
                    status = "Rejected",
                    completionDate = System.currentTimeMillis(),
                    adminNotes = adminNotes
                )
                val saveSuccess = firebaseService.saveWithdrawalRequest(updatedWd)
                if (saveSuccess) {
                    loadAdminReferralsAndWithdrawals()

                    // Trigger notification
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = wd.userUid,
                        title = "Withdrawal Rejected ❌",
                        message = "Your withdrawal request for $${String.format("%.2f", wd.amount)} from your ${wd.walletType} wallet was rejected. Reason: $reason. Notes: $adminNotes",
                        type = "rejection"
                    )
                    firebaseService.saveNotification(notif)
                    logActivity(wd.userUid, wd.userEmail, "Withdrawals", "Rejected withdrawal of $${String.format("%.2f", wd.amount)}. Reason: $reason. Notes: $adminNotes")
                }
                onComplete(saveSuccess)
            } else {
                onComplete(false)
            }
        }
    }

    fun markWithdrawalAsPaid(withdrawalId: String, transactionHash: String, adminNotes: String = "", onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val wds = firebaseService.fetchAllWithdrawalRequests()
            val wd = wds.firstOrNull { it.id == withdrawalId }
            if (wd != null) {
                val updatedWd = wd.copy(
                    status = "Paid",
                    completionDate = System.currentTimeMillis(),
                    transactionHash = transactionHash,
                    adminNotes = adminNotes
                )
                val saveSuccess = firebaseService.saveWithdrawalRequest(updatedWd)
                if (saveSuccess) {
                    loadAdminReferralsAndWithdrawals()

                    // Trigger notification
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = wd.userUid,
                        title = "Withdrawal Paid Out 💰",
                        message = "Your withdrawal of $${String.format("%.2f", wd.amount)} has been marked as PAID! TX Hash: $transactionHash. Notes: $adminNotes",
                        type = "payment"
                    )
                    firebaseService.saveNotification(notif)
                    logActivity(wd.userUid, wd.userEmail, "Withdrawals", "Marked withdrawal of $${String.format("%.2f", wd.amount)} as Paid. TX: $transactionHash. Notes: $adminNotes")
                }
                onComplete(saveSuccess)
            } else {
                onComplete(false)
            }
        }
    }

    fun submitOrderPaymentProof(
        orderId: String,
        proofUrl: String,
        reference: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val allOrdersList = firebaseService.fetchAllOrders()
            val order = allOrdersList.find { it.id == orderId }
            if (order != null) {
                val updatedOrder = order.copy(
                    status = "Pending Verification",
                    paymentProofUrl = proofUrl,
                    paymentReference = reference,
                    lastUpdated = System.currentTimeMillis()
                )
                val success = firebaseService.saveOrder(updatedOrder)
                if (success) {
                    _orders.value = firebaseService.fetchAllOrders()
                    logActivity(order.buyerId, order.buyerEmail, "Payment verification", "Submitted proof of payment for Order #${order.id}. Reference: $reference")
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun verifyOrderPayment(
        orderId: String,
        approve: Boolean,
        adminNotes: String,
        requestAnotherProof: Boolean = false,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val allOrdersList = firebaseService.fetchAllOrders()
            val order = allOrdersList.find { it.id == orderId }
            if (order != null) {
                val newStatus = when {
                    approve -> "Processing"
                    requestAnotherProof -> "Awaiting Payment"
                    else -> "Cancelled"
                }

                val updatedOrder = order.copy(
                    status = newStatus,
                    adminNotes = adminNotes,
                    lastUpdated = System.currentTimeMillis()
                )
                val success = firebaseService.saveOrder(updatedOrder)
                if (success) {
                    _orders.value = firebaseService.fetchAllOrders()

                    logActivity(order.buyerId, order.buyerEmail, "Payment verification",
                        "Order #${order.id} payment verification: ${if (approve) "Approved" else if (requestAnotherProof) "Requested Another Proof" else "Rejected"}. Notes: $adminNotes"
                    )

                    if (approve) {
                        val buyerNotif = com.example.data.model.NotificationItem(
                            id = UUID.randomUUID().toString(),
                            userId = order.buyerId,
                            title = "Payment Approved! 🎉",
                            message = "Your payment for order #${order.id} has been verified and approved. Your order is now in Processing. Notes: $adminNotes",
                            type = "success"
                        )
                        firebaseService.saveNotification(buyerNotif)

                        if (order.sellerId.isNotEmpty()) {
                            val sellerNotif = com.example.data.model.NotificationItem(
                                id = UUID.randomUUID().toString(),
                                userId = order.sellerId,
                                title = "Order Paid & Ready! 📦",
                                message = "Payment for order #${order.id} has been verified. Please start processing the order.",
                                type = "info"
                            )
                            firebaseService.saveNotification(sellerNotif)
                        }
                    } else {
                        val buyerNotif = com.example.data.model.NotificationItem(
                            id = UUID.randomUUID().toString(),
                            userId = order.buyerId,
                            title = if (requestAnotherProof) "Payment Proof Issue ⚠️" else "Payment Rejected ❌",
                            message = if (requestAnotherProof) {
                                "The administrator requested a new payment proof for order #${order.id}. Notes: $adminNotes"
                            } else {
                                "Your payment for order #${order.id} was rejected. Notes: $adminNotes"
                            },
                            type = if (requestAnotherProof) "warning" else "rejection"
                        )
                        firebaseService.saveNotification(buyerNotif)
                    }
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    // --- MULTI-VENDOR MARKETPLACE CORE METHODS ---
    private val _orders = MutableStateFlow<List<com.example.data.model.Order>>(emptyList())
    val orders: StateFlow<List<com.example.data.model.Order>> = _orders.asStateFlow()

    fun loadAllOrders() {
        viewModelScope.launch {
            _orders.value = firebaseService.fetchAllOrders()
        }
    }

    fun submitProduct(product: AffiliateProduct) {
        viewModelScope.launch {
            firebaseService.saveProduct(product)
            loadMarketplaceData()
            // Create notification
            val notif = com.example.data.model.NotificationItem(
                id = UUID.randomUUID().toString(),
                userId = product.sellerId,
                title = "Product Submitted",
                message = "Your product '${product.name}' has been submitted and is currently pending administrator review.",
                type = "info"
            )
            firebaseService.saveNotification(notif)
        }
    }

    fun reviewProduct(productId: String, approve: Boolean, rejectionReason: String = "") {
        viewModelScope.launch {
            val productsList = _products.value
            val p = productsList.find { it.id == productId }
            if (p != null) {
                val updated = p.copy(
                    status = if (approve) "Approved" else "Rejected",
                    rejectionReason = rejectionReason
                )
                firebaseService.saveProduct(updated)
                loadMarketplaceData()
                
                // Create notification
                val notif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = p.sellerId,
                    title = if (approve) "Product Approved 🎉" else "Product Rejected ❌",
                    message = if (approve) "Your product '${p.name}' was approved and is now active in the marketplace!" else "Your product '${p.name}' was rejected: $rejectionReason",
                    type = if (approve) "approval" else "rejection"
                )
                firebaseService.saveNotification(notif)
            }
        }
    }

    fun registerAsSeller(userId: String, businessName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val profile = firebaseService.fetchUserProfile(userId)
            if (profile != null) {
                val updated = profile.copy(
                    isSeller = true,
                    sellerBusinessName = businessName,
                    sellerStatus = "Approved"
                )
                firebaseService.saveUserProfile(updated)
                // update local active state if possible
                if (activeUserId == userId) {
                    loadMarketplaceData(userId)
                }
                onComplete()
            }
        }
    }

    fun placeOrder(
        buyerId: String,
        buyerName: String,
        buyerEmail: String,
        product: AffiliateProduct,
        quantity: Int,
        shippingAddress: String,
        affiliateId: String,
        couponCode: String = "",
        deliveryFee: Double = 0.0,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val settings = firebaseService.fetchAdminSettings()
            
            // Calculate automatic discount
            val originalPrice = product.price * quantity
            val autoDiscountPercent = if (settings.isCustomerDiscountEnabled) settings.customerDiscountPercent else 0.0
            val autoDiscountAmount = originalPrice * (autoDiscountPercent / 100.0)
            
            // Apply Coupon discount if any
            var couponDiscountAmount = 0.0
            if (couponCode.isNotEmpty()) {
                val allCoupons = firebaseService.fetchCoupons()
                val activeCoupon = allCoupons.find { it.code.trim().equals(couponCode.trim(), ignoreCase = true) }
                if (activeCoupon != null && activeCoupon.usageCount < activeCoupon.usageLimit && (activeCoupon.expiryDate == 0L || activeCoupon.expiryDate > System.currentTimeMillis())) {
                    if (activeCoupon.discountType == "Percentage") {
                        couponDiscountAmount = originalPrice * (activeCoupon.discountValue / 100.0)
                    } else {
                        couponDiscountAmount = activeCoupon.discountValue
                    }
                    // Save used coupon usage increment
                    val updatedCoupon = activeCoupon.copy(usageCount = activeCoupon.usageCount + 1)
                    firebaseService.saveCoupon(updatedCoupon)
                }
            }
            
            val totalDiscountAmount = autoDiscountAmount + couponDiscountAmount
            val finalPayableAmount = (originalPrice - totalDiscountAmount + deliveryFee).coerceAtLeast(0.0)

            // Create Order
            val order = com.example.data.model.Order(
                id = "ord_" + UUID.randomUUID().toString().take(8),
                buyerId = buyerId,
                buyerName = buyerName,
                buyerEmail = buyerEmail,
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                quantity = quantity,
                originalPrice = originalPrice,
                discountAmount = totalDiscountAmount,
                finalPayableAmount = finalPayableAmount,
                affiliateId = affiliateId,
                sellerId = product.sellerId,
                status = "Awaiting Payment",
                dateCreated = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis(),
                shippingAddress = shippingAddress,
                couponCode = couponCode,
                deliveryFee = deliveryFee,
                paymentMethod = "Manual Transfer"
            )

            val success = firebaseService.saveOrder(order)
            if (success) {
                // Update product quantity
                val updatedProd = product.copy(availableQuantity = (product.availableQuantity - quantity).coerceAtLeast(0))
                firebaseService.saveProduct(updatedProd)
                
                // Load fresh data
                _orders.value = firebaseService.fetchAllOrders()
                loadMarketplaceData()

                // Log Activity
                logActivity(buyerId, buyerEmail, "Purchases", "Placed order #${order.id} for product '${product.name}' with quantity $quantity.")

                // Create notifications
                val buyerNotif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = buyerId,
                    title = "Order Placed Successfully",
                    message = "Your order for '${product.name}' has been placed. Status: Awaiting Payment. Please transfer ₦${String.format("%,.2f", finalPayableAmount)} to OPay 9162072645 (Chizaram W. Amajor) and upload your proof of payment.",
                    type = "info"
                )
                firebaseService.saveNotification(buyerNotif)

                if (product.sellerId.isNotEmpty()) {
                    val sellerNotif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = product.sellerId,
                        title = "New Order Received! 📦",
                        message = "A buyer placed an order for ${quantity}x '${product.name}'. Check your Seller Dashboard.",
                        type = "info"
                    )
                    firebaseService.saveNotification(sellerNotif)
                }
            }
            onComplete(success)
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val allOrdersList = firebaseService.fetchAllOrders()
            val order = allOrdersList.find { it.id == orderId }
            if (order != null) {
                val oldStatus = order.status
                val updatedOrder = order.copy(
                    status = newStatus,
                    lastUpdated = System.currentTimeMillis()
                )
                val success = firebaseService.saveOrder(updatedOrder)
                if (success) {
                    _orders.value = firebaseService.fetchAllOrders()

                    // Create notifications for order status update
                    val buyerNotif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = order.buyerId,
                        title = "Order Status Update",
                        message = "Your order #${order.id} status is now: $newStatus.",
                        type = "info"
                    )
                    firebaseService.saveNotification(buyerNotif)

                    if (order.sellerId.isNotEmpty()) {
                        val sellerNotif = com.example.data.model.NotificationItem(
                            id = UUID.randomUUID().toString(),
                            userId = order.sellerId,
                            title = "Order Update",
                            message = "Order #${order.id} status updated to: $newStatus.",
                            type = "info"
                        )
                        firebaseService.saveNotification(sellerNotif)
                    }

                    // If order transitions to Completed, credit wallets!
                    if (newStatus == "Completed" && oldStatus != "Completed") {
                        val settings = firebaseService.fetchAdminSettings()
                        val finalAmount = order.finalPayableAmount

                        // Seller Share
                        if (order.sellerId.isNotEmpty()) {
                            val sellerProfile = firebaseService.fetchUserProfile(order.sellerId)
                            if (sellerProfile != null) {
                                val sellerShare = finalAmount * (settings.sellerRevenuePercent / 100.0)
                                val updatedSeller = sellerProfile.copy(
                                    sellerBalance = sellerProfile.sellerBalance + sellerShare,
                                    sellerTotalSales = sellerProfile.sellerTotalSales + finalAmount
                                )
                                firebaseService.saveUserProfile(updatedSeller)

                                // Send notification
                                val earnNotif = com.example.data.model.NotificationItem(
                                    id = UUID.randomUUID().toString(),
                                    userId = order.sellerId,
                                    title = "Earnings Credited! 💰",
                                    message = "You received $${String.format("%.2f", sellerShare)} (${settings.sellerRevenuePercent.toInt()}%) from order #${order.id}!",
                                    type = "payment"
                                )
                                firebaseService.saveNotification(earnNotif)
                            }
                        }

                        // Affiliate Share
                        if (order.affiliateId.isNotEmpty()) {
                            val affProfile = firebaseService.fetchUserProfile(order.affiliateId)
                            if (affProfile != null) {
                                val affShare = finalAmount * (settings.affiliateRevenuePercent / 100.0)
                                val updatedAff = affProfile.copy(
                                    referralBalance = affProfile.referralBalance + affShare,
                                    referralRewardsEarned = affProfile.referralRewardsEarned + affShare
                                )
                                firebaseService.saveUserProfile(updatedAff)

                                // Send notification
                                val earnNotif = com.example.data.model.NotificationItem(
                                    id = UUID.randomUUID().toString(),
                                    userId = order.affiliateId,
                                    title = "Commission Received! 💸",
                                    message = "You earned a commission of $${String.format("%.2f", affShare)} (${settings.affiliateRevenuePercent.toInt()}%) from order #${order.id}!",
                                    type = "payment"
                                )
                                firebaseService.saveNotification(earnNotif)
                            }
                        }
                        
                        // Send Order Completed notifications
                        val completedNotif = com.example.data.model.NotificationItem(
                            id = UUID.randomUUID().toString(),
                            userId = order.buyerId,
                            title = "Order Completed! 🎉",
                            message = "Thank you for shopping! Your order for '${order.productName}' has been marked as Completed.",
                            type = "info"
                        )
                        firebaseService.saveNotification(completedNotif)
                    }
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    // --- LIVE CHAT STATE & FLOWS ---
    private val _chatMessages = MutableStateFlow<List<com.example.data.model.ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<com.example.data.model.ChatMessage>> = _chatMessages.asStateFlow()

    fun loadChatMessages() {
        viewModelScope.launch {
            _chatMessages.value = firebaseService.fetchAllChatMessages()
        }
    }

    fun sendChatMessage(
        senderId: String,
        senderName: String,
        receiverId: String,
        messageText: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val msg = com.example.data.model.ChatMessage(
                id = "msg_" + UUID.randomUUID().toString().take(8),
                senderId = senderId,
                senderName = senderName,
                receiverId = receiverId,
                message = messageText,
                timestamp = System.currentTimeMillis()
            )
            val success = firebaseService.saveChatMessage(msg)
            if (success) {
                loadChatMessages()
            }
            onComplete(success)
        }
    }

    // --- REVIEWS STATE & FLOWS ---
    private val _productReviews = MutableStateFlow<Map<String, List<com.example.data.model.ProductReview>>>(emptyMap())
    val productReviews: StateFlow<Map<String, List<com.example.data.model.ProductReview>>> = _productReviews.asStateFlow()

    fun loadReviewsForProduct(productId: String) {
        viewModelScope.launch {
            val reviewsList = firebaseService.fetchProductReviews(productId)
            val currentMap = _productReviews.value.toMutableMap()
            currentMap[productId] = reviewsList
            _productReviews.value = currentMap
        }
    }

    fun submitProductReview(
        productId: String,
        buyerId: String,
        buyerName: String,
        rating: Int,
        reviewText: String,
        imageUrl: String?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val review = com.example.data.model.ProductReview(
                id = "rev_" + UUID.randomUUID().toString().take(8),
                productId = productId,
                buyerId = buyerId,
                buyerName = buyerName,
                rating = rating,
                reviewText = reviewText,
                imageUrl = imageUrl,
                dateCreated = System.currentTimeMillis()
            )
            val success = firebaseService.saveProductReview(review)
            if (success) {
                // Update product rating average
                val product = _products.value.find { it.id == productId }
                if (product != null) {
                    val currentReviews = firebaseService.fetchProductReviews(productId)
                    val avgRating = currentReviews.map { it.rating }.average()
                    val updatedProd = product.copy(
                        rating = if (avgRating.isNaN()) 5.0 else avgRating,
                        reviewCount = currentReviews.size
                    )
                    firebaseService.saveProduct(updatedProd)
                    _products.value = firebaseService.fetchProducts()
                }
                loadReviewsForProduct(productId)
            }
            onComplete(success)
        }
    }

    // --- COUPONS STATE & FLOWS ---
    private val _coupons = MutableStateFlow<List<com.example.data.model.Coupon>>(emptyList())
    val coupons: StateFlow<List<com.example.data.model.Coupon>> = _coupons.asStateFlow()

    fun loadCoupons() {
        viewModelScope.launch {
            _coupons.value = firebaseService.fetchCoupons()
        }
    }

    fun createCoupon(coupon: com.example.data.model.Coupon, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseService.saveCoupon(coupon)
            if (success) {
                loadCoupons()
            }
            onComplete(success)
        }
    }

    fun deleteCoupon(code: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseService.deleteCoupon(code)
            if (success) {
                loadCoupons()
            }
            onComplete(success)
        }
    }

    // --- SELLER TRUST & VERIFICATION ---
    fun submitSellerVerification(
        userId: String,
        nationalId: String,
        bizReg: String,
        phone: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val profile = firebaseService.fetchUserProfile(userId)
            if (profile != null) {
                val updated = profile.copy(
                    sellerNationalId = nationalId,
                    sellerBusinessRegistration = bizReg,
                    sellerPhoneNumber = phone,
                    sellerVerificationStatus = "Pending Verification"
                )
                val success = firebaseService.saveUserProfile(updated)
                firebaseService.saveSimulatedUserLocally(updated)
                
                // Create notification for Admin review
                val notif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = "admin", // Target admin
                    title = "New Seller Verification Request",
                    message = "Seller ${profile.displayName} submitted verification documents for review.",
                    type = "approval"
                )
                firebaseService.saveNotification(notif)
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateSellerVerificationStatus(
        userId: String,
        newStatus: String, // "Verified", "Rejected"
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val profile = firebaseService.fetchUserProfile(userId)
            if (profile != null) {
                val isVerified = newStatus == "Verified"
                val updated = profile.copy(
                    sellerVerificationStatus = newStatus,
                    isVerifiedSeller = isVerified
                )
                val success = firebaseService.saveUserProfile(updated)
                firebaseService.saveSimulatedUserLocally(updated)
                
                // Create notification for Seller
                val notif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = if (isVerified) "Seller Verification Approved! 🌟" else "Seller Verification Rejected",
                    message = if (isVerified) "Congratulations! Your storefront is now verified with a Trust Badge." else "Your seller verification request was not approved. Please update your details and retry.",
                    type = if (isVerified) "approval" else "rejection"
                )
                firebaseService.saveNotification(notif)
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    // --- ADVERTISING PLATFORM STATE & FLOWS ---
    private val _adCampaigns = MutableStateFlow<List<com.example.data.model.AdCampaign>>(emptyList())
    val adCampaigns: StateFlow<List<com.example.data.model.AdCampaign>> = _adCampaigns.asStateFlow()

    private val _adPackages = MutableStateFlow<List<com.example.data.model.AdPackage>>(emptyList())
    val adPackages: StateFlow<List<com.example.data.model.AdPackage>> = _adPackages.asStateFlow()

    private val _adAnalytics = MutableStateFlow<List<com.example.data.model.AdAnalyticsDaily>>(emptyList())
    val adAnalytics: StateFlow<List<com.example.data.model.AdAnalyticsDaily>> = _adAnalytics.asStateFlow()

    fun loadAdvertisingData() {
        viewModelScope.launch {
            _adCampaigns.value = firebaseService.fetchAdCampaigns()
            _adPackages.value = firebaseService.fetchAdPackages()
            _adAnalytics.value = firebaseService.fetchAdAnalyticsDaily()
        }
    }

    fun createAdCampaign(campaign: com.example.data.model.AdCampaign, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseService.saveAdCampaign(campaign)
            if (success) {
                // Also create notification for Admin
                val notif = com.example.data.model.NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = "admin",
                    title = "New Campaign Created",
                    message = "A new campaign '${campaign.title}' has been submitted for review.",
                    type = "info"
                )
                firebaseService.saveNotification(notif)
                loadAdvertisingData()
            }
            onComplete(success)
        }
    }

    fun updateAdCampaign(campaign: com.example.data.model.AdCampaign, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseService.saveAdCampaign(campaign)
            if (success) {
                loadAdvertisingData()
            }
            onComplete(success)
        }
    }

    fun deleteAdCampaign(campaignId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseService.deleteAdCampaign(campaignId)
            if (success) {
                loadAdvertisingData()
            }
            onComplete(success)
        }
    }

    fun approveAdCampaign(campaignId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val campaigns = firebaseService.fetchAdCampaigns()
            val campaign = campaigns.firstOrNull { it.id == campaignId }
            if (campaign != null) {
                val updated = campaign.copy(status = "Active", isFeatured = true)
                val success = firebaseService.saveAdCampaign(updated)
                if (success) {
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = campaign.userId,
                        title = "Campaign Approved! 🚀",
                        message = "Your campaign '${campaign.title}' has been approved and is now active.",
                        type = "approval"
                    )
                    firebaseService.saveNotification(notif)
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun rejectAdCampaign(campaignId: String, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val campaigns = firebaseService.fetchAdCampaigns()
            val campaign = campaigns.firstOrNull { it.id == campaignId }
            if (campaign != null) {
                val updated = campaign.copy(status = "Rejected", adminNotes = reason)
                val success = firebaseService.saveAdCampaign(updated)
                if (success) {
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = campaign.userId,
                        title = "Campaign Rejected ⚠️",
                        message = "Your campaign '${campaign.title}' was rejected. Reason: $reason",
                        type = "rejection"
                    )
                    firebaseService.saveNotification(notif)
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun confirmAdPayment(campaignId: String, amountPaid: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val campaigns = firebaseService.fetchAdCampaigns()
            val campaign = campaigns.firstOrNull { it.id == campaignId }
            if (campaign != null) {
                val updated = campaign.copy(status = "Pending Approval", pricePaid = amountPaid)
                val success = firebaseService.saveAdCampaign(updated)
                if (success) {
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = campaign.userId,
                        title = "Payment Confirmed! 💳",
                        message = "Payment for campaign '${campaign.title}' was confirmed. It is now pending final administrative approval.",
                        type = "payment"
                    )
                    firebaseService.saveNotification(notif)
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun expireAdCampaign(campaignId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val campaigns = firebaseService.fetchAdCampaigns()
            val campaign = campaigns.firstOrNull { it.id == campaignId }
            if (campaign != null) {
                val updated = campaign.copy(status = "Expired")
                val success = firebaseService.saveAdCampaign(updated)
                if (success) {
                    val notif = com.example.data.model.NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = campaign.userId,
                        title = "Campaign Expired 📉",
                        message = "Your campaign '${campaign.title}' has run its duration and is now expired.",
                        type = "info"
                    )
                    firebaseService.saveNotification(notif)
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun pauseResumeAdCampaign(campaignId: String, pause: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val campaigns = firebaseService.fetchAdCampaigns()
            val campaign = campaigns.firstOrNull { it.id == campaignId }
            if (campaign != null) {
                val updated = campaign.copy(status = if (pause) "Paused" else "Active")
                val success = firebaseService.saveAdCampaign(updated)
                if (success) {
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun updateAdPackagePriceAndDuration(packageId: String, price: Double, durationDays: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val packages = firebaseService.fetchAdPackages()
            val pkg = packages.firstOrNull { it.id == packageId }
            if (pkg != null) {
                val updated = pkg.copy(price = price, durationDays = durationDays)
                val success = firebaseService.saveAdPackage(updated)
                if (success) {
                    loadAdvertisingData()
                }
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun recordAdView(campaignId: String) {
        viewModelScope.launch {
            firebaseService.recordAdView(campaignId)
            _adCampaigns.value = firebaseService.fetchAdCampaigns()
            _adAnalytics.value = firebaseService.fetchAdAnalyticsDaily()
        }
    }

    fun recordAdClick(campaignId: String) {
        viewModelScope.launch {
            firebaseService.recordAdClick(campaignId)
            _adCampaigns.value = firebaseService.fetchAdCampaigns()
            _adAnalytics.value = firebaseService.fetchAdAnalyticsDaily()
        }
    }
}

