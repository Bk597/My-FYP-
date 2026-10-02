package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Khalida",
    val email: String = "bkhalida275@gmail.com",
    val title: String = "Aspiring Web Developer & Artisan",
    val location: String = "Phander Valley, Ghizer",
    val bio: String = "Passionate learner exploring remote tech opportunities, modern frontend development, and digital marketing for mountain communities.",
    val completedCoursesCount: Int = 2,
    val savedOpportunitiesCount: Int = 3,
    val isOnboarded: Boolean = false
)

@Entity(tableName = "skills")
data class SkillItem(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val level: String,
    val duration: String,
    val lessonsCount: Int,
    val instructor: String,
    val description: String,
    val isEnrolled: Boolean = false,
    val progress: Int = 0,
    val isFeatured: Boolean = false,
    val rating: Double = 4.8,
    val keySkills: String = "HTML, CSS, JavaScript, Freelancing",
    val syllabus: String = "Getting Started;Core Concepts;Hands-on Project;Freelance Marketplace Setup"
)

@Entity(tableName = "opportunities")
data class OpportunityItem(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val type: String, // Scholarship, Remote Job, Fellowship, Grant
    val deadline: String,
    val coverageOrStipend: String,
    val eligibility: String,
    val location: String,
    val description: String,
    val isSaved: Boolean = false,
    val applyUrl: String = "https://phanderhub.org/apply"
)

@Entity(tableName = "roadmaps")
data class RoadmapTrack(
    @PrimaryKey val id: String,
    val title: String,
    val iconName: String,
    val description: String,
    val estimatedDuration: String,
    val totalSteps: Int,
    val completedSteps: Int = 0,
    val stepsRaw: String // serialized steps: "Step 1: Description:isDone;Step 2:..."
)

@Entity(tableName = "womens_hub")
data class WomenHubItem(
    @PrimaryKey val id: String,
    val title: String,
    val creatorName: String,
    val creatorVillage: String,
    val category: String, // Handcrafted Textile, Digital Service, Local Organic, Art & Design
    val priceOrRate: String,
    val description: String,
    val contactInfo: String,
    val likesCount: Int = 0,
    val isFavorite: Boolean = false,
    val isUserCreated: Boolean = false
)

@Entity(tableName = "ai_consultations")
data class AiConsultation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val reply: String,
    val domain: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PhanderDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Skills
    @Query("SELECT * FROM skills ORDER BY isFeatured DESC, title ASC")
    fun getAllSkills(): Flow<List<SkillItem>>

    @Query("SELECT * FROM skills WHERE id = :id")
    fun getSkillById(id: String): Flow<SkillItem?>

    @Query("SELECT * FROM skills WHERE isEnrolled = 1")
    fun getEnrolledSkills(): Flow<List<SkillItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillItem>)

    @Update
    suspend fun updateSkill(skill: SkillItem)

    // Opportunities
    @Query("SELECT * FROM opportunities ORDER BY isSaved DESC, deadline ASC")
    fun getAllOpportunities(): Flow<List<OpportunityItem>>

    @Query("SELECT * FROM opportunities WHERE id = :id")
    fun getOpportunityById(id: String): Flow<OpportunityItem?>

    @Query("SELECT * FROM opportunities WHERE isSaved = 1")
    fun getSavedOpportunities(): Flow<List<OpportunityItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunities(opportunities: List<OpportunityItem>)

    @Update
    suspend fun updateOpportunity(opportunity: OpportunityItem)

    // Roadmaps
    @Query("SELECT * FROM roadmaps")
    fun getAllRoadmaps(): Flow<List<RoadmapTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmaps(roadmaps: List<RoadmapTrack>)

    @Update
    suspend fun updateRoadmap(roadmap: RoadmapTrack)

    // Women's Hub
    @Query("SELECT * FROM womens_hub ORDER BY id DESC")
    fun getAllWomenHubItems(): Flow<List<WomenHubItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWomenHubItem(item: WomenHubItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWomenHubItems(items: List<WomenHubItem>)

    @Update
    suspend fun updateWomenHubItem(item: WomenHubItem)

    @Query("DELETE FROM womens_hub WHERE id = :id")
    suspend fun deleteWomenHubItem(id: String)

    // AI consultations
    @Query("SELECT * FROM ai_consultations ORDER BY timestamp DESC")
    fun getAiConsultations(): Flow<List<AiConsultation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiConsultation(consultation: AiConsultation)
}

@Database(
    entities = [
        UserProfile::class,
        SkillItem::class,
        OpportunityItem::class,
        RoadmapTrack::class,
        WomenHubItem::class,
        AiConsultation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhanderDatabase : RoomDatabase() {
    abstract fun phanderDao(): PhanderDao
}
