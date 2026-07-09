package com.example.data.model

import java.util.UUID

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Wealth Builder Pioneer",
    val monthlyGoal: Double = 5000.0,
    val currentSavedBalance: Double = 0.0,
    val dateCreated: Long = System.currentTimeMillis(),
    val badges: List<String> = listOf("Pioneer"),
    val selectedPath: String = "All", // "Affiliate", "Freelancing", "Digital Products", "Education"
    val accountStatus: String = "Unverified", // "Unverified", "Pending Verification", "Approved", "Rejected", "Expired"
    val paymentSubmittedTime: Long = 0L,
    val paymentProofUrl: String = "",
    val paymentProofName: String = "",
    val rejectionReason: String = "",
    val isAdmin: Boolean = false,
    val referralCode: String = "",
    val referredByCode: String = "",
    val referralLinkClicks: Int = 0,
    val referredUsersCount: Int = 0,
    val referralRewardsEarned: Double = 0.0,
    val referralBalance: Double = 0.0, // Custom field for the user's referral balance
    
    // New Multi-Vendor Marketplace Seller fields
    val isSeller: Boolean = false,
    val sellerBusinessName: String = "",
    val sellerStatus: String = "Approved", // Approved automatically for instant demo/testing, can be updated by admin if needed
    val sellerBalance: Double = 0.0,
    val sellerPendingBalance: Double = 0.0,
    val sellerTotalSales: Double = 0.0,
    
    // Version 1.0 Trust & Verification fields
    val sellerNationalId: String = "",
    val sellerBusinessRegistration: String = "",
    val sellerPhoneNumber: String = "",
    val sellerEmailVerified: Boolean = false,
    val sellerVerificationStatus: String = "Unverified", // "Unverified", "Pending Verification", "Verified", "Rejected"
    val isVerifiedSeller: Boolean = false
)

enum class EarningCategory {
    AFFILIATE_MARKETING,
    FREELANCING,
    DIGITAL_PRODUCTS,
    FINANCIAL_EDUCATION
}

data class Article(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val content: String,
    val category: EarningCategory,
    val readTimeMinutes: Int,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val tags: List<String>,
    val earningPotential: String, // e.g. "$100 - $10,000 / month"
    val iconName: String, // Material Icon name
    val isFeatured: Boolean = false
)

