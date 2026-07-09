package com.example.data

import android.content.Context
import android.util.Log
import android.net.Uri
import com.example.data.model.UserProfile
import com.example.data.model.Article
import com.example.data.model.EarningCategory
import com.example.data.model.ReferralRecord
import com.example.data.model.AdminSettings
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseService(private val context: Context) {
    private val TAG = "FirebaseService"

    // Authentication, Firestore, and Storage instances (safely checked)
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var firebaseStorage: FirebaseStorage? = null
    
    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable

    // Simulated local states as robust fallbacks
    private val _simulatedUser = MutableStateFlow<UserProfile?>(null)
    val simulatedUser: StateFlow<UserProfile?> = _simulatedUser

    private val prefs = context.getSharedPreferences("wealth_builder_sim_v2", Context.MODE_PRIVATE)

    fun isAdminEmail(email: String): Boolean {
        val clean = email.lowercase().trim()
        return clean == "wealthbuilder@gmail.com" || 
               clean == "wealthbuildars@gmail.com" || 
               clean == "chizaramamajorchizaram@gmail.com"
    }

    init {
        try {
            // Safe initialization of FirebaseApp
            if (FirebaseApp.getApps(context).isEmpty()) {
                val apiKey = BuildConfig.FIREBASE_API_KEY
                val appId = BuildConfig.FIREBASE_APP_ID
                val dbUrl = BuildConfig.FIREBASE_DATABASE_URL
                val projectId = BuildConfig.FIREBASE_PROJECT_ID
                val storageBucket = BuildConfig.FIREBASE_STORAGE_BUCKET
                val gcmSenderId = BuildConfig.FIREBASE_MESSAGING_SENDER_ID

                if (apiKey.isNotEmpty() && !apiKey.startsWith("MY_") && appId.isNotEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setDatabaseUrl(dbUrl.ifEmpty { null })
                        .setProjectId(projectId.ifEmpty { null })
                        .setStorageBucket(storageBucket.ifEmpty { null })
                        .setGcmSenderId(gcmSenderId.ifEmpty { null })
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.d(TAG, "Firebase successfully initialized programmatically.")
                }
            }

            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                firebaseStorage = FirebaseStorage.getInstance()
                _isFirebaseAvailable.value = true
                Log.d(TAG, "Firebase successfully initialized.")
            } else {
                Log.w(TAG, "FirebaseApp is not initialized. Using premium simulation engine.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization failed: ${e.message}. Falling back to simulation mode.")
            firebaseAuth = null
            firestore = null
            firebaseStorage = null
            _isFirebaseAvailable.value = false
        }
        // Seed default marketplace categories, brands, partners, and products
        initializeDefaultMarketplaceData()
    }

    // PERSISTENCE FOR SIMULATION MODE
    fun saveSimulatedUserLocally(profile: UserProfile) {
        val editor = prefs.edit()
        val uid = profile.uid
        editor.putString("user_${uid}_uid", profile.uid)
        editor.putString("user_${uid}_email", profile.email)
        editor.putString("user_${uid}_displayName", profile.displayName)
        editor.putFloat("user_${uid}_monthlyGoal", profile.monthlyGoal.toFloat())
        editor.putFloat("user_${uid}_currentSavedBalance", profile.currentSavedBalance.toFloat())
        editor.putLong("user_${uid}_dateCreated", profile.dateCreated)
        editor.putString("user_${uid}_badges", profile.badges.joinToString(","))
        editor.putString("user_${uid}_selectedPath", profile.selectedPath)
        editor.putString("user_${uid}_accountStatus", profile.accountStatus)
        editor.putLong("user_${uid}_paymentSubmittedTime", profile.paymentSubmittedTime)
        editor.putString("user_${uid}_paymentProofUrl", profile.paymentProofUrl)
        editor.putString("user_${uid}_paymentProofName", profile.paymentProofName)
        editor.putString("user_${uid}_rejectionReason", profile.rejectionReason)
        editor.putBoolean("user_${uid}_isAdmin", profile.isAdmin)
        editor.putString("user_${uid}_referralCode", profile.referralCode)
        editor.putString("user_${uid}_referredByCode", profile.referredByCode)
        editor.putFloat("user_${uid}_referralBalance", profile.referralBalance.toFloat())

        val uids = (prefs.getStringSet("simulated_user_uids", emptySet()) ?: emptySet()).toMutableSet()
        uids.add(uid)
        editor.putStringSet("simulated_user_uids", uids)
        editor.apply()
    }

    fun getSimulatedUserLocally(uid: String): UserProfile? {
        if (!prefs.contains("user_${uid}_uid")) return null
        val badgesString = prefs.getString("user_${uid}_badges", "") ?: ""
        val badges = if (badgesString.isEmpty()) listOf("Pioneer") else badgesString.split(",")
        val emailVal = prefs.getString("user_${uid}_email", "") ?: ""
        val cleanEmail = emailVal.lowercase().trim()
        val isAdm = isAdminEmail(cleanEmail)
        val referralCode = prefs.getString("user_${uid}_referralCode", "") ?: ""
        val finalReferralCode = if (isAdm && !referralCode.startsWith("ADMN")) "ADMN${(1000..9999).random()}" else referralCode

        return UserProfile(
            uid = prefs.getString("user_${uid}_uid", "") ?: "",
            email = emailVal,
            displayName = if (isAdm) "Administrator" else (prefs.getString("user_${uid}_displayName", "") ?: ""),
            monthlyGoal = prefs.getFloat("user_${uid}_monthlyGoal", 5000f).toDouble(),
            currentSavedBalance = prefs.getFloat("user_${uid}_currentSavedBalance", 0f).toDouble(),
            dateCreated = prefs.getLong("user_${uid}_dateCreated", System.currentTimeMillis()),
            badges = badges,
            selectedPath = prefs.getString("user_${uid}_selectedPath", "All") ?: "All",
            accountStatus = if (isAdm) "Approved" else (prefs.getString("user_${uid}_accountStatus", "Unverified") ?: "Unverified"),
            paymentSubmittedTime = prefs.getLong("user_${uid}_paymentSubmittedTime", 0L),
            paymentProofUrl = prefs.getString("user_${uid}_paymentProofUrl", "") ?: "",
            paymentProofName = prefs.getString("user_${uid}_paymentProofName", "") ?: "",
            rejectionReason = prefs.getString("user_${uid}_rejectionReason", "") ?: "",
            isAdmin = if (isAdm) true else prefs.getBoolean("user_${uid}_isAdmin", false),
            referralCode = finalReferralCode,
            referredByCode = prefs.getString("user_${uid}_referredByCode", "") ?: "",
            referralBalance = prefs.getFloat("user_${uid}_referralBalance", 0f).toDouble()
        )
    }

    fun getAllSimulatedUsersLocally(): List<UserProfile> {
        val uids = prefs.getStringSet("simulated_user_uids", emptySet()) ?: emptySet()
        return uids.mapNotNull { getSimulatedUserLocally(it) }
    }

    // 24-HOUR TIMER SETTINGS
    fun setTimerVerificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("timer_verification_enabled", enabled).apply()
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("configs").document("timer_settings")
                    .set(mapOf("enabled" to enabled))
            } catch (e: Exception) {
                Log.e(TAG, "Error writing config to Firestore", e)
            }
        }
    }

    fun isTimerVerificationEnabled(): Boolean {
        return prefs.getBoolean("timer_verification_enabled", true)
    }

    // AUTHENTICATION APIs
    
    fun getCurrentUser(): UserProfile? {
        if (_isFirebaseAvailable.value) {
            try {
                val fbUser = firebaseAuth?.currentUser
                if (fbUser != null) {
                    val fbEmail = (fbUser.email ?: "").lowercase().trim()
                    val isAdm = isAdminEmail(fbEmail)
                    val rawProfile = getSimulatedUserLocally(fbUser.uid)
                    val profile = if (rawProfile != null) {
                        if (isAdm && (!rawProfile.isAdmin || rawProfile.accountStatus != "Approved")) {
                            val updated = rawProfile.copy(
                                isAdmin = true,
                                accountStatus = "Approved",
                                displayName = "Administrator",
                                email = fbEmail
                            )
                            saveSimulatedUserLocally(updated)
                            updated
                        } else {
                            rawProfile
                        }
                    } else {
                        val newProfile = UserProfile(
                            uid = fbUser.uid,
                            email = fbEmail,
                            displayName = if (isAdm) "Administrator" else (fbUser.displayName ?: "Wealth Pioneer"),
                            isAdmin = isAdm,
                            accountStatus = if (isAdm) "Approved" else "Unverified",
                            referralCode = if (isAdm) "ADMN${(1000..9999).random()}" else "USER${(1000..9999).random()}"
                        )
                        saveSimulatedUserLocally(newProfile)
                        newProfile
                    }
                    return profile
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current firebase user", e)
            }
        }
        val simUser = _simulatedUser.value
        if (simUser != null) {
            val simEmail = simUser.email.lowercase().trim()
            val isAdm = isAdminEmail(simEmail)
            if (isAdm && (!simUser.isAdmin || simUser.accountStatus != "Approved")) {
                val updated = simUser.copy(isAdmin = true, accountStatus = "Approved", displayName = "Administrator")
                _simulatedUser.value = updated
                saveSimulatedUserLocally(updated)
                return updated
            }
        }
        return simUser
    }

    suspend fun signUp(email: String, password: String, displayName: String, referredByCode: String = ""): Result<UserProfile> {
        val cleanEmail = email.lowercase().trim()
        val isAdm = isAdminEmail(cleanEmail)
        val initialStatus = if (isAdm) "Approved" else "Unverified"

        val cleanName = displayName.filter { it.isLetterOrDigit() }.take(4).uppercase()
        val randomPart = (1000..9999).random()
        val generatedCode = if (isAdm) "ADMN${randomPart}" else if (cleanName.isNotBlank()) "${cleanName}${randomPart}" else "USER${randomPart}"

        if (_isFirebaseAvailable.value) {
            return try {
                val authResult = firebaseAuth!!.createUserWithEmailAndPassword(cleanEmail, password).await()
                val fbUser = authResult.user
                if (fbUser != null) {
                    val profile = UserProfile(
                        uid = fbUser.uid,
                        email = cleanEmail,
                        displayName = if (isAdm) "Administrator" else displayName,
                        isAdmin = isAdm,
                        accountStatus = initialStatus,
                        referralCode = generatedCode,
                        referredByCode = referredByCode
                    )
                    // Save to Firestore & SharedPref cache
                    saveUserProfile(profile)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Failed to create user profile"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sign up failed", e)
                Result.failure(e)
            }
        } else {
            // Simulated Success
            val profile = UserProfile(
                uid = "simulated_user_${System.currentTimeMillis()}",
                email = cleanEmail,
                displayName = if (isAdm) "Administrator" else displayName,
                isAdmin = isAdm,
                accountStatus = initialStatus,
                referralCode = generatedCode,
                referredByCode = referredByCode
            )
            _simulatedUser.value = profile
            saveSimulatedUserLocally(profile)
            prefs.edit().putString("user_pwd_$cleanEmail", password).apply()
            return Result.success(profile)
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.lowercase().trim()
        val isAdm = isAdminEmail(cleanEmail)

        if (_isFirebaseAvailable.value) {
            return try {
                val authResult = firebaseAuth!!.signInWithEmailAndPassword(cleanEmail, password).await()
                val fbUser = authResult.user
                if (fbUser != null) {
                    var profile = fetchUserProfile(fbUser.uid)
                    if (profile == null) {
                        val cleanName = if (isAdm) "Administrator" else (fbUser.displayName ?: "Wealth Pioneer")
                        val randomPart = (1000..9999).random()
                        val generatedCode = if (isAdm) "ADMN${randomPart}" else "${cleanName.filter { it.isLetterOrDigit() }.take(4).uppercase()}${randomPart}"
                        profile = UserProfile(
                            uid = fbUser.uid,
                            email = cleanEmail,
                            displayName = cleanName,
                            isAdmin = isAdm,
                            accountStatus = if (isAdm) "Approved" else "Unverified",
                            referralCode = generatedCode
                        )
                        saveUserProfile(profile)
                    } else if (profile.isAdmin != isAdm) {
                        profile = profile.copy(isAdmin = isAdm)
                        saveUserProfile(profile)
                    }
                    // Save local cached version
                    saveSimulatedUserLocally(profile)
                    _simulatedUser.value = profile
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Failed to sign in"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sign in failed", e)
                // Auto-register/provision admin if it does not exist yet in Firebase Auth!
                if (isAdm && password == "Chi19062006$@") {
                    Log.d(TAG, "Predefined admin not found or credentials mismatch in Firebase. Auto-creating admin account...")
                    try {
                        val createResult = firebaseAuth!!.createUserWithEmailAndPassword(cleanEmail, password).await()
                        val fbUser = createResult.user
                        if (fbUser != null) {
                            val cleanName = "Administrator"
                            val randomPart = (1000..9999).random()
                            val generatedCode = "ADMN${randomPart}"
                            val profile = UserProfile(
                                uid = fbUser.uid,
                                email = cleanEmail,
                                displayName = cleanName,
                                isAdmin = true,
                                accountStatus = "Approved",
                                referralCode = generatedCode
                            )
                            saveUserProfile(profile)
                            _simulatedUser.value = profile
                            return Result.success(profile)
                        }
                    } catch (signUpEx: Exception) {
                        Log.e(TAG, "Failed to auto-create admin account", signUpEx)
                        return Result.failure(Exception("Admin login failed: ${e.localizedMessage}. Please verify your credentials or check Firebase Console."))
                    }
                }
                Result.failure(e)
            }
        } else {
            // Admin simulation check:
            if (isAdm && password == "Chi19062006$@") {
                val admin = UserProfile(
                    uid = "admin_uid_simulated",
                    email = cleanEmail,
                    displayName = "Administrator",
                    accountStatus = "Approved",
                    isAdmin = true
                )
                _simulatedUser.value = admin
                saveSimulatedUserLocally(admin)
                return Result.success(admin)
            } else if (isAdm) {
                return Result.failure(Exception("Incorrect password for Administrator account."))
            }

            // Simulated Success with local persistence
            val storedUser = getAllSimulatedUsersLocally().firstOrNull { it.email.lowercase().trim() == cleanEmail }
            if (storedUser != null) {
                val savedPassword = prefs.getString("user_pwd_$cleanEmail", null)
                if (savedPassword != null && savedPassword != password) {
                    return Result.failure(Exception("Incorrect password for user '$cleanEmail'."))
                }
                _simulatedUser.value = storedUser
                return Result.success(storedUser)
            } else if (email.contains("@")) {
                val profile = UserProfile(
                    uid = "simulated_user_${System.currentTimeMillis()}",
                    email = cleanEmail,
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isAdmin = isAdm,
                    accountStatus = if (isAdm) "Approved" else "Unverified"
                )
                _simulatedUser.value = profile
                saveSimulatedUserLocally(profile)
                prefs.edit().putString("user_pwd_$cleanEmail", password).apply()
                return Result.success(profile)
            }
            return Result.failure(Exception("Invalid email format or password."))
        }
    }

    fun signOut() {
        if (_isFirebaseAvailable.value) {
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sign out error", e)
            }
        }
        _simulatedUser.value = null
    }

    // FIRESTORE APIs - User Profile Management

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        saveSimulatedUserLocally(profile)
        _simulatedUser.value = profile

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("users")
                    .document(profile.uid)
                    .set(profile)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user profile to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchUserProfile(uid: String): UserProfile? {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("users")
                    .document(uid)
                    .get()
                    .await()
                var profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    val cleanEmail = profile.email.lowercase().trim()
                    val isAdm = isAdminEmail(cleanEmail)
                    if (isAdm && (!profile.isAdmin || profile.accountStatus != "Approved")) {
                        profile = profile.copy(isAdmin = true, accountStatus = "Approved", displayName = "Administrator")
                        saveUserProfile(profile)
                    }
                    saveSimulatedUserLocally(profile)
                    _simulatedUser.value = profile
                    return profile
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile from Firestore", e)
            }
        }
        return getSimulatedUserLocally(uid) ?: _simulatedUser.value
    }

    // ADMINISTRATIVE DATABASE FETCH
    suspend fun fetchAllUserProfiles(): List<UserProfile> {
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("users").get().await()
                val profiles = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                profiles.forEach { saveSimulatedUserLocally(it) } // sync to SharedPreferences cache
                profiles
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all user profiles", e)
                getAllSimulatedUsersLocally()
            }
        }
        return getAllSimulatedUsersLocally()
    }

    // UPLOAD PROOF OF PAYMENT
    suspend fun uploadPaymentProof(uid: String, uri: Uri, fileName: String): String {
        if (_isFirebaseAvailable.value && firebaseStorage != null) {
            return try {
                val storageRef = firebaseStorage!!.reference
                    .child("payment_proofs")
                    .child(uid)
                    .child(fileName)
                
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Log.d(TAG, "Uploaded payment proof. URL: $downloadUrl")
                downloadUrl
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Storage upload failed, using local URI representation", e)
                uri.toString()
            }
        }
        // Simulated mode return
        return uri.toString()
    }

    // ARTICLES & SAVED ITEMS
    suspend fun publishArticle(article: Article): Boolean {
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("articles")
                    .document(article.id)
                    .set(article)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error publishing article to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchFirestoreArticles(): List<Article> {
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("articles").get().await()
                snapshot.documents.mapNotNull { it.toObject(Article::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching articles from Firestore", e)
                emptyList()
            }
        }
        return emptyList()
    }

    // ANNOUNCEMENTS PERSISTENCE
    fun saveAnnouncementLocally(announcement: com.example.data.model.Announcement) {
        val editor = prefs.edit()
        val id = announcement.id
        editor.putString("ann_${id}_id", announcement.id)
        editor.putString("ann_${id}_title", announcement.title)
        editor.putString("ann_${id}_content", announcement.content)
        editor.putLong("ann_${id}_timestamp", announcement.timestamp)
        editor.putBoolean("ann_${id}_isPinned", announcement.isPinned)
        editor.putString("ann_${id}_author", announcement.author)

        val ids = (prefs.getStringSet("simulated_ann_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_ann_ids", ids)
        editor.apply()
    }

    fun getAnnouncementLocally(id: String): com.example.data.model.Announcement? {
        if (!prefs.contains("ann_${id}_id")) return null
        return com.example.data.model.Announcement(
            id = prefs.getString("ann_${id}_id", "") ?: "",
            title = prefs.getString("ann_${id}_title", "") ?: "",
            content = prefs.getString("ann_${id}_content", "") ?: "",
            timestamp = prefs.getLong("ann_${id}_timestamp", System.currentTimeMillis()),
            isPinned = prefs.getBoolean("ann_${id}_isPinned", false),
            author = prefs.getString("ann_${id}_author", "Admin") ?: "Admin"
        )
    }

    fun getAllAnnouncementsLocally(): List<com.example.data.model.Announcement> {
        val ids = prefs.getStringSet("simulated_ann_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getAnnouncementLocally(it) }.sortedByDescending { it.timestamp }
    }

    fun deleteAnnouncementLocally(id: String) {
        val editor = prefs.edit()
        editor.remove("ann_${id}_id")
        editor.remove("ann_${id}_title")
        editor.remove("ann_${id}_content")
        editor.remove("ann_${id}_timestamp")
        editor.remove("ann_${id}_isPinned")
        editor.remove("ann_${id}_author")
        
        val ids = (prefs.getStringSet("simulated_ann_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.remove(id)
        editor.putStringSet("simulated_ann_ids", ids)
        editor.apply()
    }

    suspend fun saveAnnouncement(announcement: com.example.data.model.Announcement): Boolean {
        saveAnnouncementLocally(announcement)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("announcements")
                    .document(announcement.id)
                    .set(announcement)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving announcement to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun deleteAnnouncement(id: String): Boolean {
        deleteAnnouncementLocally(id)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("announcements")
                    .document(id)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting announcement from Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchAnnouncements(): List<com.example.data.model.Announcement> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("announcements").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.Announcement::class.java) }
                list.forEach { saveAnnouncementLocally(it) }
                return list.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching announcements from Firestore", e)
            }
        }
        return getAllAnnouncementsLocally()
    }

    // NOTIFICATIONS PERSISTENCE
    fun saveNotificationLocally(notification: com.example.data.model.NotificationItem) {
        val editor = prefs.edit()
        val id = notification.id
        editor.putString("not_${id}_id", notification.id)
        editor.putString("not_${id}_userId", notification.userId)
        editor.putString("not_${id}_title", notification.title)
        editor.putString("not_${id}_message", notification.message)
        editor.putLong("not_${id}_timestamp", notification.timestamp)
        editor.putBoolean("not_${id}_isRead", notification.isRead)
        editor.putString("not_${id}_type", notification.type)

        val ids = (prefs.getStringSet("simulated_not_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_not_ids", ids)
        editor.apply()
    }

    fun getNotificationLocally(id: String): com.example.data.model.NotificationItem? {
        if (!prefs.contains("not_${id}_id")) return null
        return com.example.data.model.NotificationItem(
            id = prefs.getString("not_${id}_id", "") ?: "",
            userId = prefs.getString("not_${id}_userId", "") ?: "",
            title = prefs.getString("not_${id}_title", "") ?: "",
            message = prefs.getString("not_${id}_message", "") ?: "",
            timestamp = prefs.getLong("not_${id}_timestamp", System.currentTimeMillis()),
            isRead = prefs.getBoolean("not_${id}_isRead", false),
            type = prefs.getString("not_${id}_type", "info") ?: "info"
        )
    }

    fun getAllNotificationsLocally(userId: String): List<com.example.data.model.NotificationItem> {
        val ids = prefs.getStringSet("simulated_not_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getNotificationLocally(it) }
            .filter { it.userId == userId || it.userId.isEmpty() }
            .sortedByDescending { it.timestamp }
    }

    suspend fun saveNotification(notification: com.example.data.model.NotificationItem): Boolean {
        saveNotificationLocally(notification)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("notifications")
                    .document(notification.id)
                    .set(notification)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchNotifications(userId: String): List<com.example.data.model.NotificationItem> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("notifications").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.NotificationItem::class.java) }
                list.forEach { saveNotificationLocally(it) }
                return list.filter { it.userId == userId || it.userId.isEmpty() }.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching notifications from Firestore", e)
            }
        }
        return getAllNotificationsLocally(userId)
    }

    fun deleteNotificationLocally(id: String) {
        val editor = prefs.edit()
        editor.remove("not_${id}_id")
        editor.remove("not_${id}_userId")
        editor.remove("not_${id}_title")
        editor.remove("not_${id}_message")
        editor.remove("not_${id}_timestamp")
        editor.remove("not_${id}_isRead")
        editor.remove("not_${id}_type")

        val ids = (prefs.getStringSet("simulated_not_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.remove(id)
        editor.putStringSet("simulated_not_ids", ids)
        editor.apply()
    }

    suspend fun deleteNotification(id: String): Boolean {
        deleteNotificationLocally(id)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("notifications")
                    .document(id)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification from Firestore", e)
                false
            }
        }
        return true
    }

    // RECENT ACTIVITY PERSISTENCE
    fun saveRecentActivityLocally(activity: com.example.data.model.RecentActivity) {
        val editor = prefs.edit()
        val id = activity.id
        editor.putString("act_${id}_id", activity.id)
        editor.putString("act_${id}_userId", activity.userId)
        editor.putString("act_${id}_title", activity.title)
        editor.putString("act_${id}_description", activity.description)
        editor.putLong("act_${id}_timestamp", activity.timestamp)

        val ids = (prefs.getStringSet("simulated_act_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_act_ids", ids)
        editor.apply()
    }

    fun getRecentActivityLocally(id: String): com.example.data.model.RecentActivity? {
        if (!prefs.contains("act_${id}_id")) return null
        return com.example.data.model.RecentActivity(
            id = prefs.getString("act_${id}_id", "") ?: "",
            userId = prefs.getString("act_${id}_userId", "") ?: "",
            title = prefs.getString("act_${id}_title", "") ?: "",
            description = prefs.getString("act_${id}_description", "") ?: "",
            timestamp = prefs.getLong("act_${id}_timestamp", System.currentTimeMillis())
        )
    }

    fun getAllRecentActivitiesLocally(userId: String): List<com.example.data.model.RecentActivity> {
        val ids = prefs.getStringSet("simulated_act_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getRecentActivityLocally(it) }
            .filter { it.userId == userId }
            .sortedByDescending { it.timestamp }
    }

    suspend fun saveRecentActivity(activity: com.example.data.model.RecentActivity): Boolean {
        saveRecentActivityLocally(activity)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("activities")
                    .document(activity.id)
                    .set(activity)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving activity to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchRecentActivities(userId: String): List<com.example.data.model.RecentActivity> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("activities").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.RecentActivity::class.java) }
                list.forEach { saveRecentActivityLocally(it) }
                return list.filter { it.userId == userId }.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching activities from Firestore", e)
            }
        }
        return getAllRecentActivitiesLocally(userId)
    }

    suspend fun saveActivityLog(log: com.example.data.model.ActivityLog): Boolean {
        val logsList = fetchActivityLogs().toMutableList()
        logsList.add(log)
        val ja = org.json.JSONArray()
        logsList.forEach { l ->
            val jo = org.json.JSONObject()
            jo.put("id", l.id)
            jo.put("userId", l.userId)
            jo.put("userEmail", l.userEmail)
            jo.put("action", l.action)
            jo.put("details", l.details)
            jo.put("timestamp", l.timestamp)
            ja.put(jo)
        }
        prefs.edit().putString("activity_logs_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("activity_logs")
                    .document(log.id)
                    .set(log)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving activity log to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchActivityLogs(): List<com.example.data.model.ActivityLog> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("activity_logs").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.ActivityLog::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { l ->
                        val jo = org.json.JSONObject()
                        jo.put("id", l.id)
                        jo.put("userId", l.userId)
                        jo.put("userEmail", l.userEmail)
                        jo.put("action", l.action)
                        jo.put("details", l.details)
                        jo.put("timestamp", l.timestamp)
                        ja.put(jo)
                    }
                    prefs.edit().putString("activity_logs_json", ja.toString()).apply()
                    return list.sortedByDescending { it.timestamp }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching activity logs from Firestore", e)
            }
        }
        val logsStr = prefs.getString("activity_logs_json", "") ?: ""
        if (logsStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.ActivityLog>()
        try {
            val ja = org.json.JSONArray(logsStr)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.ActivityLog(
                        id = jo.optString("id", ""),
                        userId = jo.optString("userId", ""),
                        userEmail = jo.optString("userEmail", ""),
                        action = jo.optString("action", ""),
                        details = jo.optString("details", ""),
                        timestamp = jo.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing activity logs from SharedPreferences", e)
        }
        return list.sortedByDescending { it.timestamp }
    }

    // ADMIN SETTINGS PERSISTENCE
    fun saveAdminSettingsLocally(settings: com.example.data.model.AdminSettings) {
        val editor = prefs.edit()
        editor.putFloat("sett_registrationFee", settings.registrationFee.toFloat())
        editor.putBoolean("sett_isTimerEnabled", settings.isTimerEnabled)
        editor.putString("sett_bankName", settings.bankName)
        editor.putString("sett_accountNumber", settings.accountNumber)
        editor.putString("sett_accountName", settings.accountName)
        editor.putString("sett_siteName", settings.siteName)
        editor.putString("sett_siteDescription", settings.siteDescription)
        editor.putString("sett_websiteLogoUrl", settings.websiteLogoUrl)
        editor.putString("sett_websiteBannerUrl", settings.websiteBannerUrl)
        editor.putBoolean("sett_isReferralProgramEnabled", settings.isReferralProgramEnabled)
        editor.putFloat("sett_referralRewardAmount", settings.referralRewardAmount.toFloat())
        editor.putString("sett_grantRewardOnStatus", settings.grantRewardOnStatus)
        editor.putString("sett_contactEmail", settings.contactEmail)
        editor.putString("sett_contactPhone", settings.contactPhone)
        editor.apply()
    }

    fun getAdminSettingsLocally(): com.example.data.model.AdminSettings {
        return com.example.data.model.AdminSettings(
            registrationFee = prefs.getFloat("sett_registrationFee", 5000f).toDouble(),
            isTimerEnabled = prefs.getBoolean("sett_isTimerEnabled", true),
            bankName = prefs.getString("sett_bankName", "OPay") ?: "OPay",
            accountNumber = prefs.getString("sett_accountNumber", "9162072645") ?: "9162072645",
            accountName = prefs.getString("sett_accountName", "Chizaram W. Amajor") ?: "Chizaram W. Amajor",
            siteName = prefs.getString("sett_siteName", "Wealth Builder") ?: "Wealth Builder",
            siteDescription = prefs.getString("sett_siteDescription", "Build Legitimate Wealth. Learn scalable systems.") ?: "Build Legitimate Wealth. Learn scalable systems.",
            websiteLogoUrl = prefs.getString("sett_websiteLogoUrl", "") ?: "",
            websiteBannerUrl = prefs.getString("sett_websiteBannerUrl", "") ?: "",
            isReferralProgramEnabled = prefs.getBoolean("sett_isReferralProgramEnabled", true),
            referralRewardAmount = prefs.getFloat("sett_referralRewardAmount", 1500f).toDouble(),
            grantRewardOnStatus = prefs.getString("sett_grantRewardOnStatus", "Approved") ?: "Approved",
            contactEmail = prefs.getString("sett_contactEmail", "wealthbuildars@gmail.com") ?: "wealthbuildars@gmail.com",
            contactPhone = prefs.getString("sett_contactPhone", "+2349162072645") ?: "+2349162072645"
        )
    }

    suspend fun saveAdminSettings(settings: com.example.data.model.AdminSettings): Boolean {
        saveAdminSettingsLocally(settings)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("configs")
                    .document("admin_settings")
                    .set(settings)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving admin settings to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchAdminSettings(): com.example.data.model.AdminSettings {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("configs")
                    .document("admin_settings")
                    .get()
                    .await()
                val settings = snapshot.toObject(com.example.data.model.AdminSettings::class.java)
                if (settings != null) {
                    saveAdminSettingsLocally(settings)
                    return settings
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching admin settings from Firestore", e)
            }
        }
        return getAdminSettingsLocally()
    }

    // USER DELETION
    suspend fun deleteUserProfile(uid: String): Boolean {
        val editor = prefs.edit()
        editor.remove("user_${uid}_uid")
        editor.remove("user_${uid}_email")
        editor.remove("user_${uid}_displayName")
        editor.remove("user_${uid}_monthlyGoal")
        editor.remove("user_${uid}_currentSavedBalance")
        editor.remove("user_${uid}_dateCreated")
        editor.remove("user_${uid}_badges")
        editor.remove("user_${uid}_selectedPath")
        editor.remove("user_${uid}_accountStatus")
        editor.remove("user_${uid}_paymentSubmittedTime")
        editor.remove("user_${uid}_paymentProofUrl")
        editor.remove("user_${uid}_paymentProofName")
        editor.remove("user_${uid}_rejectionReason")
        editor.remove("user_${uid}_isAdmin")
        
        val uids = (prefs.getStringSet("simulated_user_uids", emptySet()) ?: emptySet()).toMutableSet()
        uids.remove(uid)
        editor.putStringSet("simulated_user_uids", uids)
        editor.apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("users")
                    .document(uid)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user from Firestore", e)
                false
            }
        }
        return true
    }

    // REFERRAL MANAGEMENT APIs
    fun saveReferralRecordLocally(record: ReferralRecord) {
        val editor = prefs.edit()
        val id = record.id
        editor.putString("ref_${id}_id", id)
        editor.putString("ref_${id}_referrerUid", record.referrerUid)
        editor.putString("ref_${id}_referredUid", record.referredUid)
        editor.putString("ref_${id}_referredEmail", record.referredEmail)
        editor.putString("ref_${id}_referredDisplayName", record.referredDisplayName)
        editor.putString("ref_${id}_referralCode", record.referralCode)
        editor.putString("ref_${id}_status", record.status)
        editor.putLong("ref_${id}_dateCreated", record.dateCreated)
        editor.putFloat("ref_${id}_rewardAmount", record.rewardAmount.toFloat())
        editor.putBoolean("ref_${id}_isRewardGranted", record.isRewardGranted)
        editor.putLong("ref_${id}_rewardGrantedTime", record.rewardGrantedTime)

        val ids = (prefs.getStringSet("simulated_ref_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_ref_ids", ids)
        editor.apply()
    }

    fun getReferralRecordLocally(id: String): ReferralRecord? {
        if (!prefs.contains("ref_${id}_id")) return null
        return ReferralRecord(
            id = prefs.getString("ref_${id}_id", "") ?: "",
            referrerUid = prefs.getString("ref_${id}_referrerUid", "") ?: "",
            referredUid = prefs.getString("ref_${id}_referredUid", "") ?: "",
            referredEmail = prefs.getString("ref_${id}_referredEmail", "") ?: "",
            referredDisplayName = prefs.getString("ref_${id}_referredDisplayName", "") ?: "",
            referralCode = prefs.getString("ref_${id}_referralCode", "") ?: "",
            status = prefs.getString("ref_${id}_status", "Pending") ?: "Pending",
            dateCreated = prefs.getLong("ref_${id}_dateCreated", System.currentTimeMillis()),
            rewardAmount = prefs.getFloat("ref_${id}_rewardAmount", 0f).toDouble(),
            isRewardGranted = prefs.getBoolean("ref_${id}_isRewardGranted", false),
            rewardGrantedTime = prefs.getLong("ref_${id}_rewardGrantedTime", 0L)
        )
    }

    fun getAllReferralRecordsLocally(): List<ReferralRecord> {
        val ids = prefs.getStringSet("simulated_ref_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getReferralRecordLocally(it) }
    }

    suspend fun saveReferralRecord(record: ReferralRecord): Boolean {
        saveReferralRecordLocally(record)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("referrals")
                    .document(record.id)
                    .set(record)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving referral record to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchReferralsForReferrer(referrerUid: String): List<ReferralRecord> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("referrals")
                    .whereEqualTo("referrerUid", referrerUid)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { it.toObject(ReferralRecord::class.java) }
                list.forEach { saveReferralRecordLocally(it) }
                return list.sortedByDescending { it.dateCreated }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching referrals from Firestore", e)
            }
        }
        return getAllReferralRecordsLocally().filter { it.referrerUid == referrerUid }.sortedByDescending { it.dateCreated }
    }

    suspend fun fetchAllReferralRecords(): List<ReferralRecord> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("referrals").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(ReferralRecord::class.java) }
                list.forEach { saveReferralRecordLocally(it) }
                return list.sortedByDescending { it.dateCreated }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all referrals from Firestore", e)
            }
        }
        return getAllReferralRecordsLocally().sortedByDescending { it.dateCreated }
    }

    suspend fun fetchReferralRecordByReferredUid(referredUid: String): ReferralRecord? {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("referrals")
                    .whereEqualTo("referredUid", referredUid)
                    .get()
                    .await()
                val record = snapshot.documents.firstOrNull()?.toObject(ReferralRecord::class.java)
                if (record != null) {
                    saveReferralRecordLocally(record)
                    return record
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching referral by referredUid", e)
            }
        }
        return getAllReferralRecordsLocally().firstOrNull { it.referredUid == referredUid }
    }

    suspend fun incrementReferralLinkClicks(referralCode: String): Boolean {
        val allUsers = fetchAllUserProfiles()
        val referrer = allUsers.firstOrNull { it.referralCode.equals(referralCode, ignoreCase = true) }
        if (referrer != null) {
            val updated = referrer.copy(referralLinkClicks = referrer.referralLinkClicks + 1)
            saveUserProfile(updated)
            return true
        }
        return false
    }

    // SUPPORT TICKET PERSISTENCE
    fun saveSupportTicketLocally(ticket: com.example.data.model.SupportTicket) {
        val editor = prefs.edit()
        val id = ticket.id
        editor.putString("tkt_${id}_id", id)
        editor.putString("tkt_${id}_userId", ticket.userId)
        editor.putString("tkt_${id}_userEmail", ticket.userEmail)
        editor.putString("tkt_${id}_userName", ticket.userName)
        editor.putString("tkt_${id}_subject", ticket.subject)
        editor.putString("tkt_${id}_description", ticket.description)
        editor.putString("tkt_${id}_status", ticket.status)
        editor.putLong("tkt_${id}_dateCreated", ticket.dateCreated)
        editor.putLong("tkt_${id}_lastUpdated", ticket.lastUpdated)
        
        val msgStr = ticket.messages.joinToString("||") { msg ->
            val encMsg = android.util.Base64.encodeToString(msg.message.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
            val encName = android.util.Base64.encodeToString(msg.senderName.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
            "${msg.id}|${msg.senderUid}|$encName|$encMsg|${msg.timestamp}"
        }
        editor.putString("tkt_${id}_messages", msgStr)

        val ids = (prefs.getStringSet("simulated_tkt_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_tkt_ids", ids)
        editor.apply()
    }

    fun getSupportTicketLocally(id: String): com.example.data.model.SupportTicket? {
        if (!prefs.contains("tkt_${id}_id")) return null
        val msgStr = prefs.getString("tkt_${id}_messages", "") ?: ""
        val messages = if (msgStr.isEmpty()) emptyList() else msgStr.split("||").mapNotNull {
            val parts = it.split("|")
            if (parts.size >= 5) {
                try {
                    val decName = String(android.util.Base64.decode(parts[2], android.util.Base64.DEFAULT), Charsets.UTF_8)
                    val decMsg = String(android.util.Base64.decode(parts[3], android.util.Base64.DEFAULT), Charsets.UTF_8)
                    com.example.data.model.TicketMessage(
                        id = parts[0],
                        senderUid = parts[1],
                        senderName = decName,
                        message = decMsg,
                        timestamp = parts[4].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            } else null
        }
        return com.example.data.model.SupportTicket(
            id = prefs.getString("tkt_${id}_id", "") ?: "",
            userId = prefs.getString("tkt_${id}_userId", "") ?: "",
            userEmail = prefs.getString("tkt_${id}_userEmail", "") ?: "",
            userName = prefs.getString("tkt_${id}_userName", "") ?: "",
            subject = prefs.getString("tkt_${id}_subject", "") ?: "",
            description = prefs.getString("tkt_${id}_description", "") ?: "",
            status = prefs.getString("tkt_${id}_status", "Open") ?: "Open",
            dateCreated = prefs.getLong("tkt_${id}_dateCreated", System.currentTimeMillis()),
            lastUpdated = prefs.getLong("tkt_${id}_lastUpdated", System.currentTimeMillis()),
            messages = messages
        )
    }

    fun getAllSupportTicketsLocally(): List<com.example.data.model.SupportTicket> {
        val ids = prefs.getStringSet("simulated_tkt_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getSupportTicketLocally(it) }.sortedByDescending { it.lastUpdated }
    }

    suspend fun saveSupportTicket(ticket: com.example.data.model.SupportTicket): Boolean {
        saveSupportTicketLocally(ticket)
        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("tickets")
                    .document(ticket.id)
                    .set(ticket)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving support ticket to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchTicketsForUser(userId: String): List<com.example.data.model.SupportTicket> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("tickets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.SupportTicket::class.java) }
                list.forEach { saveSupportTicketLocally(it) }
                return list.sortedByDescending { it.lastUpdated }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user support tickets from Firestore", e)
            }
        }
        return getAllSupportTicketsLocally().filter { it.userId == userId }.sortedByDescending { it.lastUpdated }
    }

    suspend fun fetchAllSupportTickets(): List<com.example.data.model.SupportTicket> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("tickets").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.SupportTicket::class.java) }
                list.forEach { saveSupportTicketLocally(it) }
                return list.sortedByDescending { it.lastUpdated }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all support tickets from Firestore", e)
            }
        }
        return getAllSupportTicketsLocally()
    }

    // --- PRODUCT REFERRAL SALES PERSISTENCE & APIs ---
    fun saveReferralSaleLocally(sale: com.example.data.model.ProductReferralSale) {
        val editor = prefs.edit()
        val id = sale.id
        editor.putString("sale_${id}_id", id)
        editor.putString("sale_${id}_referrerUid", sale.referrerUid)
        editor.putString("sale_${id}_referrerEmail", sale.referrerEmail)
        editor.putString("sale_${id}_productId", sale.productId)
        editor.putString("sale_${id}_productName", sale.productName)
        editor.putFloat("sale_${id}_productPrice", sale.productPrice.toFloat())
        editor.putString("sale_${id}_buyerName", sale.buyerName)
        editor.putString("sale_${id}_buyerEmail", sale.buyerEmail)
        editor.putFloat("sale_${id}_salePrice", sale.salePrice.toFloat())
        editor.putFloat("sale_${id}_commissionEarned", sale.commissionEarned.toFloat())
        editor.putLong("sale_${id}_dateSubmitted", sale.dateSubmitted)
        editor.putString("sale_${id}_status", sale.status)
        editor.putString("sale_${id}_paymentReference", sale.paymentReference)
        editor.putString("sale_${id}_rejectionReason", sale.rejectionReason)

        val ids = (prefs.getStringSet("simulated_sale_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_sale_ids", ids)
        editor.apply()
    }

    fun getReferralSaleLocally(id: String): com.example.data.model.ProductReferralSale? {
        if (!prefs.contains("sale_${id}_id")) return null
        return com.example.data.model.ProductReferralSale(
            id = prefs.getString("sale_${id}_id", "") ?: "",
            referrerUid = prefs.getString("sale_${id}_referrerUid", "") ?: "",
            referrerEmail = prefs.getString("sale_${id}_referrerEmail", "") ?: "",
            productId = prefs.getString("sale_${id}_productId", "") ?: "",
            productName = prefs.getString("sale_${id}_productName", "") ?: "",
            productPrice = prefs.getFloat("sale_${id}_productPrice", 0f).toDouble(),
            buyerName = prefs.getString("sale_${id}_buyerName", "") ?: "",
            buyerEmail = prefs.getString("sale_${id}_buyerEmail", "") ?: "",
            salePrice = prefs.getFloat("sale_${id}_salePrice", 0f).toDouble(),
            commissionEarned = prefs.getFloat("sale_${id}_commissionEarned", 0f).toDouble(),
            dateSubmitted = prefs.getLong("sale_${id}_dateSubmitted", System.currentTimeMillis()),
            status = prefs.getString("sale_${id}_status", "Pending Approval") ?: "Pending Approval",
            paymentReference = prefs.getString("sale_${id}_paymentReference", "") ?: "",
            rejectionReason = prefs.getString("sale_${id}_rejectionReason", "") ?: ""
        )
    }

    fun getAllReferralSalesLocally(): List<com.example.data.model.ProductReferralSale> {
        val ids = prefs.getStringSet("simulated_sale_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getReferralSaleLocally(it) }.sortedByDescending { it.dateSubmitted }
    }

    suspend fun saveReferralSale(sale: com.example.data.model.ProductReferralSale): Boolean {
        saveReferralSaleLocally(sale)
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("referral_sales").document(sale.id).set(sale).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving referral sale to Firestore", e)
                return false
            }
        }
        return true
    }

    suspend fun fetchReferralSalesForUser(referrerUid: String): List<com.example.data.model.ProductReferralSale> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("referral_sales")
                    .whereEqualTo("referrerUid", referrerUid)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.ProductReferralSale::class.java) }
                list.forEach { saveReferralSaleLocally(it) }
                return list.sortedByDescending { it.dateSubmitted }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user referral sales from Firestore", e)
            }
        }
        return getAllReferralSalesLocally().filter { it.referrerUid == referrerUid }.sortedByDescending { it.dateSubmitted }
    }

    suspend fun fetchAllReferralSales(): List<com.example.data.model.ProductReferralSale> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("referral_sales").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.ProductReferralSale::class.java) }
                list.forEach { saveReferralSaleLocally(it) }
                return list.sortedByDescending { it.dateSubmitted }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all referral sales from Firestore", e)
            }
        }
        return getAllReferralSalesLocally()
    }


    // --- WITHDRAWAL REQUESTS PERSISTENCE & APIs ---
    fun saveWithdrawalRequestLocally(withdrawal: com.example.data.model.WithdrawalRequest) {
        val editor = prefs.edit()
        val id = withdrawal.id
        editor.putString("wd_${id}_id", id)
        editor.putString("wd_${id}_userUid", withdrawal.userUid)
        editor.putString("wd_${id}_userEmail", withdrawal.userEmail)
        editor.putFloat("wd_${id}_amount", withdrawal.amount.toFloat())
        editor.putString("wd_${id}_payoutMethod", withdrawal.payoutMethod)
        editor.putString("wd_${id}_payoutDetails", withdrawal.payoutDetails)
        editor.putLong("wd_${id}_dateSubmitted", withdrawal.dateSubmitted)
        editor.putString("wd_${id}_status", withdrawal.status)
        editor.putLong("wd_${id}_completionDate", withdrawal.completionDate)
        editor.putString("wd_${id}_transactionHash", withdrawal.transactionHash)
        editor.putString("wd_${id}_adminNotes", withdrawal.adminNotes)

        val ids = (prefs.getStringSet("simulated_wd_ids", emptySet()) ?: emptySet()).toMutableSet()
        ids.add(id)
        editor.putStringSet("simulated_wd_ids", ids)
        editor.apply()
    }

    fun getWithdrawalRequestLocally(id: String): com.example.data.model.WithdrawalRequest? {
        if (!prefs.contains("wd_${id}_id")) return null
        return com.example.data.model.WithdrawalRequest(
            id = prefs.getString("wd_${id}_id", "") ?: "",
            userUid = prefs.getString("wd_${id}_userUid", "") ?: "",
            userEmail = prefs.getString("wd_${id}_userEmail", "") ?: "",
            amount = prefs.getFloat("wd_${id}_amount", 0f).toDouble(),
            payoutMethod = prefs.getString("wd_${id}_payoutMethod", "") ?: "",
            payoutDetails = prefs.getString("wd_${id}_payoutDetails", "") ?: "",
            dateSubmitted = prefs.getLong("wd_${id}_dateSubmitted", System.currentTimeMillis()),
            status = prefs.getString("wd_${id}_status", "Pending Approval") ?: "Pending Approval",
            completionDate = prefs.getLong("wd_${id}_completionDate", 0L),
            transactionHash = prefs.getString("wd_${id}_transactionHash", "") ?: "",
            adminNotes = prefs.getString("wd_${id}_adminNotes", "") ?: ""
        )
    }

    fun getAllWithdrawalRequestsLocally(): List<com.example.data.model.WithdrawalRequest> {
        val ids = prefs.getStringSet("simulated_wd_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { getWithdrawalRequestLocally(it) }.sortedByDescending { it.dateSubmitted }
    }

    suspend fun saveWithdrawalRequest(withdrawal: com.example.data.model.WithdrawalRequest): Boolean {
        saveWithdrawalRequestLocally(withdrawal)
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("withdrawals").document(withdrawal.id).set(withdrawal).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving withdrawal to Firestore", e)
                return false
            }
        }
        return true
    }

    suspend fun fetchWithdrawalsForUser(userUid: String): List<com.example.data.model.WithdrawalRequest> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("withdrawals")
                    .whereEqualTo("userUid", userUid)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.WithdrawalRequest::class.java) }
                list.forEach { saveWithdrawalRequestLocally(it) }
                return list.sortedByDescending { it.dateSubmitted }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user withdrawals from Firestore", e)
            }
        }
        return getAllWithdrawalRequestsLocally().filter { it.userUid == userUid }.sortedByDescending { it.dateSubmitted }
    }

    suspend fun fetchAllWithdrawalRequests(): List<com.example.data.model.WithdrawalRequest> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("withdrawals").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.WithdrawalRequest::class.java) }
                list.forEach { saveWithdrawalRequestLocally(it) }
                return list.sortedByDescending { it.dateSubmitted }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all withdrawals from Firestore", e)
            }
        }
        return getAllWithdrawalRequestsLocally()
    }

    // --- JSON SERIALIZATION HELPERS FOR PRODUCTS & PARTNERS ---
    private fun productToJSON(p: com.example.data.model.AffiliateProduct): org.json.JSONObject {
        val jo = org.json.JSONObject()
        jo.put("id", p.id)
        jo.put("name", p.name)
        jo.put("description", p.description)
        jo.put("category", p.category)
        jo.put("subcategory", p.subcategory)
        jo.put("brand", p.brand)
        
        val jaImages = org.json.JSONArray()
        p.images.forEach { jaImages.put(it) }
        jo.put("images", jaImages)
        
        jo.put("price", p.price)
        p.discountPrice?.let { jo.put("discountPrice", it) }
        jo.put("currency", p.currency)
        jo.put("affiliateLink", p.affiliateLink)
        jo.put("merchantName", p.merchantName)
        jo.put("stockStatus", p.stockStatus)
        jo.put("rating", p.rating)
        jo.put("reviewCount", p.reviewCount)
        
        val joSpecs = org.json.JSONObject()
        p.specifications.forEach { (k, v) -> joSpecs.put(k, v) }
        jo.put("specifications", joSpecs)
        
        jo.put("dateAdded", p.dateAdded)
        jo.put("lastUpdated", p.lastUpdated)
        jo.put("isFeatured", p.isFeatured)
        jo.put("isTrending", p.isTrending)
        jo.put("isRecommended", p.isRecommended)
        jo.put("isArchived", p.isArchived)
        jo.put("partnerId", p.partnerId)
        jo.put("commissionPercent", p.commissionPercent)
        
        val jaTags = org.json.JSONArray()
        p.tags.forEach { jaTags.put(it) }
        jo.put("tags", jaTags)
        
        // Multi-vendor and delivery fields
        jo.put("status", p.status)
        jo.put("sellerId", p.sellerId)
        jo.put("sellerName", p.sellerName)
        jo.put("rejectionReason", p.rejectionReason)
        jo.put("availableQuantity", p.availableQuantity)
        jo.put("deliveryFee", p.deliveryFee)
        jo.put("deliveryRegions", p.deliveryRegions)
        jo.put("estimatedDeliveryTime", p.estimatedDeliveryTime)
        return jo
    }

    private fun jsonToProduct(jo: org.json.JSONObject): com.example.data.model.AffiliateProduct {
        val id = jo.optString("id", "")
        val name = jo.optString("name", "")
        val description = jo.optString("description", "")
        val category = jo.optString("category", "")
        val subcategory = jo.optString("subcategory", "")
        val brand = jo.optString("brand", "")
        
        val jaImages = jo.optJSONArray("images")
        val images = mutableListOf<String>()
        if (jaImages != null) {
            for (i in 0 until jaImages.length()) {
                images.add(jaImages.getString(i))
            }
        }
        
        val price = jo.optDouble("price", 0.0)
        val discountPrice = if (jo.has("discountPrice")) jo.getDouble("discountPrice") else null
        val currency = jo.optString("currency", "USD")
        val affiliateLink = jo.optString("affiliateLink", "")
        val merchantName = jo.optString("merchantName", "")
        val stockStatus = jo.optString("stockStatus", "In Stock")
        val rating = jo.optDouble("rating", 5.0)
        val reviewCount = jo.optInt("reviewCount", 0)
        
        val joSpecs = jo.optJSONObject("specifications")
        val specs = mutableMapOf<String, String>()
        if (joSpecs != null) {
            val keys = joSpecs.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                specs[key] = joSpecs.getString(key)
            }
        }
        
        val dateAdded = jo.optLong("dateAdded", System.currentTimeMillis())
        val lastUpdated = jo.optLong("lastUpdated", System.currentTimeMillis())
        val isFeatured = jo.optBoolean("isFeatured", false)
        val isTrending = jo.optBoolean("isTrending", false)
        val isRecommended = jo.optBoolean("isRecommended", false)
        val isArchived = jo.optBoolean("isArchived", false)
        val partnerId = jo.optString("partnerId", "")
        val commissionPercent = jo.optDouble("commissionPercent", 40.0)
        
        val jaTags = jo.optJSONArray("tags")
        val tags = mutableListOf<String>()
        if (jaTags != null) {
            for (i in 0 until jaTags.length()) {
                tags.add(jaTags.getString(i))
            }
        }
        
        return com.example.data.model.AffiliateProduct(
            id = id, name = name, description = description, category = category, subcategory = subcategory,
            brand = brand, images = images, price = price, discountPrice = discountPrice, currency = currency,
            affiliateLink = affiliateLink, merchantName = merchantName, stockStatus = stockStatus,
            rating = rating, reviewCount = reviewCount, specifications = specs, dateAdded = dateAdded,
            lastUpdated = lastUpdated, isFeatured = isFeatured, isTrending = isTrending,
            isRecommended = isRecommended, isArchived = isArchived, partnerId = partnerId, tags = tags,
            commissionPercent = commissionPercent,
            status = jo.optString("status", "Approved"),
            sellerId = jo.optString("sellerId", ""),
            sellerName = jo.optString("sellerName", ""),
            rejectionReason = jo.optString("rejectionReason", ""),
            availableQuantity = jo.optInt("availableQuantity", 100),
            deliveryFee = jo.optDouble("deliveryFee", 0.0),
            deliveryRegions = jo.optString("deliveryRegions", "All Regions"),
            estimatedDeliveryTime = jo.optString("estimatedDeliveryTime", "3-5 business days")
        )
    }

    private fun partnerToJSON(p: com.example.data.model.AffiliatePartner): org.json.JSONObject {
        val jo = org.json.JSONObject()
        jo.put("id", p.id)
        jo.put("name", p.name)
        jo.put("description", p.description)
        jo.put("status", p.status)
        jo.put("logoUrl", p.logoUrl)
        jo.put("commissionRate", p.commissionRate)
        jo.put("websiteUrl", p.websiteUrl)
        jo.put("apiKey", p.apiKey)
        jo.put("apiSecret", p.apiSecret)
        jo.put("trackingId", p.trackingId)
        
        val joExtra = org.json.JSONObject()
        p.extraConfig.forEach { (k, v) -> joExtra.put(k, v) }
        jo.put("extraConfig", joExtra)
        return jo
    }

    private fun jsonToPartner(jo: org.json.JSONObject): com.example.data.model.AffiliatePartner {
        val extraMap = mutableMapOf<String, String>()
        val joExtra = jo.optJSONObject("extraConfig")
        if (joExtra != null) {
            val keys = joExtra.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                extraMap[key] = joExtra.getString(key)
            }
        }
        return com.example.data.model.AffiliatePartner(
            id = jo.optString("id", ""),
            name = jo.optString("name", ""),
            description = jo.optString("description", ""),
            status = jo.optString("status", "Active"),
            logoUrl = jo.optString("logoUrl", ""),
            commissionRate = jo.optDouble("commissionRate", 0.0),
            websiteUrl = jo.optString("websiteUrl", ""),
            apiKey = jo.optString("apiKey", ""),
            apiSecret = jo.optString("apiSecret", ""),
            trackingId = jo.optString("trackingId", ""),
            extraConfig = extraMap
        )
    }

    // --- MARKETPLACE INITIALIZATION FLOW ---
    fun initializeDefaultMarketplaceData() {
        if (!prefs.contains("has_initialized_marketplace")) {
            val defaultPartners = listOf(
                com.example.data.model.AffiliatePartner(
                    id = "amazon_associates",
                    name = "Amazon Associates",
                    description = "Earn commissions on physical and digital products across Amazon's global marketplace.",
                    status = "Active",
                    logoUrl = "ic_amazon",
                    commissionRate = 3.5,
                    websiteUrl = "https://affiliate-program.amazon.com",
                    apiKey = "",
                    apiSecret = "",
                    trackingId = "wealthb-20"
                ),
                com.example.data.model.AffiliatePartner(
                    id = "wealth_builder_direct",
                    name = "Wealth Builder Direct",
                    description = "Direct premium software tools and educational programs with high-recurring commissions.",
                    status = "Active",
                    logoUrl = "ic_direct",
                    commissionRate = 40.0,
                    websiteUrl = "https://wealthbuilder.com/affiliates"
                )
            )
            
            val defaultCategories = listOf(
                com.example.data.model.MarketplaceCategory("cat_tech", "Tech & Gadgets", listOf("Laptops", "Smartphones", "Audio", "Accessories")),
                com.example.data.model.MarketplaceCategory("cat_business", "E-Learning & Business", listOf("Courses", "Software Tools", "E-Books")),
                com.example.data.model.MarketplaceCategory("cat_productivity", "Books & Productivity", listOf("Self-Help", "Finance", "Planners")),
                com.example.data.model.MarketplaceCategory("cat_office", "Home Office", listOf("Furniture", "Lighting", "Monitors"))
            )
            
            val defaultBrands = listOf(
                com.example.data.model.MarketplaceBrand("brand_apple", "Apple"),
                com.example.data.model.MarketplaceBrand("brand_sony", "Sony"),
                com.example.data.model.MarketplaceBrand("brand_wb", "Wealth Builder"),
                com.example.data.model.MarketplaceBrand("brand_harper", "Harper Business"),
                com.example.data.model.MarketplaceBrand("brand_steelcase", "Steelcase")
            )
            
            val defaultProducts = listOf(
                com.example.data.model.AffiliateProduct(
                    id = "prod_macbook",
                    name = "MacBook Pro M3 Max",
                    description = "The ultimate mobile workstation for developers, creators, and online business systems. Features a 14-core CPU, 30-core GPU, and up to 128GB of unified memory.",
                    category = "Tech & Gadgets",
                    subcategory = "Laptops",
                    brand = "Apple",
                    images = listOf("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800"),
                    price = 2499.0,
                    discountPrice = 2299.0,
                    currency = "USD",
                    affiliateLink = "https://www.amazon.com/dp/B0CM5N1ZNW?tag=wealthb-20",
                    merchantName = "Amazon",
                    stockStatus = "In Stock",
                    rating = 4.8,
                    reviewCount = 1240,
                    specifications = mapOf("Processor" to "M3 Max", "RAM" to "36GB", "Storage" to "1TB SSD", "Screen Size" to "16-inch Liquid Retina XDR"),
                    isFeatured = true,
                    isRecommended = true,
                    partnerId = "amazon_associates",
                    tags = listOf("Laptop", "Premium", "Creator", "Workstation")
                ),
                com.example.data.model.AffiliateProduct(
                    id = "prod_sony_headphones",
                    name = "Sony WH-1000XM5 ANC Headphones",
                    description = "Industry-leading noise canceling wireless headphones with auto noise-canceling optimizer, crystal-clear hands-free calling, and Alexa voice control.",
                    category = "Tech & Gadgets",
                    subcategory = "Audio",
                    brand = "Sony",
                    images = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800"),
                    price = 398.0,
                    discountPrice = 348.0,
                    currency = "USD",
                    affiliateLink = "https://www.amazon.com/dp/B09XS7JWHH?tag=wealthb-20",
                    merchantName = "Amazon",
                    stockStatus = "In Stock",
                    rating = 4.6,
                    reviewCount = 8520,
                    specifications = mapOf("Battery Life" to "Up to 30 hours", "Charging" to "USB-C quick charge", "Bluetooth Version" to "5.2", "Weight" to "250g"),
                    isTrending = true,
                    partnerId = "amazon_associates",
                    tags = listOf("Headphones", "Noise Canceling", "Audio", "Travel")
                ),
                com.example.data.model.AffiliateProduct(
                    id = "prod_wb_blueprint",
                    name = "Wealth Builder Pro Academy",
                    description = "The absolute step-by-step masterclass to launch, scale, and automate digital marketing funnels, freelancing systems, and digital product assets. Over 40 hours of practical video training.",
                    category = "E-Learning & Business",
                    subcategory = "Courses",
                    brand = "Wealth Builder",
                    images = listOf("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"),
                    price = 497.0,
                    discountPrice = 197.0,
                    currency = "USD",
                    affiliateLink = "https://wealthbuilder.com/pro-academy?aff=123",
                    merchantName = "Wealth Builder Hub",
                    stockStatus = "In Stock",
                    rating = 4.9,
                    reviewCount = 380,
                    specifications = mapOf("Duration" to "Lifetime Access", "Modules" to "12 comprehensive modules", "Support" to "Weekly live Q&A", "Community" to "Private Slack Access"),
                    isFeatured = true,
                    isRecommended = true,
                    isTrending = true,
                    partnerId = "wealth_builder_direct",
                    tags = listOf("Course", "Passive Income", "Funnel Builder", "Wealth Systems")
                ),
                com.example.data.model.AffiliateProduct(
                    id = "prod_intelligent_investor",
                    name = "The Intelligent Investor Book",
                    description = "The classic text on value investing, with updated commentary by Jason Zweig. Widely considered the stock market bible, teaching principles of long-term wealth preservation.",
                    category = "Books & Productivity",
                    subcategory = "Finance",
                    brand = "Harper Business",
                    images = listOf("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"),
                    price = 24.99,
                    discountPrice = 14.79,
                    currency = "USD",
                    affiliateLink = "https://www.amazon.com/dp/0060555661?tag=wealthb-20",
                    merchantName = "Amazon",
                    stockStatus = "In Stock",
                    rating = 4.7,
                    reviewCount = 28400,
                    specifications = mapOf("Format" to "Paperback", "Pages" to "640 pages", "Publisher" to "Harper Business", "Language" to "English"),
                    isRecommended = true,
                    partnerId = "amazon_associates",
                    tags = listOf("Investing", "Finance", "Stocks", "Value Investing", "Best Seller")
                )
            )
            
            // Save Default Partners
            val jaPartners = org.json.JSONArray()
            defaultPartners.forEach { jaPartners.put(partnerToJSON(it)) }
            prefs.edit().putString("aff_partners_json", jaPartners.toString()).apply()
            
            // Save Default Categories
            val jaCats = org.json.JSONArray()
            defaultCategories.forEach { c ->
                val jo = org.json.JSONObject()
                jo.put("id", c.id)
                jo.put("name", c.name)
                val jaSubs = org.json.JSONArray()
                c.subcategories.forEach { jaSubs.put(it) }
                jo.put("subcategories", jaSubs)
                jaCats.put(jo)
            }
            prefs.edit().putString("marketplace_categories_json", jaCats.toString()).apply()
            
            // Save Default Brands
            val jaBrands = org.json.JSONArray()
            defaultBrands.forEach { b ->
                val jo = org.json.JSONObject()
                jo.put("id", b.id)
                jo.put("name", b.name)
                jaBrands.put(jo)
            }
            prefs.edit().putString("marketplace_brands_json", jaBrands.toString()).apply()
            
            // Save Default Products
            val jaProds = org.json.JSONArray()
            defaultProducts.forEach { jaProds.put(productToJSON(it)) }
            prefs.edit().putString("aff_products_json", jaProds.toString()).apply()
            
            prefs.edit().putBoolean("has_initialized_marketplace", true).apply()
        }
    }

    // --- AFFILIATE PARTNER METHODS ---
    suspend fun saveAffiliatePartner(partner: com.example.data.model.AffiliatePartner): Boolean {
        val partnersList = fetchAffiliatePartners().toMutableList()
        val index = partnersList.indexOfFirst { it.id == partner.id }
        if (index != -1) {
            partnersList[index] = partner
        } else {
            partnersList.add(partner)
        }
        val ja = org.json.JSONArray()
        partnersList.forEach { ja.put(partnerToJSON(it)) }
        prefs.edit().putString("aff_partners_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("partners")
                    .document(partner.id)
                    .set(partner)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving partner to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun deleteAffiliatePartner(id: String): Boolean {
        val partnersList = fetchAffiliatePartners().toMutableList()
        partnersList.removeAll { it.id == id }
        val ja = org.json.JSONArray()
        partnersList.forEach { ja.put(partnerToJSON(it)) }
        prefs.edit().putString("aff_partners_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("partners")
                    .document(id)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting partner from Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchAffiliatePartners(): List<com.example.data.model.AffiliatePartner> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("partners").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.AffiliatePartner::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { ja.put(partnerToJSON(it)) }
                    prefs.edit().putString("aff_partners_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching partners from Firestore", e)
            }
        }
        val partnersStr = prefs.getString("aff_partners_json", "") ?: ""
        if (partnersStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.AffiliatePartner>()
        try {
            val ja = org.json.JSONArray(partnersStr)
            for (i in 0 until ja.length()) {
                list.add(jsonToPartner(ja.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing partners from SharedPreferences", e)
        }
        return list
    }

    // --- AFFILIATE PRODUCT METHODS ---
    suspend fun saveProduct(product: com.example.data.model.AffiliateProduct): Boolean {
        val productsList = fetchProducts().toMutableList()
        val index = productsList.indexOfFirst { it.id == product.id }
        if (index != -1) {
            productsList[index] = product
        } else {
            productsList.add(product)
        }
        val ja = org.json.JSONArray()
        productsList.forEach { ja.put(productToJSON(it)) }
        prefs.edit().putString("aff_products_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("products")
                    .document(product.id)
                    .set(product)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving product to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun deleteProduct(id: String): Boolean {
        val productsList = fetchProducts().toMutableList()
        productsList.removeAll { it.id == id }
        val ja = org.json.JSONArray()
        productsList.forEach { ja.put(productToJSON(it)) }
        prefs.edit().putString("aff_products_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("products")
                    .document(id)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting product from Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchProducts(): List<com.example.data.model.AffiliateProduct> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("products").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.AffiliateProduct::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { ja.put(productToJSON(it)) }
                    prefs.edit().putString("aff_products_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching products from Firestore", e)
            }
        }
        val productsStr = prefs.getString("aff_products_json", "") ?: ""
        if (productsStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.AffiliateProduct>()
        try {
            val ja = org.json.JSONArray(productsStr)
            for (i in 0 until ja.length()) {
                list.add(jsonToProduct(ja.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing products from SharedPreferences", e)
        }
        return list
    }

    // --- MARKETPLACE CATEGORY METHODS ---
    suspend fun saveMarketplaceCategory(category: com.example.data.model.MarketplaceCategory): Boolean {
        val list = fetchMarketplaceCategories().toMutableList()
        val idx = list.indexOfFirst { it.id == category.id }
        if (idx != -1) list[idx] = category else list.add(category)
        
        val ja = org.json.JSONArray()
        list.forEach { c ->
            val jo = org.json.JSONObject()
            jo.put("id", c.id)
            jo.put("name", c.name)
            val jaSubs = org.json.JSONArray()
            c.subcategories.forEach { jaSubs.put(it) }
            jo.put("subcategories", jaSubs)
            ja.put(jo)
        }
        prefs.edit().putString("marketplace_categories_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("categories")
                    .document(category.id)
                    .set(category)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving category to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchMarketplaceCategories(): List<com.example.data.model.MarketplaceCategory> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("categories").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.MarketplaceCategory::class.java) }
                if (list.isNotEmpty()) {
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching categories from Firestore", e)
            }
        }
        val categoriesStr = prefs.getString("marketplace_categories_json", "") ?: ""
        if (categoriesStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.MarketplaceCategory>()
        try {
            val ja = org.json.JSONArray(categoriesStr)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                val jaSubs = jo.optJSONArray("subcategories")
                val subs = mutableListOf<String>()
                if (jaSubs != null) {
                    for (j in 0 until jaSubs.length()) {
                        subs.add(jaSubs.getString(j))
                    }
                }
                list.add(com.example.data.model.MarketplaceCategory(jo.getString("id"), jo.getString("name"), subs))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing categories", e)
        }
        return list
    }

    // --- MARKETPLACE BRAND METHODS ---
    suspend fun saveMarketplaceBrand(brand: com.example.data.model.MarketplaceBrand): Boolean {
        val list = fetchMarketplaceBrands().toMutableList()
        val idx = list.indexOfFirst { it.id == brand.id }
        if (idx != -1) list[idx] = brand else list.add(brand)
        
        val ja = org.json.JSONArray()
        list.forEach { b ->
            val jo = org.json.JSONObject()
            jo.put("id", b.id)
            jo.put("name", b.name)
            ja.put(jo)
        }
        prefs.edit().putString("marketplace_brands_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("brands")
                    .document(brand.id)
                    .set(brand)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving brand to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchMarketplaceBrands(): List<com.example.data.model.MarketplaceBrand> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("brands").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.MarketplaceBrand::class.java) }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching brands from Firestore", e)
            }
        }
        val brandsStr = prefs.getString("marketplace_brands_json", "") ?: ""
        if (brandsStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.MarketplaceBrand>()
        try {
            val ja = org.json.JSONArray(brandsStr)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(com.example.data.model.MarketplaceBrand(jo.getString("id"), jo.getString("name")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing brands", e)
        }
        return list
    }

    // --- PRODUCT ANALYTICS METHODS ---
    suspend fun recordProductView(productId: String): Boolean {
        val key = "analytics_views_$productId"
        val currentViews = prefs.getLong(key, 0L)
        prefs.edit().putLong(key, currentViews + 1L).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                val docRef = firestore!!.collection("analytics").document(productId)
                val snap = docRef.get().await()
                var clicks = 0L
                var views = 0L
                if (snap.exists()) {
                    clicks = snap.getLong("clicks") ?: 0L
                    views = snap.getLong("views") ?: 0L
                }
                docRef.set(mapOf("productId" to productId, "views" to (views + 1L), "clicks" to clicks)).await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving product view analytics to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun recordProductClick(productId: String): Boolean {
        val key = "analytics_clicks_$productId"
        val currentClicks = prefs.getLong(key, 0L)
        prefs.edit().putLong(key, currentClicks + 1L).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                val docRef = firestore!!.collection("analytics").document(productId)
                val snap = docRef.get().await()
                var clicks = 0L
                var views = 0L
                if (snap.exists()) {
                    clicks = snap.getLong("clicks") ?: 0L
                    views = snap.getLong("views") ?: 0L
                }
                docRef.set(mapOf("productId" to productId, "views" to views, "clicks" to (clicks + 1L))).await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving product click analytics to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchMarketplaceAnalytics(): com.example.data.model.MarketplaceAnalytics {
        val products = fetchProducts()
        val totalProdCount = products.size
        var totalViews = 0L
        var totalClicks = 0L
        val viewsMap = mutableMapOf<String, Long>()
        val clicksMap = mutableMapOf<String, Long>()

        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("analytics").get().await()
                snapshot.documents.forEach { doc ->
                    val pId = doc.id
                    val views = doc.getLong("views") ?: 0L
                    val clicks = doc.getLong("clicks") ?: 0L
                    viewsMap[pId] = views
                    clicksMap[pId] = clicks
                    totalViews += views
                    totalClicks += clicks
                }
                return com.example.data.model.MarketplaceAnalytics(totalProdCount, totalViews, totalClicks, viewsMap, clicksMap)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching analytics from Firestore", e)
            }
        }

        // SharedPreferences fallback
        products.forEach { p ->
            val v = prefs.getLong("analytics_views_${p.id}", 0L)
            val c = prefs.getLong("analytics_clicks_${p.id}", 0L)
            viewsMap[p.id] = v
            clicksMap[p.id] = c
            totalViews += v
            totalClicks += c
        }
        return com.example.data.model.MarketplaceAnalytics(totalProdCount, totalViews, totalClicks, viewsMap, clicksMap)
    }

    // --- WISHLIST / FAVORITES METHODS ---
    suspend fun saveUserWishlist(userId: String, wishlist: List<String>): Boolean {
        prefs.edit().putString("wishlist_$userId", wishlist.joinToString(",")).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("wishlists")
                    .document(userId)
                    .set(com.example.data.model.UserWishlist(userId, wishlist))
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving wishlist to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchUserWishlist(userId: String): List<String> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val doc = firestore!!.collection("wishlists").document(userId).get().await()
                val obj = doc.toObject(com.example.data.model.UserWishlist::class.java)
                if (obj != null) {
                    prefs.edit().putString("wishlist_$userId", obj.productIds.joinToString(",")).apply()
                    return obj.productIds
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching wishlist from Firestore", e)
            }
        }
        val str = prefs.getString("wishlist_$userId", "") ?: ""
        return if (str.isEmpty()) emptyList() else str.split(",")
    }

    // --- MULTI-VENDOR ORDER METHODS ---
    suspend fun saveOrder(order: com.example.data.model.Order): Boolean {
        val ordersList = fetchAllOrders().toMutableList()
        val index = ordersList.indexOfFirst { it.id == order.id }
        if (index != -1) {
            ordersList[index] = order
        } else {
            ordersList.add(order)
        }
        val ja = org.json.JSONArray()
        ordersList.forEach { o ->
            val jo = org.json.JSONObject()
            jo.put("id", o.id)
            jo.put("buyerId", o.buyerId)
            jo.put("buyerName", o.buyerName)
            jo.put("buyerEmail", o.buyerEmail)
            jo.put("productId", o.productId)
            jo.put("productName", o.productName)
            jo.put("productPrice", o.productPrice)
            jo.put("quantity", o.quantity)
            jo.put("originalPrice", o.originalPrice)
            jo.put("discountAmount", o.discountAmount)
            jo.put("finalPayableAmount", o.finalPayableAmount)
            jo.put("affiliateId", o.affiliateId)
            jo.put("sellerId", o.sellerId)
            jo.put("status", o.status)
            jo.put("dateCreated", o.dateCreated)
            jo.put("lastUpdated", o.lastUpdated)
            jo.put("shippingAddress", o.shippingAddress)
            jo.put("paymentMethod", o.paymentMethod)
            jo.put("couponCode", o.couponCode)
            jo.put("deliveryFee", o.deliveryFee)
            jo.put("paymentProofUrl", o.paymentProofUrl)
            jo.put("paymentReference", o.paymentReference)
            jo.put("adminNotes", o.adminNotes)
            ja.put(jo)
        }
        prefs.edit().putString("mv_orders_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            return try {
                firestore!!.collection("orders")
                    .document(order.id)
                    .set(order)
                    .await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving order to Firestore", e)
                false
            }
        }
        return true
    }

    suspend fun fetchAllOrders(): List<com.example.data.model.Order> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("orders").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.Order::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { o ->
                        val jo = org.json.JSONObject()
                        jo.put("id", o.id)
                        jo.put("buyerId", o.buyerId)
                        jo.put("buyerName", o.buyerName)
                        jo.put("buyerEmail", o.buyerEmail)
                        jo.put("productId", o.productId)
                        jo.put("productName", o.productName)
                        jo.put("productPrice", o.productPrice)
                        jo.put("quantity", o.quantity)
                        jo.put("originalPrice", o.originalPrice)
                        jo.put("discountAmount", o.discountAmount)
                        jo.put("finalPayableAmount", o.finalPayableAmount)
                        jo.put("affiliateId", o.affiliateId)
                        jo.put("sellerId", o.sellerId)
                        jo.put("status", o.status)
                        jo.put("dateCreated", o.dateCreated)
                        jo.put("lastUpdated", o.lastUpdated)
                        jo.put("shippingAddress", o.shippingAddress)
                        jo.put("paymentMethod", o.paymentMethod)
                        jo.put("couponCode", o.couponCode)
                        jo.put("deliveryFee", o.deliveryFee)
                        jo.put("paymentProofUrl", o.paymentProofUrl)
                        jo.put("paymentReference", o.paymentReference)
                        jo.put("adminNotes", o.adminNotes)
                        ja.put(jo)
                    }
                    prefs.edit().putString("mv_orders_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching orders from Firestore", e)
            }
        }
        val ordersStr = prefs.getString("mv_orders_json", "") ?: ""
        if (ordersStr.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.Order>()
        try {
            val ja = org.json.JSONArray(ordersStr)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.Order(
                        id = jo.optString("id", ""),
                        buyerId = jo.optString("buyerId", ""),
                        buyerName = jo.optString("buyerName", ""),
                        buyerEmail = jo.optString("buyerEmail", ""),
                        productId = jo.optString("productId", ""),
                        productName = jo.optString("productName", ""),
                        productPrice = jo.optDouble("productPrice", 0.0),
                        quantity = jo.optInt("quantity", 1),
                        originalPrice = jo.optDouble("originalPrice", 0.0),
                        discountAmount = jo.optDouble("discountAmount", 0.0),
                        finalPayableAmount = jo.optDouble("finalPayableAmount", 0.0),
                        affiliateId = jo.optString("affiliateId", ""),
                        sellerId = jo.optString("sellerId", ""),
                        status = jo.optString("status", "Pending"),
                        dateCreated = jo.optLong("dateCreated", System.currentTimeMillis()),
                        lastUpdated = jo.optLong("lastUpdated", System.currentTimeMillis()),
                        shippingAddress = jo.optString("shippingAddress", ""),
                        paymentMethod = jo.optString("paymentMethod", "Wallet Balance"),
                        couponCode = jo.optString("couponCode", ""),
                        deliveryFee = jo.optDouble("deliveryFee", 0.0),
                        paymentProofUrl = jo.optString("paymentProofUrl", ""),
                        paymentReference = jo.optString("paymentReference", ""),
                        adminNotes = jo.optString("adminNotes", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing orders from SharedPreferences", e)
        }
        return list
    }

    // --- REVIEWS METHODS ---
    suspend fun saveProductReview(review: com.example.data.model.ProductReview): Boolean {
        val reviewsList = fetchProductReviews(review.productId).toMutableList()
        reviewsList.add(review)
        val ja = org.json.JSONArray()
        reviewsList.forEach { r ->
            val jo = org.json.JSONObject()
            jo.put("id", r.id)
            jo.put("productId", r.productId)
            jo.put("buyerId", r.buyerId)
            jo.put("buyerName", r.buyerName)
            jo.put("rating", r.rating)
            jo.put("reviewText", r.reviewText)
            r.imageUrl?.let { jo.put("imageUrl", it) }
            jo.put("dateCreated", r.dateCreated)
            ja.put(jo)
        }
        prefs.edit().putString("product_reviews_${review.productId}", ja.toString()).apply()
        return true
    }

    suspend fun fetchProductReviews(productId: String): List<com.example.data.model.ProductReview> {
        val str = prefs.getString("product_reviews_$productId", "") ?: ""
        if (str.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.ProductReview>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.ProductReview(
                        id = jo.optString("id", ""),
                        productId = jo.optString("productId", ""),
                        buyerId = jo.optString("buyerId", ""),
                        buyerName = jo.optString("buyerName", ""),
                        rating = jo.optInt("rating", 5),
                        reviewText = jo.optString("reviewText", ""),
                        imageUrl = if (jo.has("imageUrl")) jo.getString("imageUrl") else null,
                        dateCreated = jo.optLong("dateCreated", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing reviews for $productId", e)
        }
        return list
    }

    // --- LIVE CHAT METHODS ---
    suspend fun saveChatMessage(message: com.example.data.model.ChatMessage): Boolean {
        val chatList = fetchAllChatMessages().toMutableList()
        chatList.add(message)
        val ja = org.json.JSONArray()
        chatList.forEach { m ->
            val jo = org.json.JSONObject()
            jo.put("id", m.id)
            jo.put("senderId", m.senderId)
            jo.put("senderName", m.senderName)
            jo.put("receiverId", m.receiverId)
            jo.put("message", m.message)
            jo.put("timestamp", m.timestamp)
            ja.put(jo)
        }
        prefs.edit().putString("chat_messages_json", ja.toString()).apply()
        return true
    }

    suspend fun fetchAllChatMessages(): List<com.example.data.model.ChatMessage> {
        val str = prefs.getString("chat_messages_json", "") ?: ""
        if (str.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.ChatMessage>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.ChatMessage(
                        id = jo.optString("id", ""),
                        senderId = jo.optString("senderId", ""),
                        senderName = jo.optString("senderName", ""),
                        receiverId = jo.optString("receiverId", ""),
                        message = jo.optString("message", ""),
                        timestamp = jo.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing chat messages", e)
        }
        return list
    }

    // --- COUPON METHODS ---
    suspend fun saveCoupon(coupon: com.example.data.model.Coupon): Boolean {
        val couponsList = fetchCoupons().toMutableList()
        val index = couponsList.indexOfFirst { it.code.trim().equals(coupon.code.trim(), ignoreCase = true) }
        if (index != -1) {
            couponsList[index] = coupon
        } else {
            couponsList.add(coupon)
        }
        val ja = org.json.JSONArray()
        couponsList.forEach { c ->
            val jo = org.json.JSONObject()
            jo.put("code", c.code)
            jo.put("discountType", c.discountType)
            jo.put("discountValue", c.discountValue)
            jo.put("expiryDate", c.expiryDate)
            jo.put("usageLimit", c.usageLimit)
            jo.put("usageCount", c.usageCount)
            ja.put(jo)
        }
        prefs.edit().putString("coupons_json", ja.toString()).apply()
        return true
    }

    suspend fun fetchCoupons(): List<com.example.data.model.Coupon> {
        val str = prefs.getString("coupons_json", "") ?: ""
        if (str.isEmpty()) {
            val defaultCoupons = listOf(
                com.example.data.model.Coupon("LAUNCH10", "Percentage", 10.0, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, 100, 0),
                com.example.data.model.Coupon("WELCOME500", "Fixed", 500.0, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, 50, 0)
            )
            return defaultCoupons
        }
        val list = mutableListOf<com.example.data.model.Coupon>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.Coupon(
                        code = jo.optString("code", ""),
                        discountType = jo.optString("discountType", "Percentage"),
                        discountValue = jo.optDouble("discountValue", 0.0),
                        expiryDate = jo.optLong("expiryDate", 0L),
                        usageLimit = jo.optInt("usageLimit", 100),
                        usageCount = jo.optInt("usageCount", 0)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing coupons", e)
        }
        return list
    }

    suspend fun deleteCoupon(code: String): Boolean {
        val couponsList = fetchCoupons().toMutableList()
        couponsList.removeAll { it.code.trim().equals(code.trim(), ignoreCase = true) }
        val ja = org.json.JSONArray()
        couponsList.forEach { c ->
            val jo = org.json.JSONObject()
            jo.put("code", c.code)
            jo.put("discountType", c.discountType)
            jo.put("discountValue", c.discountValue)
            jo.put("expiryDate", c.expiryDate)
            jo.put("usageLimit", c.usageLimit)
            jo.put("usageCount", c.usageCount)
            ja.put(jo)
        }
        prefs.edit().putString("coupons_json", ja.toString()).apply()
        return true
    }

    // --- ADVERTISING PLATFORM METHODS ---
    suspend fun saveAdCampaign(campaign: com.example.data.model.AdCampaign): Boolean {
        val list = fetchAdCampaigns().toMutableList()
        val index = list.indexOfFirst { it.id == campaign.id }
        if (index != -1) {
            list[index] = campaign
        } else {
            list.add(campaign)
        }
        
        val ja = org.json.JSONArray()
        list.forEach { c ->
            val jo = org.json.JSONObject()
            jo.put("id", c.id)
            jo.put("userId", c.userId)
            jo.put("userEmail", c.userEmail)
            jo.put("title", c.title)
            jo.put("description", c.description)
            jo.put("bannerUrl", c.bannerUrl)
            jo.put("videoUrl", c.videoUrl)
            jo.put("destinationUrl", c.destinationUrl)
            jo.put("category", c.category)
            jo.put("adType", c.adType)
            jo.put("startDate", c.startDate)
            jo.put("endDate", c.endDate)
            jo.put("status", c.status)
            jo.put("budget", c.budget)
            jo.put("pricePaid", c.pricePaid)
            jo.put("planName", c.planName)
            jo.put("dateCreated", c.dateCreated)
            jo.put("isFeatured", c.isFeatured)
            jo.put("adminNotes", c.adminNotes)
            jo.put("viewsCount", c.viewsCount)
            jo.put("clicksCount", c.clicksCount)
            jo.put("paymentProofUrl", c.paymentProofUrl)
            jo.put("paymentReference", c.paymentReference)
            ja.put(jo)
        }
        prefs.edit().putString("ad_campaigns_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("ad_campaigns").document(campaign.id).set(campaign).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving campaign to Firestore", e)
            }
        }
        return true
    }

    suspend fun fetchAdCampaigns(): List<com.example.data.model.AdCampaign> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("ad_campaigns").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.AdCampaign::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { c ->
                        val jo = org.json.JSONObject()
                        jo.put("id", c.id)
                        jo.put("userId", c.userId)
                        jo.put("userEmail", c.userEmail)
                        jo.put("title", c.title)
                        jo.put("description", c.description)
                        jo.put("bannerUrl", c.bannerUrl)
                        jo.put("videoUrl", c.videoUrl)
                        jo.put("destinationUrl", c.destinationUrl)
                        jo.put("category", c.category)
                        jo.put("adType", c.adType)
                        jo.put("startDate", c.startDate)
                        jo.put("endDate", c.endDate)
                        jo.put("status", c.status)
                        jo.put("budget", c.budget)
                        jo.put("pricePaid", c.pricePaid)
                        jo.put("planName", c.planName)
                        jo.put("dateCreated", c.dateCreated)
                        jo.put("isFeatured", c.isFeatured)
                        jo.put("adminNotes", c.adminNotes)
                        jo.put("viewsCount", c.viewsCount)
                        jo.put("clicksCount", c.clicksCount)
                        jo.put("paymentProofUrl", c.paymentProofUrl)
                        jo.put("paymentReference", c.paymentReference)
                        ja.put(jo)
                    }
                    prefs.edit().putString("ad_campaigns_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching campaigns from Firestore", e)
            }
        }

        val str = prefs.getString("ad_campaigns_json", "") ?: ""
        if (str.isEmpty()) {
            return emptyList()
        }
        val list = mutableListOf<com.example.data.model.AdCampaign>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.AdCampaign(
                        id = jo.optString("id", ""),
                        userId = jo.optString("userId", ""),
                        userEmail = jo.optString("userEmail", ""),
                        title = jo.optString("title", ""),
                        description = jo.optString("description", ""),
                        bannerUrl = jo.optString("bannerUrl", ""),
                        videoUrl = jo.optString("videoUrl", ""),
                        destinationUrl = jo.optString("destinationUrl", ""),
                        category = jo.optString("category", ""),
                        adType = jo.optString("adType", ""),
                        startDate = jo.optLong("startDate", 0L),
                        endDate = jo.optLong("endDate", 0L),
                        status = jo.optString("status", "Awaiting Payment"),
                        budget = jo.optDouble("budget", 0.0),
                        pricePaid = jo.optDouble("pricePaid", 0.0),
                        planName = jo.optString("planName", ""),
                        dateCreated = jo.optLong("dateCreated", System.currentTimeMillis()),
                        isFeatured = jo.optBoolean("isFeatured", false),
                        adminNotes = jo.optString("adminNotes", ""),
                        viewsCount = jo.optInt("viewsCount", 0),
                        clicksCount = jo.optInt("clicksCount", 0),
                        paymentProofUrl = jo.optString("paymentProofUrl", ""),
                        paymentReference = jo.optString("paymentReference", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing campaigns", e)
        }
        return list
    }

    suspend fun saveAdPackage(pkg: com.example.data.model.AdPackage): Boolean {
        val list = fetchAdPackages().toMutableList()
        val index = list.indexOfFirst { it.id == pkg.id }
        if (index != -1) {
            list[index] = pkg
        } else {
            list.add(pkg)
        }

        val ja = org.json.JSONArray()
        list.forEach { p ->
            val jo = org.json.JSONObject()
            jo.put("id", p.id)
            jo.put("name", p.name)
            jo.put("price", p.price)
            jo.put("durationDays", p.durationDays)
            jo.put("description", p.description)
            jo.put("adType", p.adType)
            ja.put(jo)
        }
        prefs.edit().putString("ad_packages_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("ad_packages").document(pkg.id).set(pkg).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving ad package to Firestore", e)
            }
        }
        return true
    }

    suspend fun fetchAdPackages(): List<com.example.data.model.AdPackage> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("ad_packages").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.AdPackage::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { p ->
                        val jo = org.json.JSONObject()
                        jo.put("id", p.id)
                        jo.put("name", p.name)
                        jo.put("price", p.price)
                        jo.put("durationDays", p.durationDays)
                        jo.put("description", p.description)
                        jo.put("adType", p.adType)
                        ja.put(jo)
                    }
                    prefs.edit().putString("ad_packages_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching ad packages from Firestore", e)
            }
        }

        val str = prefs.getString("ad_packages_json", "") ?: ""
        if (str.isEmpty()) {
            val defaultPackages = listOf(
                com.example.data.model.AdPackage("basic", "Basic Side Banner", 5000.0, 7, "Perfect for startups. Sidebar Banner for 7 days.", "Sidebar Banner"),
                com.example.data.model.AdPackage("standard", "Standard In-App Card", 15000.0, 14, "Get higher engagement. In-App Promotional Card for 14 days.", "In-App Promotional Cards"),
                com.example.data.model.AdPackage("premium", "Premium Sponsored Product", 30000.0, 30, "Promote your brand at scale. Product Sponsored Ad for 30 days.", "Product Sponsored Ads"),
                com.example.data.model.AdPackage("homepage", "Featured Homepage Banner", 50000.0, 30, "Maximum visibility. Prominent Homepage Banner for 30 days.", "Homepage Banner")
            )
            return defaultPackages
        }

        val list = mutableListOf<com.example.data.model.AdPackage>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.AdPackage(
                        id = jo.optString("id", ""),
                        name = jo.optString("name", ""),
                        price = jo.optDouble("price", 0.0),
                        durationDays = jo.optInt("durationDays", 30),
                        description = jo.optString("description", ""),
                        adType = jo.optString("adType", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ad packages", e)
        }
        return list
    }

    suspend fun recordAdView(campaignId: String) {
        val campaigns = fetchAdCampaigns()
        val campaign = campaigns.firstOrNull { it.id == campaignId } ?: return
        val updated = campaign.copy(viewsCount = campaign.viewsCount + 1)
        saveAdCampaign(updated)
        
        // Record Daily Analytics
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        recordDailyAnalytics(campaignId, todayStr, viewsInc = 1, clicksInc = 0, revenueInc = 0.0)
    }

    suspend fun recordAdClick(campaignId: String) {
        val campaigns = fetchAdCampaigns()
        val campaign = campaigns.firstOrNull { it.id == campaignId } ?: return
        val updated = campaign.copy(clicksCount = campaign.clicksCount + 1)
        saveAdCampaign(updated)
        
        // Record Daily Analytics
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        recordDailyAnalytics(campaignId, todayStr, viewsInc = 0, clicksInc = 1, revenueInc = 0.0)
    }

    private suspend fun recordDailyAnalytics(campaignId: String, dateStr: String, viewsInc: Int, clicksInc: Int, revenueInc: Double) {
        val list = fetchAdAnalyticsDaily().toMutableList()
        val key = "${campaignId}_$dateStr"
        val existingIndex = list.indexOfFirst { it.id == key }
        val entry = if (existingIndex != -1) {
            val old = list[existingIndex]
            old.copy(
                views = old.views + viewsInc,
                clicks = old.clicks + clicksInc,
                revenue = old.revenue + revenueInc
            )
        } else {
            com.example.data.model.AdAnalyticsDaily(
                id = key,
                campaignId = campaignId,
                dateString = dateStr,
                views = viewsInc,
                clicks = clicksInc,
                revenue = revenueInc
            )
        }
        
        if (existingIndex != -1) list[existingIndex] = entry else list.add(entry)
        
        val ja = org.json.JSONArray()
        list.forEach { d ->
            val jo = org.json.JSONObject()
            jo.put("id", d.id)
            jo.put("campaignId", d.campaignId)
            jo.put("dateString", d.dateString)
            jo.put("views", d.views)
            jo.put("clicks", d.clicks)
            jo.put("revenue", d.revenue)
            ja.put(jo)
        }
        prefs.edit().putString("ad_daily_analytics_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("ad_daily_analytics").document(key).set(entry).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving ad analytics to Firestore", e)
            }
        }
    }

    suspend fun fetchAdAnalyticsDaily(): List<com.example.data.model.AdAnalyticsDaily> {
        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                val snapshot = firestore!!.collection("ad_daily_analytics").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(com.example.data.model.AdAnalyticsDaily::class.java) }
                if (list.isNotEmpty()) {
                    val ja = org.json.JSONArray()
                    list.forEach { d ->
                        val jo = org.json.JSONObject()
                        jo.put("id", d.id)
                        jo.put("campaignId", d.campaignId)
                        jo.put("dateString", d.dateString)
                        jo.put("views", d.views)
                        jo.put("clicks", d.clicks)
                        jo.put("revenue", d.revenue)
                        ja.put(jo)
                    }
                    prefs.edit().putString("ad_daily_analytics_json", ja.toString()).apply()
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching ad analytics from Firestore", e)
            }
        }

        val str = prefs.getString("ad_daily_analytics_json", "") ?: ""
        if (str.isEmpty()) return emptyList()
        val list = mutableListOf<com.example.data.model.AdAnalyticsDaily>()
        try {
            val ja = org.json.JSONArray(str)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                list.add(
                    com.example.data.model.AdAnalyticsDaily(
                        id = jo.optString("id", ""),
                        campaignId = jo.optString("campaignId", ""),
                        dateString = jo.optString("dateString", ""),
                        views = jo.optInt("views", 0),
                        clicks = jo.optInt("clicks", 0),
                        revenue = jo.optDouble("revenue", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ad daily analytics", e)
        }
        return list
    }

    suspend fun deleteAdCampaign(campaignId: String): Boolean {
        val list = fetchAdCampaigns().toMutableList()
        list.removeAll { it.id == campaignId }
        val ja = org.json.JSONArray()
        list.forEach { c ->
            val jo = org.json.JSONObject()
            jo.put("id", c.id)
            jo.put("userId", c.userId)
            jo.put("userEmail", c.userEmail)
            jo.put("title", c.title)
            jo.put("description", c.description)
            jo.put("bannerUrl", c.bannerUrl)
            jo.put("videoUrl", c.videoUrl)
            jo.put("destinationUrl", c.destinationUrl)
            jo.put("category", c.category)
            jo.put("adType", c.adType)
            jo.put("startDate", c.startDate)
            jo.put("endDate", c.endDate)
            jo.put("status", c.status)
            jo.put("budget", c.budget)
            jo.put("pricePaid", c.pricePaid)
            jo.put("planName", c.planName)
            jo.put("dateCreated", c.dateCreated)
            jo.put("isFeatured", c.isFeatured)
            jo.put("adminNotes", c.adminNotes)
            jo.put("viewsCount", c.viewsCount)
            jo.put("clicksCount", c.clicksCount)
            jo.put("paymentProofUrl", c.paymentProofUrl)
            jo.put("paymentReference", c.paymentReference)
            ja.put(jo)
        }
        prefs.edit().putString("ad_campaigns_json", ja.toString()).apply()

        if (_isFirebaseAvailable.value && firestore != null) {
            try {
                firestore!!.collection("ad_campaigns").document(campaignId).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting campaign from Firestore", e)
            }
        }
        return true
    }
}



