package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CareerAiScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OpportunitiesScreen
import com.example.ui.screens.OpportunityDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoadmapsScreen
import com.example.ui.screens.SkillDetailScreen
import com.example.ui.screens.SkillsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.WomensHubScreen
import com.example.ui.theme.PhanderLakeTurquoise
import com.example.ui.theme.PhanderTealContainer
import com.example.ui.theme.PhanderTealOnContainer
import com.example.ui.theme.PhanderTealPrimary
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.PhanderViewModel
import com.example.ui.viewmodel.SubScreen

@Composable
fun MainApp(viewModel: PhanderViewModel) {
    val destination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = destination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppDestinationTransition"
    ) { currentDest ->
        when (currentDest) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onGetStarted = { viewModel.onSplashGetStarted() }
                )
            }
            AppDestination.WELCOME -> {
                WelcomeScreen(
                    initialName = userProfile?.name ?: "Khalida",
                    onGetStarted = { name, interest ->
                        viewModel.onWelcomeGetStarted(name, interest)
                    }
                )
            }
            AppDestination.MAIN -> {
                MainHubContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainHubContainer(viewModel: PhanderViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentSubScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allSkills by viewModel.allSkills.collectAsStateWithLifecycle()
    val enrolledSkills by viewModel.enrolledSkills.collectAsStateWithLifecycle()
    val allOpportunities by viewModel.allOpportunities.collectAsStateWithLifecycle()
    val savedOpportunities by viewModel.savedOpportunities.collectAsStateWithLifecycle()
    val allRoadmaps by viewModel.allRoadmaps.collectAsStateWithLifecycle()
    val womenHubItems by viewModel.womenHubItems.collectAsStateWithLifecycle()

    val selectedSkillCategory by viewModel.selectedSkillCategory.collectAsStateWithLifecycle()
    val selectedOpportunityType by viewModel.selectedOpportunityType.collectAsStateWithLifecycle()
    val selectedWomenCategory by viewModel.selectedWomenHubCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val aiMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    // If subscreen is active, render subscreen
    when (val sub = currentSubScreen) {
        is SubScreen.SkillDetail -> {
            val skill = allSkills.find { it.id == sub.skillId }
            SkillDetailScreen(
                skill = skill,
                onBack = { viewModel.navigateBack() },
                onEnrollToggle = { viewModel.toggleSkillEnrollment(it) },
                onProgressUpdate = { s, p -> viewModel.updateSkillProgress(s, p) }
            )
        }
        is SubScreen.OpportunityDetail -> {
            val opp = allOpportunities.find { it.id == sub.opportunityId }
            OpportunityDetailScreen(
                opportunity = opp,
                onBack = { viewModel.navigateBack() },
                onToggleSave = { viewModel.toggleOpportunitySaved(it) }
            )
        }
        is SubScreen.CareerAi -> {
            CareerAiScreen(
                messages = aiMessages,
                isThinking = isAiThinking,
                onSendMessage = { viewModel.askCareerAi(it) },
                onBack = { viewModel.navigateBack() }
            )
        }
        is SubScreen.Roadmaps -> {
            RoadmapsScreen(
                roadmaps = allRoadmaps,
                onBack = { viewModel.navigateBack() },
                onToggleStep = { r, s -> viewModel.toggleRoadmapStep(r, s) }
            )
        }
        is SubScreen.WomensHub -> {
            WomensHubScreen(
                items = womenHubItems,
                selectedCategory = selectedWomenCategory,
                onCategorySelected = { viewModel.setWomenHubCategory(it) },
                onToggleFavorite = { viewModel.toggleWomenHubFavorite(it) },
                onAddItem = { title, creator, village, cat, price, desc, contact ->
                    viewModel.addWomenHubItem(title, creator, village, cat, price, desc, contact)
                },
                onDeleteItem = { viewModel.deleteWomenHubItem(it) },
                onBack = { viewModel.navigateBack() }
            )
        }
        else -> {
            // Main Bottom Tab Scaffold
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("main_bottom_nav")
                    ) {
                        // 🏠 Home
                        NavigationBarItem(
                            selected = selectedTab == MainTab.HOME,
                            onClick = { viewModel.selectTab(MainTab.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PhanderTealContainer,
                                selectedIconColor = PhanderTealOnContainer
                            ),
                            modifier = Modifier.testTag("tab_home")
                        )

                        // 📚 Skills
                        NavigationBarItem(
                            selected = selectedTab == MainTab.SKILLS,
                            onClick = { viewModel.selectTab(MainTab.SKILLS) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.SKILLS) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = "Skills"
                                )
                            },
                            label = { Text("Skills", fontWeight = if (selectedTab == MainTab.SKILLS) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PhanderTealContainer,
                                selectedIconColor = PhanderTealOnContainer
                            ),
                            modifier = Modifier.testTag("tab_skills")
                        )

                        // 🎓 Jobs / Scholarships
                        NavigationBarItem(
                            selected = selectedTab == MainTab.OPPORTUNITIES,
                            onClick = { viewModel.selectTab(MainTab.OPPORTUNITIES) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.OPPORTUNITIES) Icons.Filled.School else Icons.Outlined.School,
                                    contentDescription = "Jobs & Scholarships"
                                )
                            },
                            label = { Text("Jobs", fontWeight = if (selectedTab == MainTab.OPPORTUNITIES) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PhanderTealContainer,
                                selectedIconColor = PhanderTealOnContainer
                            ),
                            modifier = Modifier.testTag("tab_opportunities")
                        )

                        // 👤 Me / Profile
                        NavigationBarItem(
                            selected = selectedTab == MainTab.PROFILE,
                            onClick = { viewModel.selectTab(MainTab.PROFILE) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Me"
                                )
                            },
                            label = { Text("Me", fontWeight = if (selectedTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PhanderTealContainer,
                                selectedIconColor = PhanderTealOnContainer
                            ),
                            modifier = Modifier.testTag("tab_profile")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                userProfile = userProfile,
                                enrolledSkills = enrolledSkills,
                                featuredOpportunities = allOpportunities,
                                onOpenSkills = { viewModel.selectTab(MainTab.SKILLS) },
                                onOpenScholarships = {
                                    viewModel.setOpportunityType("Scholarship")
                                    viewModel.selectTab(MainTab.OPPORTUNITIES)
                                },
                                onOpenCareerAi = { viewModel.navigateToSubScreen(SubScreen.CareerAi) },
                                onOpenRoadmaps = { viewModel.navigateToSubScreen(SubScreen.Roadmaps) },
                                onOpenWomensHub = { viewModel.navigateToSubScreen(SubScreen.WomensHub) },
                                onOpenSkillDetail = { id -> viewModel.navigateToSubScreen(SubScreen.SkillDetail(id)) },
                                onOpenOpportunityDetail = { id -> viewModel.navigateToSubScreen(SubScreen.OpportunityDetail(id)) }
                            )
                        }
                        MainTab.SKILLS -> {
                            SkillsScreen(
                                skills = allSkills,
                                selectedCategory = selectedSkillCategory,
                                searchQuery = searchQuery,
                                onCategorySelected = { viewModel.setSkillCategory(it) },
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onSkillClick = { id -> viewModel.navigateToSubScreen(SubScreen.SkillDetail(id)) },
                                onEnrollToggle = { viewModel.toggleSkillEnrollment(it) }
                            )
                        }
                        MainTab.OPPORTUNITIES -> {
                            OpportunitiesScreen(
                                opportunities = allOpportunities,
                                selectedType = selectedOpportunityType,
                                searchQuery = searchQuery,
                                onTypeSelected = { viewModel.setOpportunityType(it) },
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onOpportunityClick = { id -> viewModel.navigateToSubScreen(SubScreen.OpportunityDetail(id)) },
                                onToggleSave = { viewModel.toggleOpportunitySaved(it) }
                            )
                        }
                        MainTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                enrolledSkills = enrolledSkills,
                                savedOpportunities = savedOpportunities,
                                womenHubItems = womenHubItems,
                                onSkillClick = { id -> viewModel.navigateToSubScreen(SubScreen.SkillDetail(id)) },
                                onOpportunityClick = { id -> viewModel.navigateToSubScreen(SubScreen.OpportunityDetail(id)) },
                                onUpdateProfile = { name, title, loc, bio ->
                                    viewModel.updateProfile(name, title, loc, bio)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
