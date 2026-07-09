package com.example.ui.auth

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseService
import com.example.data.model.UserProfile
import com.example.data.model.ReferralRecord
import com.example.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val firebaseService = FirebaseService(application.applicationContext)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _isTimerEnabled = MutableStateFlow(true)
    val isTimerEnabled: StateFlow<Boolean> = _isTimerEnabled.asStateFlow()

    val isFirebaseAvailable: StateFlow<Boolean> = firebaseService.isFirebaseAvailable

    // New state flows
    private val _announcements = MutableStateFlow<List<com.example.data.model.Announcement>>(emptyList())
    val announcements: StateFlow<List<com.example.data.model.Announcement>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow<List<com.example.data.model.NotificationItem>>(emptyList())
    val notifications: StateFlow<List<com.example.data.model.NotificationItem>> = _notifications.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<com.example.data.model.ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<com.example.data.model.ActivityLog>> = _activityLogs.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<com.example.data.model.RecentActivity>>(emptyList())
    val recentActivities: StateFlow<List<com.example.data.model.RecentActivity>> = _recentActivities.asStateFlow()

    private val _adminSettings = MutableStateFlow<com.example.data.model.AdminSettings>(com.example.data.model.AdminSettings())
    val adminSettings: StateFlow<com.example.data.model.AdminSettings> = _adminSettings.asStateFlow()

    private val _userReferrals = MutableStateFlow<List<ReferralRecord>>(emptyList())
    val userReferrals: StateFlow<List<ReferralRecord>> = _userReferrals.asStateFlow()

    private val _allReferrals = MutableStateFlow<List<ReferralRecord>>(emptyList())
    val allReferrals: StateFlow<List<ReferralRecord>> = _allReferrals.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _userTickets = MutableStateFlow<List<com.example.data.model.SupportTicket>>(emptyList())
    val userTickets: StateFlow<List<com.example.data.model.SupportTicket>> = _userTickets.asStateFlow()

    private val _allTickets = MutableStateFlow<List<com.example.data.model.SupportTicket>>(emptyList())
    val allTickets: StateFlow<List<com.example.data.model.SupportTicket>> = _allTickets.asStateFlow()

    init {
        _isTimerEnabled.value = firebaseService.isTimerVerificationEnabled()
        checkUserSession()
        loadAnnouncements()
        loadAdminSettings()
    }

    fun checkTimerExpiration(user: UserProfile): UserProfile {
        if (user.accountStatus == "Pending Verification" && firebaseService.isTimerVerificationEnabled()) {
            val elapsed = System.currentTimeMillis() - user.paymentSubmittedTime
            val limit = 24 * 60 * 60 * 1000L // 24 hours
            if (elapsed >= limit) {
                val expiredUser = user.copy(accountStatus = "Expired")
                viewModelScope.launch {
                    firebaseService.saveUserProfile(expiredUser)
                }
                return expiredUser
            }
        }
        return user
    }

    fun checkUserSession() {
        val user = firebaseService.getCurrentUser()
        if (user != null) {
            val checkedUser = checkTimerExpiration(user)
            _currentUser.value = checkedUser
            _uiState.value = AuthUiState.Authenticated(checkedUser)
            if (checkedUser.isAdmin) {
                fetchAllUsers()
                loadAllReferralRecords()
                loadAllSupportTickets()
            } else {
                loadUserSupportTickets(checkedUser.uid)
            }
            loadNotifications(checkedUser.uid)
            loadRecentActivities(checkedUser.uid)
            loadUserReferrals(checkedUser.uid)
            loadLeaderboard()
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                val users = firebaseService.fetchAllUserProfiles()
                _allUsers.value = users
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching users: ${e.message}")
            }
        }
    }

    fun setTimerEnabled(enabled: Boolean) {
        firebaseService.setTimerVerificationEnabled(enabled)
        _isTimerEnabled.value = enabled
        fetchAllUsers() // refresh
    }

    fun updateVerificationStatus(uid: String, status: String, rejectionReason: String = "") {
        viewModelScope.launch {
            try {
                val targetUser = firebaseService.fetchUserProfile(uid)
                if (targetUser != null) {
                    val code = if (targetUser.referralCode.isBlank() && status == "Approved") {
                        generateUniqueReferralCode(targetUser.displayName)
                    } else {
                        targetUser.referralCode
                    }
                    val updated = targetUser.copy(
                        accountStatus = status,
                        rejectionReason = rejectionReason,
                        referralCode = code
                    )
                    firebaseService.saveUserProfile(updated)
                    
                    // Trigger referral reward checking
                    grantReferralRewardIfApplicable(uid, status)

                    fetchAllUsers()
                    
                    // If we approved/rejected/expired the current user (e.g. self update/admin update), sync locally
                    if (_currentUser.value?.uid == uid) {
                        _currentUser.value = updated
                        _uiState.value = AuthUiState.Authenticated(updated)
                        loadNotifications(uid)
                        loadRecentActivities(uid)
                        loadUserReferrals(uid)
                    }

                    // Automatic notifications and activities on verification updates
                    val notifTitle = when (status) {
                        "Approved" -> "Account Activated! 🎉"
                        "Rejected" -> "Registration Rejected ❌"
                        "Expired" -> "Verification Expired ⚠️"
                        "Suspended" -> "Account Suspended"
                        else -> "Status Updated"
                    }
                    val notifMsg = when (status) {
                        "Approved" -> "Congratulations! Your payment proof was verified. You now have full access to Wealth Builder's premium members-only content, guides, tools, and community."
                        "Rejected" -> "We could not verify your payment proof. Reason: $rejectionReason. Please review the details and upload a valid payment proof."
                        "Expired" -> "Your 24-hour verification window has expired. Please re-submit payment proof or contact support."
                        "Suspended" -> "Your account has been suspended by an administrator. Reason: $rejectionReason"
                        else -> "Your registration status is now: $status."
                    }
                    sendNotification(
                        userId = uid,
                        title = notifTitle,
                        message = notifMsg,
                        type = when (status) {
                            "Approved" -> "approval"
                            "Rejected", "Expired", "Suspended" -> "rejection"
                            else -> "info"
                        }
                    )
                    addRecentActivity(
                        userId = uid,
                        title = "Verification Status: $status",
                        description = notifMsg
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating status: ${e.message}")
            }
        }
    }

    fun uploadProofAndSubmit(uri: Uri, fileName: String) {
        val user = _currentUser.value ?: return
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val downloadUrl = firebaseService.uploadPaymentProof(user.uid, uri, fileName)
                val updated = user.copy(
                    accountStatus = "Pending Verification",
                    paymentSubmittedTime = System.currentTimeMillis(),
                    paymentProofUrl = downloadUrl,
                    paymentProofName = fileName,
                    rejectionReason = ""
                )
                firebaseService.saveUserProfile(updated)
                _currentUser.value = updated
                _uiState.value = AuthUiState.Authenticated(updated)

                sendNotification(
                    userId = user.uid,
                    title = "Payment Proof Received",
                    message = "Your payment proof '$fileName' has been securely received. Our team will review and activate your account within 24 hours.",
                    type = "payment"
                )
                addRecentActivity(
                    userId = user.uid,
                    title = "Payment Proof Uploaded",
                    description = "Uploaded proof of payment: '$fileName'."
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to upload payment proof.")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = firebaseService.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    val checkedUser = checkTimerExpiration(user)
                    _currentUser.value = checkedUser
                    _uiState.value = AuthUiState.Authenticated(checkedUser)
                    logActivity(checkedUser.uid, checkedUser.email, "Login", "Successfully logged in.")
                    if (checkedUser.isAdmin) {
                        fetchAllUsers()
                        loadAllReferralRecords()
                    }
                    loadNotifications(checkedUser.uid)
                    loadRecentActivities(checkedUser.uid)
                    loadUserReferrals(checkedUser.uid)
                    loadLeaderboard()
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed.")
                }
            )
        }
    }

    fun register(email: String, password: String, displayName: String, referralCodeEntered: String = "") {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            var referrerProfile: UserProfile? = null
            if (referralCodeEntered.isNotBlank()) {
                val allProfiles = firebaseService.fetchAllUserProfiles()
                val foundReferrer = allProfiles.firstOrNull { it.referralCode.equals(referralCodeEntered.trim(), ignoreCase = true) }
                if (foundReferrer == null) {
                    _uiState.value = AuthUiState.Error("Invalid referral code entered.")
                    return@launch
                }
                if (foundReferrer.accountStatus != "Approved") {
                    _uiState.value = AuthUiState.Error("Referral program is only active for approved members.")
                    return@launch
                }
                if (foundReferrer.email.equals(email, ignoreCase = true)) {
                    _uiState.value = AuthUiState.Error("You cannot refer yourself.")
                    return@launch
                }
                referrerProfile = foundReferrer
            }

            val result = firebaseService.signUp(email, password, displayName, referredByCode = referralCodeEntered.trim())
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Authenticated(user)
                    logActivity(user.uid, user.email, "Registration", "Account registered successfully for ${user.displayName}.")
                    
                    if (referrerProfile != null) {
                        // Create a referral record
                        val refRecord = ReferralRecord(
                            id = "ref_rec_${System.currentTimeMillis()}_${(100..999).random()}",
                            referrerUid = referrerProfile.uid,
                            referredUid = user.uid,
                            referredEmail = user.email,
                            referredDisplayName = user.displayName,
                            referralCode = referrerProfile.referralCode,
                            status = "Pending",
                            dateCreated = System.currentTimeMillis(),
                            rewardAmount = 0.0,
                            isRewardGranted = false
                        )
                        firebaseService.saveReferralRecord(refRecord)
                        
                        // Check if we should immediately grant reward on Registration
                        grantReferralRewardIfApplicable(user.uid, user.accountStatus)
                    }

                    if (user.isAdmin) {
                        fetchAllUsers()
                        loadAllReferralRecords()
                    } else {
                        // send initial welcome notification
                        sendNotification(
                            userId = user.uid,
                            title = "Welcome to Wealth Builder! 🚀",
                            message = "Step 1: Please pay the ₦5,000 activation fee to OPay 9162072645 and upload your payment proof to activate your account.",
                            type = "info"
                        )
                        addRecentActivity(
                            userId = user.uid,
                            title = "Account Created",
                            description = "Welcome to the Wealth Builder family."
                        )
                        loadNotifications(user.uid)
                        loadRecentActivities(user.uid)
                        loadUserReferrals(user.uid)
                        loadLeaderboard()
                    }
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed.")
                }
            )
        }
    }

    fun signOut() {
        firebaseService.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState.Unauthenticated
        _notifications.value = emptyList()
        _recentActivities.value = emptyList()
    }

    fun updateProfile(displayName: String, monthlyGoal: Double, currentSavedBalance: Double, selectedPath: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            displayName = displayName,
            monthlyGoal = monthlyGoal,
            currentSavedBalance = currentSavedBalance,
            selectedPath = selectedPath
        )
        _currentUser.value = updated
        _uiState.value = AuthUiState.Authenticated(updated)

        viewModelScope.launch {
            firebaseService.saveUserProfile(updated)
            addRecentActivity(
                userId = current.uid,
                title = "Profile Updated",
                description = "Updated personal profile information."
            )
        }
    }

    fun earnMockCredits(amount: Double) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            currentSavedBalance = current.currentSavedBalance + amount
        )
        // Award badge for first earnings
        val badges = current.badges.toMutableList()
        if (updated.currentSavedBalance > 0 && !badges.contains("Earner")) {
            badges.add("Earner")
        }
        if (updated.currentSavedBalance >= updated.monthlyGoal && !badges.contains("Wealth Overlord")) {
            badges.add("Wealth Overlord")
        }
        val finalProfile = updated.copy(badges = badges)
        
        _currentUser.value = finalProfile
        _uiState.value = AuthUiState.Authenticated(finalProfile)

        viewModelScope.launch {
            firebaseService.saveUserProfile(finalProfile)
            addRecentActivity(
                userId = current.uid,
                title = "Simulated Balance Earned",
                description = "Added ₦${amount} to simulated balance."
            )
        }
    }

    fun addBadge(badge: String) {
        val current = _currentUser.value ?: return
        if (current.badges.contains(badge)) return
        val updated = current.copy(badges = current.badges + badge)
        _currentUser.value = updated
        _uiState.value = AuthUiState.Authenticated(updated)
        viewModelScope.launch {
            firebaseService.saveUserProfile(updated)
            addRecentActivity(
                userId = current.uid,
                title = "Badge Earned! 🏆",
                description = "Unlocked the '$badge' professional credential."
            )
            sendNotification(
                userId = current.uid,
                title = "New Badge Unlocked! 🏆",
                message = "Congratulations! You've unlocked the professional credential: $badge.",
                type = "info"
            )
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = if (_currentUser.value != null) {
                AuthUiState.Authenticated(_currentUser.value!!)
            } else {
                AuthUiState.Unauthenticated
            }
        }
    }

    fun updateProfile(displayName: String, selectedPath: String, monthlyGoal: Double) {
        viewModelScope.launch {
            try {
                val current = _currentUser.value
                if (current != null) {
                    val updated = current.copy(
                        displayName = displayName,
                        selectedPath = selectedPath,
                        monthlyGoal = monthlyGoal
                    )
                    firebaseService.saveUserProfile(updated)
                    _currentUser.value = updated
                    _uiState.value = AuthUiState.Authenticated(updated)
                    
                    addRecentActivity(
                        userId = current.uid,
                        title = "Profile Setup Completed ⚙️",
                        description = "Configured name to '$displayName' and selected $selectedPath blueprint."
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating profile details", e)
            }
        }
    }

    // ANNOUNCEMENTS
    fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val list = firebaseService.fetchAnnouncements()
                // If there are no announcements, add some default ones
                if (list.isEmpty()) {
                    val defaultAnn = com.example.data.model.Announcement(
                        id = "welcome_announcement",
                        title = "Welcome to the Elite Wealth Builder Platform! 🚀",
                        content = "We are thrilled to welcome you to the Wealth Builder Academy. Start your journey by exploring the core earning routes under the Discover tab. Check out our high-paying templates in the Toolbox, or complete the introductory finance quiz to earn your Analyst badge!",
                        timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                        isPinned = true,
                        author = "Head of Academy"
                    )
                    firebaseService.saveAnnouncement(defaultAnn)
                    _announcements.value = listOf(defaultAnn)
                } else {
                    _announcements.value = list
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading announcements", e)
            }
        }
    }

    fun createAnnouncement(title: String, content: String, isPinned: Boolean) {
        viewModelScope.launch {
            try {
                val newAnn = com.example.data.model.Announcement(
                    id = "ann_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    isPinned = isPinned,
                    author = "Academy Director"
                )
                firebaseService.saveAnnouncement(newAnn)
                loadAnnouncements()

                // Notify all users about a new announcement
                sendNotification(
                    userId = "", // sent to all
                    title = "New Announcement: $title 📢",
                    message = content,
                    type = "announcement"
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error creating announcement", e)
            }
        }
    }

    fun updateAnnouncement(announcement: com.example.data.model.Announcement) {
        viewModelScope.launch {
            try {
                firebaseService.saveAnnouncement(announcement)
                loadAnnouncements()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating announcement", e)
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            try {
                firebaseService.deleteAnnouncement(id)
                loadAnnouncements()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error deleting announcement", e)
            }
        }
    }

    // NOTIFICATIONS
    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            try {
                _notifications.value = firebaseService.fetchNotifications(userId)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading notifications", e)
            }
        }
    }

    fun sendNotification(userId: String, title: String, message: String, type: String = "info") {
        viewModelScope.launch {
            try {
                val notif = com.example.data.model.NotificationItem(
                    id = "not_${System.currentTimeMillis()}_${(100..999).random()}",
                    userId = userId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    type = type
                )
                firebaseService.saveNotification(notif)
                
                // If this is for current user, reload their notifications
                val currentUid = _currentUser.value?.uid
                if (currentUid != null && (userId == currentUid || userId.isEmpty())) {
                    loadNotifications(currentUid)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending notification", e)
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            try {
                val currentUid = _currentUser.value?.uid ?: return@launch
                val currentNotifs = _notifications.value
                val found = currentNotifs.firstOrNull { it.id == id }
                if (found != null) {
                    val updated = found.copy(isRead = true)
                    firebaseService.saveNotification(updated)
                    loadNotifications(currentUid)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error marking notification as read", e)
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                val currentUid = _currentUser.value?.uid ?: return@launch
                firebaseService.deleteNotification(id)
                loadNotifications(currentUid)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error deleting notification", e)
            }
        }
    }

    // ACTIVITY LOGS
    fun loadActivityLogs() {
        viewModelScope.launch {
            try {
                _activityLogs.value = firebaseService.fetchActivityLogs()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading activity logs", e)
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
                loadActivityLogs()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging activity", e)
            }
        }
    }

    // RECENT ACTIVITIES
    fun loadRecentActivities(userId: String) {
        viewModelScope.launch {
            try {
                _recentActivities.value = firebaseService.fetchRecentActivities(userId)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading activities", e)
            }
        }
    }

    fun addRecentActivity(userId: String, title: String, description: String) {
        viewModelScope.launch {
            try {
                val act = com.example.data.model.RecentActivity(
                    id = "act_${System.currentTimeMillis()}_${(100..999).random()}",
                    userId = userId,
                    title = title,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
                firebaseService.saveRecentActivity(act)
                
                val currentUid = _currentUser.value?.uid
                if (currentUid == userId) {
                    loadRecentActivities(userId)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error adding recent activity", e)
            }
        }
    }

    // ADMIN SETTINGS
    fun loadAdminSettings() {
        viewModelScope.launch {
            try {
                _adminSettings.value = firebaseService.fetchAdminSettings()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading admin settings", e)
            }
        }
    }

    fun updateAdminSettings(settings: com.example.data.model.AdminSettings) {
        viewModelScope.launch {
            try {
                firebaseService.saveAdminSettings(settings)
                _adminSettings.value = settings
                // Also sync firebaseService timer config
                firebaseService.setTimerVerificationEnabled(settings.isTimerEnabled)
                _isTimerEnabled.value = settings.isTimerEnabled
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating admin settings", e)
            }
        }
    }

    // USER DELETION & SUSPENSION
    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                firebaseService.deleteUserProfile(uid)
                fetchAllUsers()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error deleting user", e)
            }
        }
    }

    fun suspendUser(uid: String) {
        updateVerificationStatus(uid, "Suspended", "Account has been suspended by the administrator.")
    }

    fun reactivateUser(uid: String) {
        updateVerificationStatus(uid, "Approved", "Account has been reactivated by the administrator.")
    }

    // REFERRAL MANAGEMENT HELPER METHODS
    fun generateUniqueReferralCode(displayName: String): String {
        val cleanName = displayName.filter { it.isLetterOrDigit() }.take(4).uppercase()
        val randomPart = (1000..9999).random()
        return "${cleanName}${randomPart}"
    }

    fun loadUserReferrals(uid: String) {
        viewModelScope.launch {
            try {
                val list = firebaseService.fetchReferralsForReferrer(uid)
                _userReferrals.value = list
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user referrals: ${e.message}")
            }
        }
    }

    fun loadAllReferralRecords() {
        viewModelScope.launch {
            try {
                val list = firebaseService.fetchAllReferralRecords()
                _allReferrals.value = list
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading all referrals: ${e.message}")
            }
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val referrals = firebaseService.fetchAllReferralRecords()
                val users = firebaseService.fetchAllUserProfiles()
                val referrerGroups = referrals.filter { it.status == "Active" }.groupBy { it.referrerUid }
                
                val entries = referrerGroups.map { (referrerUid, userRefs) ->
                    val user = users.firstOrNull { it.uid == referrerUid }
                    val name = user?.displayName ?: "Anonymous Member"
                    val totalRewardEarned = userRefs.sumOf { it.rewardAmount }
                    LeaderboardEntry(
                        displayName = name,
                        referralsCount = userRefs.size,
                        totalRewards = totalRewardEarned
                    )
                }.sortedByDescending { it.referralsCount }
                
                val rankedEntries = entries.mapIndexed { index, entry ->
                    entry.copy(rank = index + 1)
                }
                
                _leaderboard.value = rankedEntries
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading leaderboard: ${e.message}")
            }
        }
    }

    fun incrementReferralClicks(code: String) {
        viewModelScope.launch {
            try {
                firebaseService.incrementReferralLinkClicks(code)
                _currentUser.value?.let { user ->
                    val updatedUser = firebaseService.fetchUserProfile(user.uid)
                    if (updatedUser != null) {
                        _currentUser.value = updatedUser
                        _uiState.value = AuthUiState.Authenticated(updatedUser)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error incrementing clicks: ${e.message}")
            }
        }
    }

    fun refreshUserProfile(uid: String, onComplete: (UserProfile) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val updatedUser = firebaseService.fetchUserProfile(uid)
                if (updatedUser != null) {
                    _currentUser.value = updatedUser
                    _uiState.value = AuthUiState.Authenticated(updatedUser)
                    onComplete(updatedUser)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error refreshing user profile: ${e.message}")
            }
        }
    }

    fun grantReferralRewardIfApplicable(referredUid: String, currentStatus: String) {
        viewModelScope.launch {
            try {
                val referralRecord = firebaseService.fetchReferralRecordByReferredUid(referredUid) ?: return@launch
                if (referralRecord.isRewardGranted) return@launch

                val settings = firebaseService.fetchAdminSettings()
                if (!settings.isReferralProgramEnabled) return@launch

                val triggerStatus = settings.grantRewardOnStatus // "Approved", "Pending", "Registration"
                val shouldGrant = when (triggerStatus) {
                    "Registration" -> true
                    "Pending" -> (currentStatus == "Pending Verification" || currentStatus == "Approved")
                    "Approved" -> (currentStatus == "Approved")
                    else -> false
                }

                if (shouldGrant) {
                    val referrerUid = referralRecord.referrerUid
                    val referrerProfile = firebaseService.fetchUserProfile(referrerUid)
                    if (referrerProfile != null && referrerProfile.accountStatus == "Approved") {
                        val rewardAmount = settings.referralRewardAmount
                        val updatedRecord = referralRecord.copy(
                            status = if (currentStatus == "Approved") "Active" else referralRecord.status,
                            rewardAmount = rewardAmount,
                            isRewardGranted = true,
                            rewardGrantedTime = System.currentTimeMillis()
                        )
                        firebaseService.saveReferralRecord(updatedRecord)

                        val updatedReferrer = referrerProfile.copy(
                            currentSavedBalance = referrerProfile.currentSavedBalance + rewardAmount,
                            referralRewardsEarned = referrerProfile.referralRewardsEarned + rewardAmount,
                            referredUsersCount = referrerProfile.referredUsersCount + 1
                        )
                        firebaseService.saveUserProfile(updatedReferrer)

                        sendNotification(
                            userId = referrerUid,
                            title = "Referral Reward Earned! 💰",
                            message = "You earned ₦${rewardAmount.toInt()} because ${referralRecord.referredDisplayName} registered and progressed. Keep sharing!",
                            type = "info"
                        )
                        addRecentActivity(
                            userId = referrerUid,
                            title = "Referral Reward",
                            description = "Earned ₦${rewardAmount.toInt()} from referring ${referralRecord.referredDisplayName}."
                        )
                    }
                } else {
                    val updatedRecord = referralRecord.copy(
                        status = if (currentStatus == "Approved") "Active" else if (currentStatus == "Rejected" || currentStatus == "Expired") "Rejected" else "Pending"
                    )
                    firebaseService.saveReferralRecord(updatedRecord)
                }

                loadAllReferralRecords()
                loadLeaderboard()
                _currentUser.value?.let { loadUserReferrals(it.uid) }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error processing referral reward: ${e.message}")
            }
        }
    }

    // SUPPORT TICKETS
    fun loadUserSupportTickets(userId: String) {
        viewModelScope.launch {
            try {
                val list = firebaseService.fetchTicketsForUser(userId)
                _userTickets.value = list
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user support tickets", e)
            }
        }
    }

    fun loadAllSupportTickets() {
        viewModelScope.launch {
            try {
                val list = firebaseService.fetchAllSupportTickets()
                _allTickets.value = list
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading all support tickets", e)
            }
        }
    }

    fun createSupportTicket(subject: String, description: String, user: UserProfile) {
        viewModelScope.launch {
            try {
                val ticketId = "tkt_${System.currentTimeMillis()}"
                val ticket = com.example.data.model.SupportTicket(
                    id = ticketId,
                    userId = user.uid,
                    userEmail = user.email,
                    userName = user.displayName,
                    subject = subject,
                    description = description,
                    status = "Open",
                    dateCreated = System.currentTimeMillis(),
                    lastUpdated = System.currentTimeMillis(),
                    messages = emptyList()
                )
                firebaseService.saveSupportTicket(ticket)
                
                addRecentActivity(
                    userId = user.uid,
                    title = "Support Ticket Created",
                    description = "Logged ticket: '$subject'."
                )
                sendNotification(
                    userId = user.uid,
                    title = "Ticket Submitted Successfully 💬",
                    message = "We have received your ticket regarding '$subject'. An advisor will reply within 24 hours.",
                    type = "info"
                )
                
                loadUserSupportTickets(user.uid)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error creating support ticket", e)
            }
        }
    }

    fun replyToSupportTicket(ticketId: String, senderUid: String, senderName: String, messageText: String) {
        viewModelScope.launch {
            try {
                val allT = firebaseService.fetchAllSupportTickets()
                val targetTicket = allT.firstOrNull { it.id == ticketId }
                if (targetTicket != null) {
                    val messageId = "msg_${System.currentTimeMillis()}"
                    val newMsg = com.example.data.model.TicketMessage(
                        id = messageId,
                        senderUid = senderUid,
                        senderName = senderName,
                        message = messageText,
                        timestamp = System.currentTimeMillis()
                    )
                    val updatedMessages = targetTicket.messages + newMsg
                    val isSenderAdmin = targetTicket.userId != senderUid
                    val updatedStatus = if (isSenderAdmin) "Replied" else "Open"
                    
                    val updatedTicket = targetTicket.copy(
                        messages = updatedMessages,
                        status = updatedStatus,
                        lastUpdated = System.currentTimeMillis()
                    )
                    firebaseService.saveSupportTicket(updatedTicket)
                    
                    if (isSenderAdmin) {
                        sendNotification(
                            userId = targetTicket.userId,
                            title = "New Reply on Support Ticket 🔔",
                            message = "An advisor replied to your ticket regarding '${targetTicket.subject}': \"$messageText\"",
                            type = "info"
                        )
                    } else {
                        addRecentActivity(
                            userId = targetTicket.userId,
                            title = "Reply added to ticket",
                            description = "User replied: \"$messageText\""
                        )
                    }
                    
                    loadUserSupportTickets(targetTicket.userId)
                    loadAllSupportTickets()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error replying to support ticket", e)
            }
        }
    }

    fun updateSupportTicketStatus(ticketId: String, status: String, notifyUser: Boolean = false) {
        viewModelScope.launch {
            try {
                val allT = firebaseService.fetchAllSupportTickets()
                val targetTicket = allT.firstOrNull { it.id == ticketId }
                if (targetTicket != null) {
                    val updatedTicket = targetTicket.copy(
                        status = status,
                        lastUpdated = System.currentTimeMillis()
                    )
                    firebaseService.saveSupportTicket(updatedTicket)
                    
                    if (notifyUser) {
                        sendNotification(
                            userId = targetTicket.userId,
                            title = "Support Ticket Status Updated",
                            message = "Your support ticket regarding '${targetTicket.subject}' is now marked as $status.",
                            type = "info"
                        )
                    }
                    
                    loadUserSupportTickets(targetTicket.userId)
                    loadAllSupportTickets()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating support ticket status", e)
            }
        }
    }

    fun reloadProfile() {
        viewModelScope.launch {
            try {
                _currentUser.value?.let { user ->
                    val updatedUser = firebaseService.fetchUserProfile(user.uid)
                    if (updatedUser != null) {
                        _currentUser.value = updatedUser
                        _uiState.value = AuthUiState.Authenticated(updatedUser)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error reloading profile: ${e.message}")
            }
        }
    }
}

sealed interface AuthUiState {
    object Initial : AuthUiState
    object Loading : AuthUiState
    object Unauthenticated : AuthUiState
    data class Authenticated(val user: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
