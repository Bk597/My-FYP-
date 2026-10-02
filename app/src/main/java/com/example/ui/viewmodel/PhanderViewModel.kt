package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AiConsultation
import com.example.data.local.OpportunityItem
import com.example.data.local.RoadmapTrack
import com.example.data.local.SkillItem
import com.example.data.local.UserProfile
import com.example.data.local.WomenHubItem
import com.example.data.repository.PhanderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppDestination {
    SPLASH,
    WELCOME,
    MAIN
}

enum class MainTab {
    HOME,
    SKILLS,
    OPPORTUNITIES,
    PROFILE
}

sealed class SubScreen {
    object None : SubScreen()
    data class SkillDetail(val skillId: String) : SubScreen()
    data class OpportunityDetail(val opportunityId: String) : SubScreen()
    object CareerAi : SubScreen()
    object Roadmaps : SubScreen()
    object WomensHub : SubScreen()
    object EditProfile : SubScreen()
}

data class CareerAiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PhanderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhanderRepository.getInstance(application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSkills: StateFlow<List<SkillItem>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrolledSkills: StateFlow<List<SkillItem>> = repository.enrolledSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOpportunities: StateFlow<List<OpportunityItem>> = repository.allOpportunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedOpportunities: StateFlow<List<OpportunityItem>> = repository.savedOpportunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoadmaps: StateFlow<List<RoadmapTrack>> = repository.allRoadmaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val womenHubItems: StateFlow<List<WomenHubItem>> = repository.womenHubItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiConsultations: StateFlow<List<AiConsultation>> = repository.aiConsultations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.SPLASH)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _subScreenStack = MutableStateFlow<List<SubScreen>>(emptyList())
    private val _currentSubScreen = MutableStateFlow<SubScreen>(SubScreen.None)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    // Career AI interactive chat session
    private val _aiChatMessages = MutableStateFlow(
        listOf(
            CareerAiMessage(
                sender = "ai",
                text = "Salam Khalida! I am your Phander Career & Skills AI Advisor. Whether you want to break into remote freelancing from Ghizer, find fully funded tech scholarships, or price your digital services, I am here to guide you step-by-step. What goal would you like to work on today?"
            )
        )
    )
    val aiChatMessages: StateFlow<List<CareerAiMessage>> = _aiChatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Filtering states
    private val _selectedSkillCategory = MutableStateFlow("All")
    val selectedSkillCategory: StateFlow<String> = _selectedSkillCategory.asStateFlow()

    private val _selectedOpportunityType = MutableStateFlow("All")
    val selectedOpportunityType: StateFlow<String> = _selectedOpportunityType.asStateFlow()

    private val _selectedWomenHubCategory = MutableStateFlow("All")
    val selectedWomenHubCategory: StateFlow<String> = _selectedWomenHubCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSplashGetStarted() {
        _currentDestination.value = AppDestination.WELCOME
    }

    fun onWelcomeGetStarted(name: String, interest: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(
                current.copy(
                    name = if (name.isNotBlank()) name else "Khalida",
                    isOnboarded = true,
                    title = if (interest.isNotBlank()) "$interest Specialist" else current.title
                )
            )
            _currentDestination.value = AppDestination.MAIN
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        _subScreenStack.value = emptyList() // clear any subscreen when switching root tab
        _currentSubScreen.value = SubScreen.None
    }

    fun navigateToSubScreen(subScreen: SubScreen) {
        _subScreenStack.value = _subScreenStack.value + subScreen
        _currentSubScreen.value = subScreen
    }

    fun navigateBack(): Boolean {
        if (_subScreenStack.value.isNotEmpty()) {
            val newStack = _subScreenStack.value.dropLast(1)
            _subScreenStack.value = newStack
            _currentSubScreen.value = newStack.lastOrNull() ?: SubScreen.None
            return true
        }
        return false
    }

    fun setSkillCategory(category: String) {
        _selectedSkillCategory.value = category
    }

    fun setOpportunityType(type: String) {
        _selectedOpportunityType.value = type
    }

    fun setWomenHubCategory(category: String) {
        _selectedWomenHubCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSkillEnrollment(skill: SkillItem) {
        viewModelScope.launch {
            repository.toggleSkillEnrollment(skill)
        }
    }

    fun updateSkillProgress(skill: SkillItem, progress: Int) {
        viewModelScope.launch {
            repository.updateSkillProgress(skill, progress)
        }
    }

    fun toggleOpportunitySaved(opportunity: OpportunityItem) {
        viewModelScope.launch {
            repository.toggleOpportunitySaved(opportunity)
        }
    }

    fun toggleRoadmapStep(roadmap: RoadmapTrack, stepIndex: Int) {
        viewModelScope.launch {
            repository.toggleRoadmapStep(roadmap, stepIndex)
        }
    }

    fun toggleWomenHubFavorite(item: WomenHubItem) {
        viewModelScope.launch {
            repository.toggleWomenHubFavorite(item)
        }
    }

    fun addWomenHubItem(
        title: String,
        creatorName: String,
        creatorVillage: String,
        category: String,
        priceOrRate: String,
        description: String,
        contactInfo: String
    ) {
        viewModelScope.launch {
            val newItem = WomenHubItem(
                id = "item_${System.currentTimeMillis()}",
                title = title.trim(),
                creatorName = creatorName.trim().ifEmpty { "Khalida" },
                creatorVillage = creatorVillage.trim().ifEmpty { "Phander Valley" },
                category = category,
                priceOrRate = priceOrRate.trim(),
                description = description.trim(),
                contactInfo = contactInfo.trim().ifEmpty { "Contact via Phander Hub" },
                likesCount = 1,
                isFavorite = true,
                isUserCreated = true
            )
            repository.addWomenHubItem(newItem)
        }
    }

    fun deleteWomenHubItem(id: String) {
        viewModelScope.launch {
            repository.deleteWomenHubItem(id)
        }
    }

    fun updateProfile(name: String, title: String, location: String, bio: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(
                current.copy(
                    name = name.trim(),
                    title = title.trim(),
                    location = location.trim(),
                    bio = bio.trim()
                )
            )
            navigateBack()
        }
    }

    fun askCareerAi(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        val userMsg = CareerAiMessage(sender = "user", text = trimmed)
        _aiChatMessages.value = _aiChatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(600) // realistic smooth interaction
            val responseText = generateSmartAdvice(trimmed)
            val aiMsg = CareerAiMessage(sender = "ai", text = responseText)
            _aiChatMessages.value = _aiChatMessages.value + aiMsg
            _isAiThinking.value = false

            repository.recordAiConsultation(trimmed, responseText, "Career Guidance")
        }
    }

    private fun generateSmartAdvice(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("freelanc") || q.contains("upwork") || q.contains("fiverr") -> {
                "🌟 Great question about freelancing from Phander!\n\n" +
                "1. **Start with a Hyper-Focused Niche**: Rather than 'general web dev', offer 'Responsive Landing Pages for Tourism & Local Businesses' or 'Shopify Store setup'.\n" +
                "2. **Handle Remote Mountain Internet**: Always download course materials and repositories locally. Use offline Git workflows, and configure async communication with clients so occasional weather-related outages won't hurt your ratings.\n" +
                "3. **Payment Setup**: Link your Payoneer or Nayapay/SadaPay to receive USD directly into local banks with lowest withdrawal fees.\n" +
                "4. **Winning Proposals**: Quote the client's problem in the very first sentence. Example: 'I noticed your website takes 4 seconds to load; here is how I will compress your assets...'"
            }
            q.contains("scholarship") || q.contains("fund") || q.contains("grant") -> {
                "🎓 Here are the top funding tracks for mountain youth in Gilgit-Baltistan:\n\n" +
                "• **GB Digital Youth Scholarship**: Full funding + laptop subsidy. Applications open this month! Check the Opportunities tab.\n" +
                "• **Northern Women Tech Fellowship**: Offers a Rs. 25,000/mo stipend for women learning coding and design.\n" +
                "• **AKRSP Innovation Seed Grant**: Up to Rs. 350,000 for women-led digital ventures and handicraft enterprises.\n\n" +
                "💡 Pro-Tip: When applying, emphasize your community impact—explain how you will use digital skills to train more girls in your village!"
            }
            q.contains("roadmap") || q.contains("learn") || q.contains("start") || q.contains("coding") || q.contains("web") -> {
                "🚀 Here is your recommended 3-Phase Roadmap:\n\n" +
                "• **Phase 1 (Month 1-2)**: HTML5 semantic tags, CSS Flexbox & CSS Grid, and Git version control.\n" +
                "• **Phase 2 (Month 3-4)**: JavaScript fundamentals (DOM, Fetch API, array methods) + Tailwind CSS.\n" +
                "• **Phase 3 (Month 5)**: React or Vue component development + deploy 3 live projects showcasing Phander tourism and local crafts.\n\n" +
                "Check out the **Roadmaps** section in the app where you can tick off each step as you complete it!"
            }
            q.contains("women") || q.contains("sell") || q.contains("craft") || q.contains("showcase") -> {
                "👩 The Phander Women's Hub is built specifically for you!\n\n" +
                "1. **Showcase Your Craft**: Tap the '+ Showcase Your Skill' button in the Women's Hub to list your handwoven shawls, Gilgiti caps, or digital services.\n" +
                "2. **Fair Pricing Formula**: (Material Cost + Hours Spent × Rs. 350) + 20% reinvestment buffer.\n" +
                "3. **Direct Contact**: Your listing features instant WhatsApp and email contact so buyers and collaborators reach you directly without middleman cuts."
            }
            else -> {
                "✨ Here is actionable advice for your journey in Phander Digital Hub:\n\n" +
                "• **Skill Priority**: Focus on Web Development, Digital Content, or E-Commerce—these offer the highest remote earning potential from anywhere in GB.\n" +
                "• **Portfolio First**: Clients and scholarship committees value verifiable proof of work over theoretical degrees. Build real projects for local schools, hotels, or artisans.\n" +
                "• **Community Power**: Study with fellow peers in Phander, Gupis, and Ghizer. Shared learning speeds up progress by 3x!\n\n" +
                "Feel free to ask specifically about: 'How to get my first Upwork client', 'Best scholarships for women', or 'Pricing mountain handicrafts'!"
            }
        }
    }
}
