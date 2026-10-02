package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AiConsultation
import com.example.data.local.OpportunityItem
import com.example.data.local.PhanderDao
import com.example.data.local.PhanderDatabase
import com.example.data.local.RoadmapTrack
import com.example.data.local.SkillItem
import com.example.data.local.UserProfile
import com.example.data.local.WomenHubItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PhanderRepository(private val dao: PhanderDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allSkills: Flow<List<SkillItem>> = dao.getAllSkills()
    val enrolledSkills: Flow<List<SkillItem>> = dao.getEnrolledSkills()
    val allOpportunities: Flow<List<OpportunityItem>> = dao.getAllOpportunities()
    val savedOpportunities: Flow<List<OpportunityItem>> = dao.getSavedOpportunities()
    val allRoadmaps: Flow<List<RoadmapTrack>> = dao.getAllRoadmaps()
    val womenHubItems: Flow<List<WomenHubItem>> = dao.getAllWomenHubItems()
    val aiConsultations: Flow<List<AiConsultation>> = dao.getAiConsultations()

    fun getSkillById(id: String): Flow<SkillItem?> = dao.getSkillById(id)
    fun getOpportunityById(id: String): Flow<OpportunityItem?> = dao.getOpportunityById(id)

    suspend fun updateProfile(profile: UserProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun toggleSkillEnrollment(skill: SkillItem) {
        val updated = skill.copy(
            isEnrolled = !skill.isEnrolled,
            progress = if (!skill.isEnrolled) 15 else 0
        )
        dao.updateSkill(updated)
    }

    suspend fun updateSkillProgress(skill: SkillItem, newProgress: Int) {
        val updated = skill.copy(
            progress = newProgress.coerceIn(0, 100),
            isEnrolled = true
        )
        dao.updateSkill(updated)
    }

    suspend fun toggleOpportunitySaved(opportunity: OpportunityItem) {
        val updated = opportunity.copy(isSaved = !opportunity.isSaved)
        dao.updateOpportunity(updated)
    }

    suspend fun toggleRoadmapStep(roadmap: RoadmapTrack, stepIndex: Int) {
        val steps = roadmap.stepsRaw.split(";").filter { it.isNotBlank() }.toMutableList()
        if (stepIndex in steps.indices) {
            val parts = steps[stepIndex].split("|")
            if (parts.size >= 3) {
                val isDone = parts[2].toBoolean()
                val newStatus = (!isDone).toString()
                steps[stepIndex] = "${parts[0]}|${parts[1]}|$newStatus"
            }
        }
        val completed = steps.count { it.endsWith("|true") }
        val updated = roadmap.copy(
            stepsRaw = steps.joinToString(";"),
            completedSteps = completed
        )
        dao.updateRoadmap(updated)
    }

    suspend fun addWomenHubItem(item: WomenHubItem) {
        dao.insertWomenHubItem(item)
    }

    suspend fun toggleWomenHubFavorite(item: WomenHubItem) {
        val updated = item.copy(
            isFavorite = !item.isFavorite,
            likesCount = if (!item.isFavorite) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0)
        )
        dao.updateWomenHubItem(updated)
    }

    suspend fun deleteWomenHubItem(id: String) {
        dao.deleteWomenHubItem(id)
    }

    suspend fun recordAiConsultation(query: String, reply: String, domain: String) {
        dao.insertAiConsultation(
            AiConsultation(
                query = query,
                reply = reply,
                domain = domain
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingProfile = userProfile.firstOrNull()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    name = "Khalida",
                    email = "bkhalida275@gmail.com",
                    title = "Aspiring Web Developer & Artisan",
                    location = "Phander Valley, Ghizer",
                    bio = "Empowering mountain youth and women through digital freelancing, coding, and remote career opportunities.",
                    completedCoursesCount = 1,
                    savedOpportunitiesCount = 2,
                    isOnboarded = false
                )
            )
        }

        val skills = dao.getAllSkills().firstOrNull()
        if (skills.isNullOrEmpty()) {
            dao.insertSkills(defaultSkills)
        }

        val opps = dao.getAllOpportunities().firstOrNull()
        if (opps.isNullOrEmpty()) {
            dao.insertOpportunities(defaultOpportunities)
        }

        val roadmaps = dao.getAllRoadmaps().firstOrNull()
        if (roadmaps.isNullOrEmpty()) {
            dao.insertRoadmaps(defaultRoadmaps)
        }

        val hubItems = dao.getAllWomenHubItems().firstOrNull()
        if (hubItems.isNullOrEmpty()) {
            dao.insertWomenHubItems(defaultWomenHubItems)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PhanderRepository? = null

        fun getInstance(context: Context): PhanderRepository {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    PhanderDatabase::class.java,
                    "phander_hub.db"
                ).build()
                val repo = PhanderRepository(db.phanderDao())
                INSTANCE = repo
                // Seed asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    repo.seedInitialDataIfEmpty()
                }
                repo
            }
        }

        val defaultSkills = listOf(
            SkillItem(
                id = "skill_web_dev",
                title = "Frontend Web Development",
                category = "Tech & Coding",
                level = "Beginner to Pro",
                duration = "6 Weeks",
                lessonsCount = 24,
                instructor = "Engr. Salman Ali & Team GB",
                description = "Learn HTML5, CSS3, modern JavaScript, and responsive design tailored for remote work with offline-first caching for mountainous regions.",
                isEnrolled = true,
                progress = 65,
                isFeatured = true,
                rating = 4.9,
                keySkills = "HTML5, CSS Flexbox & Grid, JavaScript, React Basics, Git",
                syllabus = "Web Foundations & Semantic HTML;CSS Styling & Responsive Design;Modern JavaScript (ES6+);Building a Portfolio Project;Low-Bandwidth Optimization;Freelance Marketplace Preparation"
            ),
            SkillItem(
                id = "skill_freelancing",
                title = "Freelance Mastery & Remote Work",
                category = "Freelancing",
                level = "All Levels",
                duration = "4 Weeks",
                lessonsCount = 16,
                instructor = "Farhana Baig (Top Rated Freelancer)",
                description = "Master international platforms like Upwork and Fiverr. Learn client communication, proposal writing, setting up Payoneer, and time zone management.",
                isEnrolled = true,
                progress = 40,
                isFeatured = true,
                rating = 4.95,
                keySkills = "Proposal Writing, Client Pitching, Upwork SEO, Payoneer, Pricing Strategy",
                syllabus = "Crafting a Winning Profile;Proposal Writing Masterclass;Pricing and Contracts;Client Communication & Remote Ethics;Receiving Global Payments in Pakistan"
            ),
            SkillItem(
                id = "skill_ui_ux",
                title = "UI/UX Design with Figma",
                category = "Design",
                level = "Intermediate",
                duration = "5 Weeks",
                lessonsCount = 20,
                instructor = "Tariq Shah (Lead Product Designer)",
                description = "Design modern mobile and web interfaces. Understand wireframing, color theory, typography, components, and auto-layout in Figma.",
                isEnrolled = false,
                progress = 0,
                isFeatured = false,
                rating = 4.85,
                keySkills = "Figma, Wireframing, User Research, Auto-Layout, Design Systems",
                syllabus = "Design Thinking Principles;Figma Tools & Layout Grids;Interactive Prototyping;Creating Design Systems;Exporting Assets for Developers"
            ),
            SkillItem(
                id = "skill_digital_marketing",
                title = "Digital Marketing & Local E-Commerce",
                category = "Business & Sales",
                level = "Beginner",
                duration = "3 Weeks",
                lessonsCount = 12,
                instructor = "Bibi Maryam (E-Commerce Specialist)",
                description = "Learn how to market Phander valley handicrafts, trout fishery, organic dry fruits, and tourism services online using social media ads and SEO.",
                isEnrolled = false,
                progress = 0,
                isFeatured = true,
                rating = 4.8,
                keySkills = "Social Media Ads, SEO, Product Photography, Content Strategy",
                syllabus = "Social Media Marketing Foundations;Visual Content on a Budget;Instagram & WhatsApp Commerce;Targeted Ads for Tourism & Crafts;Customer Service & Shipping Logistics"
            ),
            SkillItem(
                id = "skill_python_data",
                title = "Python for Data & Automation",
                category = "Tech & Coding",
                level = "Beginner",
                duration = "6 Weeks",
                lessonsCount = 22,
                instructor = "Dr. Asim Khan",
                description = "Get started with programming logic, automating repetitive spreadsheet tasks, web scraping, and analyzing datasets with Python.",
                isEnrolled = false,
                progress = 0,
                isFeatured = false,
                rating = 4.75,
                keySkills = "Python Syntax, Data Automation, Pandas, Web Scraping",
                syllabus = "Python Setup & Syntax;Working with Files & Spreadsheets;Automation Scripts;Data Analysis with Pandas;Portfolio Capstone Project"
            ),
            SkillItem(
                id = "skill_artisan_catalog",
                title = "Artisan Digital Cataloging & Sales",
                category = "Crafts & Enterprise",
                level = "All Levels",
                duration = "2 Weeks",
                lessonsCount = 8,
                instructor = "Khalida & GB Crafts Council",
                description = "Step-by-step guidance for artisans to photograph handmade shawls, caps, and gemstone jewelry, set fair prices, and sell to international buyers.",
                isEnrolled = false,
                progress = 0,
                isFeatured = false,
                rating = 4.9,
                keySkills = "Mobile Photography, Storytelling, Pricing, Packaging, Courier Shipping",
                syllabus = "Showcasing Mountain Heritage;Capturing High-Quality Mobile Photos;Story-Driven Product Descriptions;Packaging & Safe Delivery;Connecting with Global Buyers"
            )
        )

        val defaultOpportunities = listOf(
            OpportunityItem(
                id = "opp_gb_digital_scholarship",
                title = "Gilgit-Baltistan Digital Youth Scholarship",
                organization = "GB IT Board & Tech Partners",
                type = "Scholarship",
                deadline = "October 25, 2026",
                coverageOrStipend = "Fully Funded + Laptop & Internet Subsidy",
                eligibility = "Youth aged 18-32 residing in Gilgit-Baltistan with passion for digital skills",
                location = "Phander, Ghizer & Northern Valleys",
                description = "Provides complete tuition fee coverage for 6-month specialized web development and remote freelancing tracks, plus high-speed 4G device subsidy.",
                isSaved = true
            ),
            OpportunityItem(
                id = "opp_women_tech_fellowship",
                title = "Northern Women Tech Fellowship",
                organization = "Mountain Empowerment Initiative",
                type = "Fellowship",
                deadline = "November 10, 2026",
                coverageOrStipend = "Rs. 25,000 / month stipend + Mentorship",
                eligibility = "Female students, homemakers, and career switchers in rural districts",
                location = "Hybrid / Remote (Phander & Ghizer)",
                description = "A safe, supportive cohort for women learning digital commerce, UI design, and freelance content creation with 1-on-1 female tech mentors.",
                isSaved = true
            ),
            OpportunityItem(
                id = "opp_remote_frontend_intern",
                title = "Remote Junior Frontend Developer",
                organization = "CloudPeak Technologies",
                type = "Remote Job",
                deadline = "Rolling Applications",
                coverageOrStipend = "Rs. 75,000 - 110,000 / month",
                eligibility = "Proficiency in HTML/CSS, basic React or Vue, responsive web design",
                location = "100% Remote (Flexible Hours)",
                description = "Work on global client dashboards, landing pages, and web apps. Async communication model ideal for areas with occasional power or connectivity dips.",
                isSaved = false
            ),
            OpportunityItem(
                id = "opp_aga_khan_foundation_grant",
                title = "Rural Enterprise & Artisans Grant",
                organization = "Aga Khan Rural Support Programme (AKRSP)",
                type = "Grant",
                deadline = "December 5, 2026",
                coverageOrStipend = "Up to Rs. 350,000 Seed Capital",
                eligibility = "Individual women entrepreneurs or youth groups starting digital or handicraft ventures",
                location = "Ghizer District (Phander, Gupis, Yasin)",
                description = "Non-repayable seed grant for procuring workshop materials, solar power backup, and digital equipment to scale local production.",
                isSaved = false
            ),
            OpportunityItem(
                id = "opp_digiskills_global_freelance",
                title = "DigiSkills Mountain Cohort 2026",
                organization = "Ignite National Technology Fund",
                type = "Scholarship",
                deadline = "November 20, 2026",
                coverageOrStipend = "100% Free Certified Training",
                eligibility = "All education levels from Matriculation onwards",
                location = "Online / Self-Paced with Local Support Hub",
                description = "Nationally recognized government certifications in freelancing, WordPress, SEO, and digital graphic design with Phander community study circles.",
                isSaved = true
            )
        )

        val defaultRoadmaps = listOf(
            RoadmapTrack(
                id = "roadmap_web_dev",
                title = "Frontend Web Developer",
                iconName = "Code",
                description = "Zero to job-ready frontend engineer building responsive, accessible web applications.",
                estimatedDuration = "5 - 6 Months",
                totalSteps = 5,
                completedSteps = 2,
                stepsRaw = "1. Internet & HTML5 Fundamentals|Learn tags, DOM, semantic structures, forms and SEO basics.|true;2. Responsive CSS & Flexbox/Grid|Style layouts, media queries, mobile-first design and Tailwind CSS.|true;3. Modern JavaScript & APIs|Variables, async/await, fetching JSON data, and local storage.|false;4. Component Frameworks (React/Vue)|State management, hooks, reusable components and routing.|false;5. Portfolio & Remote Job Hunt|Build 3 polished live projects, deploy on Vercel, and apply to remote roles.|false"
            ),
            RoadmapTrack(
                id = "roadmap_freelancer",
                title = "Global Freelancer & Upwork Pro",
                iconName = "Work",
                description = "Master client acquisition, high-converting proposals, and financial independence.",
                estimatedDuration = "2 - 3 Months",
                totalSteps = 4,
                completedSteps = 1,
                stepsRaw = "1. Niche & Service Definition|Identify your marketable core skill (design, coding, writing) and target audience.|true;2. Profile Optimization & Portfolio|Write compelling bio, case studies, video intro and verified badges.|false;3. Proposal Mastery & Bidding|Write personalized proposals within 15 minutes of job posting; avoid generic templates.|false;4. Scaling & Long-Term Contracts|Convert one-off clients into retainers, collect 5-star reviews, and raise hourly rates.|false"
            ),
            RoadmapTrack(
                id = "roadmap_ui_ux",
                title = "UI/UX Product Designer",
                iconName = "Palette",
                description = "Create intuitive digital experiences and user-centered design systems.",
                estimatedDuration = "4 Months",
                totalSteps = 4,
                completedSteps = 0,
                stepsRaw = "1. User Research & Wireframes|Empathy mapping, user interviews, information architecture, and paper sketches.|false;2. Figma Mastery & Auto-Layout|Master components, variants, variables, tokens and responsive auto-layout.|false;3. Interactive Prototyping & Micro-interactions|Design realistic click-through flows and animated micro-interactions.|false;4. Product Case Study Presentation|Write detailed Behance/Dribbble case studies showing problems solved and impact.|false"
            ),
            RoadmapTrack(
                id = "roadmap_digital_artisan",
                title = "Digital Artisan & E-Commerce Creator",
                iconName = "Store",
                description = "Preserve mountain craft heritage while selling to national and international markets.",
                estimatedDuration = "2 Months",
                totalSteps = 4,
                completedSteps = 2,
                stepsRaw = "1. Product Standardization & Costing|Calculate yarn/material costs, hours worked, and fair profit margins.|true;2. Mobile Product Photography|Shoot clean daylight photos against Phander natural backgrounds.|true;3. Online Catalog & Social Showcase|Publish on Phander Women's Hub, Instagram Shop, and WhatsApp Business catalog.|false;4. Safe Packaging & Nationwide Courier|Partner with local courier pick-up services and accept digital payments (JazzCash/Nayapay).|false"
            )
        )

        val defaultWomenHubItems = listOf(
            WomenHubItem(
                id = "hub_item_1",
                title = "Pure Hand-Embroidered Phander Shawl",
                creatorName = "Bibi Amina",
                creatorVillage = "Phander Main Valley",
                category = "Handcrafted Textile",
                priceOrRate = "Rs. 6,500",
                description = "Finely spun local sheep wool hand-embroidered with classic Ghizer floral and geometric border patterns. Warm, lightweight, and authentic heritage craft.",
                contactInfo = "WhatsApp: +92 345 9821430",
                likesCount = 38,
                isFavorite = true,
                isUserCreated = false
            ),
            WomenHubItem(
                id = "hub_item_2",
                title = "Custom Responsive Website Development",
                creatorName = "Khalida & Tech Circle",
                creatorVillage = "Phander Koh",
                category = "Digital Service",
                priceOrRate = "Rs. 18,000 / project",
                description = "Clean modern landing pages, tourism booking sites, or portfolio websites built with mobile-first responsiveness and high performance.",
                contactInfo = "Email: bkhalida275@gmail.com",
                likesCount = 52,
                isFavorite = true,
                isUserCreated = false
            ),
            WomenHubItem(
                id = "hub_item_3",
                title = "Raw Wild Mountain Honey (Phander Valley)",
                creatorName = "Fatima Gul",
                creatorVillage = "Chashi Valley",
                category = "Local Organic",
                priceOrRate = "Rs. 2,200 / 500g jar",
                description = "100% natural raw blossom honey collected from high-altitude alpine wildflowers in Phander and Chashi valleys. Unfiltered and nutrient-rich.",
                contactInfo = "WhatsApp: +92 346 5129984",
                likesCount = 27,
                isFavorite = false,
                isUserCreated = false
            ),
            WomenHubItem(
                id = "hub_item_4",
                title = "Traditional Gilgit Hand-Knitted Cap & Feather Plume",
                creatorName = "Saira Bano",
                creatorVillage = "Shamran Village",
                category = "Handcrafted Textile",
                priceOrRate = "Rs. 2,800",
                description = "Handmade woolen Gilgiti cap decorated with traditional beadwork and custom feather brooch. Made with love by village artisans.",
                contactInfo = "WhatsApp: +92 348 7741209",
                likesCount = 44,
                isFavorite = false,
                isUserCreated = false
            ),
            WomenHubItem(
                id = "hub_item_5",
                title = "Social Media Branding & Logo Design Pack",
                creatorName = "Zubaida Begum",
                creatorVillage = "Gupis",
                category = "Digital Service",
                priceOrRate = "Rs. 4,500 / pack",
                description = "Includes customized Instagram post templates, story highlights, business card design, and Facebook banner tailored for mountain businesses.",
                contactInfo = "WhatsApp: +92 341 6398210",
                likesCount = 19,
                isFavorite = false,
                isUserCreated = false
            )
        )
    }
}