data class QuizQuestion(
    val id: String = UUID.randomUUID().toString(),
    val category: EarningCategory,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class SavedItem(
    val id: String = UUID.randomUUID().toString(),
    val articleId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val author: String = "Admin"
)

data class NotificationItem(
    val id: String = "",
    val userId: String = "", // empty if sent to all
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "info" // "approval", "rejection", "payment", "announcement", "info"
)

data class AdminSettings(
    val registrationFee: Double = 5000.0,
    val isTimerEnabled: Boolean = true,
    val bankName: String = "OPay",
    val accountNumber: String = "9162072645",
    val accountName: String = "Chizaram W. Amajor",
    val siteName: String = "Wealth Builder",
    val siteDescription: String = "Build Legitimate Wealth. Learn scalable systems.",
    val websiteLogoUrl: String = "",
    val websiteBannerUrl: String = "",
    val isReferralProgramEnabled: Boolean = true,
    val referralRewardAmount: Double = 1500.0,
    val grantRewardOnStatus: String = "Approved", // "Approved", "Pending", "Registration"
    val homepageHeroTitle: String = "Build Legitimate Wealth Online",
    val homepageHeroSubtitle: String = "Discover scalable, ethical, and highly lucrative digital business models today.",
    val aboutText: String = "Wealth Builder is a premium e-learning and tracking platform designed to help aspiring digital entrepreneurs build long-term, scalable incomes through affiliate marketing, freelancing, digital products, and sound financial education.",
    val contactEmail: String = "wealthbuildars@gmail.com",
    val contactPhone: String = "+2349162072645",
    val businessHours: String = "Mon - Fri: 9:00 AM - 5:00 PM",
    val googleMapsAddress: String = "Lagos, Nigeria",
    val footerCopyright: String = "© 2026 Wealth Builder. All Rights Reserved.",
    val websiteColorPrimary: String = "#0F9D58", // Green hex
    val websiteColorSecondary: String = "#F4B400", // Gold hex
    val websiteColorBackground: String = "#FFFFFF", // White hex
    val featuresList: List<String> = listOf(
        "Premium Educational Blueprints|Detailed, step-by-step guides on scalable online businesses.",
        "Interactive Earning Trackers|Monitor your goals, saved balances, and referral earnings dynamically.",
        "Active Referral Network|Invite others and earn instant commission upon their account activation.",
        "Dedicated Support System|Instant ticketing channel to get help from expert financial tutors."
    ),
    val faqsList: List<String> = listOf(
        "Is the registration fee a one-time payment?|Yes! The registration is a strict one-time setup fee with no recurring charges.",
        "How long does the approval process take?|Our admin team works around the clock to verify transfers within 1 to 24 hours.",
        "How does the referral program work?|Once approved, you get a unique link/code. When friends register and get verified, you earn instant commissions!"
    ),
    val testimonialsList: List<String> = listOf(
        "Tunde A.|The affiliate blueprint transformed my approach entirely. Within 3 weeks, my first OPay alerts started rolling in!",
        "Chinedu O.|Wealth Builder provides genuine, step-by-step actions. No MLM, no empty promises. Strictly systems that scale.",
        "Amina Y.|The Support Center is incredibly responsive. I asked about freelancers' invoice tools and got an advisor response in 2 hours!"
    ),
    
    // New Admin configurations for multi-vendor revenue sharing and discount
    val sellerRevenuePercent: Double = 50.0,
    val affiliateRevenuePercent: Double = 40.0,
    val platformRevenuePercent: Double = 10.0,
    val isCustomerDiscountEnabled: Boolean = true,
    val customerDiscountPercent: Double = 5.0
)

data class ReferralRecord(
    val id: String = "",
    val referrerUid: String = "",
    val referredUid: String = "",
    val referredEmail: String = "",
    val referredDisplayName: String = "",
    val referralCode: String = "",
    val status: String = "Pending", // "Pending", "Active" (Approved), "Rejected", "Expired"
    val dateCreated: Long = System.currentTimeMillis(),
    val rewardAmount: Double = 0.0,
    val isRewardGranted: Boolean = false,
    val rewardGrantedTime: Long = 0L
)

data class RecentActivity(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class LeaderboardEntry(
    val displayName: String = "",
    val referralsCount: Int = 0,
    val totalRewards: Double = 0.0,
    val rank: Int = 0
)

data class TicketMessage(
    val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val subject: String = "",
    val description: String = "",
    val status: String = "Open", // "Open", "Replied", "Closed"
    val dateCreated: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val messages: List<TicketMessage> = emptyList()
)

data class AffiliateProduct(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val subcategory: String = "",
    val brand: String = "",
    val images: List<String> = emptyList(), // Can be URLs or drawable descriptors
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val currency: String = "USD",
    val affiliateLink: String = "",
    val merchantName: String = "",
    val stockStatus: String = "In Stock", // "In Stock", "Out of Stock", "Low Stock"
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val specifications: Map<String, String> = emptyMap(),
    val dateAdded: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isRecommended: Boolean = false,
    val isArchived: Boolean = false,
    val partnerId: String = "", // Links to AffiliatePartner
    val tags: List<String> = emptyList(),
    val commissionPercent: Double = 40.0, // Default commission percentage (40% as requested)
    
    // New Multi-Vendor Marketplace fields
    val status: String = "Approved", // "Pending Review", "Approved", "Rejected"
    val sellerId: String = "", // Empty if created by admin
    val sellerName: String = "",
    val rejectionReason: String = "",
    val availableQuantity: Int = 100,
    
    // Delivery fields (Version 1.0)
    val deliveryFee: Double = 0.0,
    val deliveryRegions: String = "All Regions",
    val estimatedDeliveryTime: String = "3-5 business days"
)

data class ProductReferralSale(
    val id: String = "",
    val referrerUid: String = "",
    val referrerEmail: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val buyerName: String = "",
    val buyerEmail: String = "",
    val salePrice: Double = 0.0,
    val commissionEarned: Double = 0.0,
    val dateSubmitted: Long = System.currentTimeMillis(),
    val status: String = "Pending Approval", // "Pending Approval", "Completed", "Rejected"
    val paymentReference: String = "",
    val rejectionReason: String = ""
)

data class WithdrawalRequest(
    val id: String = "",
    val userUid: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val payoutMethod: String = "", // e.g. "Bank Transfer", "PayPal", "USDT"
    val payoutDetails: String = "", // e.g. Account Number or Crypto Address
    val dateSubmitted: Long = System.currentTimeMillis(),
    val status: String = "Pending Approval", // "Pending Approval", "Approved", "Paid", "Rejected"
    val completionDate: Long = 0L,
    val transactionHash: String = "",
    val walletType: String = "Affiliate", // "Affiliate" or "Seller"
    val adminNotes: String = ""
)

data class AffiliatePartner(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "Active", // "Active", "Inactive"
    val logoUrl: String = "",
    val commissionRate: Double = 0.0,
    val websiteUrl: String = "",
    val apiKey: String = "",
    val apiSecret: String = "",
    val trackingId: String = "", // e.g. Amazon Associate Tag
    val extraConfig: Map<String, String> = emptyMap()
)

data class MarketplaceCategory(
    val id: String = "",
    val name: String = "",
    val subcategories: List<String> = emptyList()
)

data class MarketplaceBrand(
    val id: String = "",
    val name: String = ""
)

data class ProductAnalytics(
    val productId: String = "",
    val views: Long = 0L,
    val clicks: Long = 0L
)

data class UserWishlist(
    val userId: String = "",
    val productIds: List<String> = emptyList()
)

data class MarketplaceAnalytics(
    val totalProducts: Int = 0,
    val totalProductViews: Long = 0L,
    val totalAffiliateClicks: Long = 0L,
    val productViews: Map<String, Long> = emptyMap(),
    val productClicks: Map<String, Long> = emptyMap()
)

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerEmail: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 1,
    val originalPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val finalPayableAmount: Double = 0.0,
    val affiliateId: String = "", // affiliate who referred this purchase
    val sellerId: String = "", // seller of the product
    val status: String = "Pending", // "Pending", "Processing", "Shipped", "Delivered", "Completed", "Cancelled", "Refunded"
    val dateCreated: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val shippingAddress: String = "",
    val paymentMethod: String = "Wallet Balance",
    val couponCode: String = "",
    val deliveryFee: Double = 0.0,
    val paymentProofUrl: String = "",
    val paymentReference: String = "",
    val adminNotes: String = ""
)

data class ProductReview(
    val id: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val rating: Int = 5, // 1-5 stars
    val reviewText: String = "",
    val imageUrl: String? = null,
    val dateCreated: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "", // "admin" or sellerUid
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Coupon(
    val code: String = "",
    val discountType: String = "Percentage", // "Percentage" or "Fixed"
    val discountValue: Double = 0.0,
    val expiryDate: Long = 0L,
    val usageLimit: Int = 100,
    val usageCount: Int = 0
)

data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val action: String = "", // e.g. "Registration", "Login", "Product upload", "Purchase", "Payment verification", "Withdrawal"
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AdCampaign(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val title: String = "",
    val description: String = "",
    val bannerUrl: String = "",
    val videoUrl: String = "",
    val destinationUrl: String = "",
    val category: String = "",
    val adType: String = "", // e.g. "Homepage Banner", "Sidebar Banner", "Product Sponsored Ads", "Search Result Sponsored Ads", "Featured Seller Ads", "Popup Advertisements", "In-App Promotional Cards"
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: String = "Awaiting Payment", // "Awaiting Payment", "Pending Approval", "Active", "Paused", "Rejected", "Completed", "Expired"
    val budget: Double = 0.0,
    val pricePaid: Double = 0.0,
    val planName: String = "", // Basic, Standard, Premium, Featured Homepage Banner
    val dateCreated: Long = System.currentTimeMillis(),
    val isFeatured: Boolean = false,
    val adminNotes: String = "",
    val viewsCount: Int = 0,
    val clicksCount: Int = 0,
    val paymentProofUrl: String = "",
    val paymentReference: String = ""
)

data class AdPackage(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val durationDays: Int = 30,
    val description: String = "",
    val adType: String = ""
)

data class AdAnalyticsDaily(
    val id: String = "",
    val campaignId: String = "",
    val dateString: String = "", // e.g. "2026-07-09"
    val views: Int = 0,
    val clicks: Int = 0,
    val revenue: Double = 0.0
)



