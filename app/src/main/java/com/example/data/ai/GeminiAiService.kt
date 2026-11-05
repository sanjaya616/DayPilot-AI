package com.example.data.ai

import android.util.Log
import com.example.data.model.AiMemory
import com.example.data.model.LearningPlan
import com.example.data.model.ScheduledTask
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiAiService"

data class GeneratedRoadmap(
    val title: String,
    val description: String,
    val category: String,
    val totalDays: Int,
    val dailyDurationMinutes: Int,
    val targetSkillLevel: String,
    val lessons: List<GeneratedLesson>
)

data class GeneratedLesson(
    val dayNumber: Int,
    val title: String,
    val durationMinutes: Int,
    val objectives: List<String>,
    val steps: List<String>,
    val resources: List<String>,
    val assignment: String,
    val completionCriteria: String
)

sealed interface AiAssistantResponse {
    data class ConversationalAnswer(val text: String) : AiAssistantResponse
    data class ClarificationNeeded(val question: String) : AiAssistantResponse
    data class RoadmapProposal(val roadmap: GeneratedRoadmap, val explanation: String) : AiAssistantResponse
    data class Error(val message: String) : AiAssistantResponse
}

class GeminiAiService {

    // 1. Structured Schema for Roadmaps
    private val lessonSchema = Schema.obj(
        properties = mapOf(
            "dayNumber" to Schema.integer(description = "Day number, sequential starting from 1"),
            "title" to Schema.string(description = "Concise, descriptive lesson title"),
            "durationMinutes" to Schema.integer(description = "Lesson study duration in minutes"),
            "objectives" to Schema.array(items = Schema.string(), description = "Learning objectives"),
            "steps" to Schema.array(items = Schema.string(), description = "Step-by-step instructions"),
            "resources" to Schema.array(items = Schema.string(), description = "Verified tutorials or verified study guides"),
            "assignment" to Schema.string(description = "Hands-on practical task"),
            "completionCriteria" to Schema.string(description = "Expected result and deliverables")
        )
    )

    private val roadmapSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Title of the learning roadmap"),
            "description" to Schema.string(description = "Comprehensive overview of the roadmap"),
            "category" to Schema.string(description = "Category: Tech & Software, Design, Data Analysis, Fitness, Leadership, or General"),
            "totalDays" to Schema.integer(description = "Total number of daily lessons"),
            "dailyDurationMinutes" to Schema.integer(description = "Daily study commitment in minutes"),
            "targetSkillLevel" to Schema.string(description = "Target proficiency e.g. Intermediate, Mastery"),
            "lessons" to Schema.array(items = lessonSchema, description = "Day-by-day sequential lessons")
        )
    )

    // Lazy initialization of Generative Models
    private val conversationalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val structuredRoadmapModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = roadmapSchema
            }
        )
    }

    suspend fun generateStructuredRoadmap(
        goalPrompt: String,
        memory: AiMemory?,
        requestedDays: Int = 14,
        dailyMinutes: Int = 45
    ): GeneratedRoadmap = withContext(Dispatchers.IO) {
        withTimeout(60_000L) {
            val memoryContext = if (memory != null) {
                """
                User Learning Context:
                - Preferred Languages: ${memory.preferredLanguages}
                - Current Skills: ${memory.skillLevels}
                - Learning Difficulties: ${memory.difficulties}
                - Notes: ${memory.notes}
                """.trimIndent()
            } else ""

            val prompt = """
            You are DayPilot AI, an elite educational curriculum designer.
            Create a realistic, sequential, non-duplicated daily learning roadmap for:
            "$goalPrompt"
            
            $memoryContext
            
            Requirements:
            - Create exactly $requestedDays consecutive daily lessons (Day 1 to Day $requestedDays).
            - Daily study duration: $dailyMinutes minutes.
            - Provide clear step-by-step instructions and actionable assignments for every lesson.
            - Ensure verified or official resource references (no fake broken URLs; reference official documentation, e.g. photopea.com/learn, kotlinlang.org, developer.android.com).
            - Day numbers must be strictly sequential (1, 2, 3...).
            """.trimIndent()

            val response = structuredRoadmapModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Model returned empty text.")

            parseRoadmapJson(jsonText)
        }
    }

    suspend fun processAssistantMessage(
        userMessage: String,
        activePlans: List<LearningPlan>,
        todayTasks: List<ScheduledTask>,
        allTasks: List<ScheduledTask>,
        userProfile: UserProfile,
        aiMemory: AiMemory?
    ): AiAssistantResponse = withContext(Dispatchers.IO) {
        try {
            withTimeout(60_000L) {
                // Check if user is asking to create/learn something new
                val lower = userMessage.lowercase()
                val isGoalRequest = lower.contains("i want to learn") ||
                    lower.contains("i want to study") ||
                    lower.contains("i want to exercise") ||
                    lower.contains("create a roadmap") ||
                    lower.contains("create a plan") ||
                    lower.contains("make a plan") ||
                    lower.contains("teach me") ||
                    (lower.contains("learn") && (lower.contains("days") || lower.contains("month") || lower.contains("hours")))

                if (isGoalRequest) {
                    // Check if critical details are missing (e.g. topic or timeframe)
                    val hasTimeframe = lower.contains("day") || lower.contains("week") || lower.contains("month")
                    val hasDailyTime = lower.contains("min") || lower.contains("hour") || lower.contains("pm") || lower.contains("am")

                    if (!hasTimeframe && !hasDailyTime && userMessage.trim().split(" ").size < 6) {
                        return@withTimeout AiAssistantResponse.ClarificationNeeded(
                            "I would love to build a personalized roadmap for you! How many days would you like to dedicate to this, and how many minutes can you study each day?"
                        )
                    }

                    // Extract approximate days & minutes or default gracefully
                    val extractedDays = when {
                        lower.contains("30 days") || lower.contains("a month") -> 30
                        lower.contains("2 weeks") || lower.contains("two weeks") || lower.contains("14 days") -> 14
                        lower.contains("7 days") || lower.contains("a week") || lower.contains("one week") -> 7
                        lower.contains("21 days") || lower.contains("3 weeks") -> 21
                        else -> 14
                    }

                    val extractedMinutes = when {
                        lower.contains("1 hour") || lower.contains("one hour") || lower.contains("60 min") -> 60
                        lower.contains("45 min") -> 45
                        lower.contains("30 min") -> 30
                        lower.contains("90 min") || lower.contains("1.5 hour") -> 90
                        else -> 45
                    }

                    val roadmap = generateStructuredRoadmap(
                        goalPrompt = userMessage,
                        memory = aiMemory,
                        requestedDays = extractedDays,
                        dailyMinutes = extractedMinutes
                    )

                    return@withTimeout AiAssistantResponse.RoadmapProposal(
                        roadmap = roadmap,
                        explanation = "I have generated a ${roadmap.totalDays}-day structured curriculum for **${roadmap.title}** (${roadmap.dailyDurationMinutes} min/day). You can preview all day-by-day lessons, edit instructions, and approve the schedule below."
                    )
                }

                // General Contextual Q&A using live database state
                val plansContext = if (activePlans.isEmpty()) "No active plans."
                else activePlans.joinToString("; ") { "${it.name} (${it.status}, progress: ${it.progressPercent.toInt()}%)" }

                val todayTasksContext = if (todayTasks.isEmpty()) "No tasks scheduled for today."
                else todayTasks.joinToString("; ") { "${it.title} at ${it.startTime} (${it.durationMinutes}m, completed: ${it.isCompleted})" }

                val memoryContext = if (aiMemory != null) {
                    """
                    Saved AI Memory:
                    - Preferred Study Time: ${aiMemory.preferredStudyTime}
                    - Languages: ${aiMemory.preferredLanguages}
                    - Skill Levels: ${aiMemory.skillLevels}
                    - Difficulties: ${aiMemory.difficulties}
                    - Learning Notes: ${aiMemory.notes}
                    """.trimIndent()
                } else "No memory recorded yet."

                val prompt = """
                You are DayPilot AI, a personal study, productivity, and scheduling assistant.
                
                Live User Application Data:
                - User: ${userProfile.displayName} (Streak: ${userProfile.streakDays} days, Score: ${userProfile.overallScore})
                - Active Learning Plans: $plansContext
                - Today's Scheduled Activities: $todayTasksContext
                - Total Tasks in System: ${allTasks.size}
                $memoryContext
                
                User query: "$userMessage"
                
                Instructions:
                - Use the real application data above to answer accurately (e.g. what's next, progress, missed tasks, explanations).
                - Do not invent fake unrecorded tasks or pretended notifications.
                - Keep your tone motivating, concise, and structured.
                """.trimIndent()

                val response = conversationalModel.generateContent(prompt)
                val text = response.text ?: "I am here to assist with your learning and schedule."
                AiAssistantResponse.ConversationalAnswer(text)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI processing timed out", e)
            AiAssistantResponse.Error("Request timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI processing failed", e)
            AiAssistantResponse.Error(e.localizedMessage ?: "AI assistant encountered an issue.")
        }
    }

    private fun parseRoadmapJson(jsonString: String): GeneratedRoadmap {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "Structured Learning Roadmap")
        val description = root.optString("description", "")
        val category = root.optString("category", "General")
        val totalDays = root.optInt("totalDays", 14)
        val dailyDurationMinutes = root.optInt("dailyDurationMinutes", 45)
        val targetSkillLevel = root.optString("targetSkillLevel", "Intermediate")

        val lessonsArray = root.optJSONArray("lessons") ?: org.json.JSONArray()
        val lessons = mutableListOf<GeneratedLesson>()

        for (i in 0 until lessonsArray.length()) {
            val lessonObj = lessonsArray.getJSONObject(i)
            val dayNumber = lessonObj.optInt("dayNumber", i + 1)
            val lessonTitle = lessonObj.optString("title", "Day $dayNumber Lesson")
            val durationMinutes = lessonObj.optInt("durationMinutes", dailyDurationMinutes)

            val objectivesList = mutableListOf<String>()
            val objArray = lessonObj.optJSONArray("objectives")
            if (objArray != null) {
                for (j in 0 until objArray.length()) {
                    objectivesList.add(objArray.getString(j))
                }
            }

            val stepsList = mutableListOf<String>()
            val stepsArray = lessonObj.optJSONArray("steps")
            if (stepsArray != null) {
                for (j in 0 until stepsArray.length()) {
                    stepsList.add(stepsArray.getString(j))
                }
            }

            val resourcesList = mutableListOf<String>()
            val resArray = lessonObj.optJSONArray("resources")
            if (resArray != null) {
                for (j in 0 until resArray.length()) {
                    resourcesList.add(resArray.getString(j))
                }
            }

            val assignment = lessonObj.optString("assignment", "")
            val completionCriteria = lessonObj.optString("completionCriteria", "")

            lessons.add(
                GeneratedLesson(
                    dayNumber = dayNumber,
                    title = lessonTitle,
                    durationMinutes = durationMinutes,
                    objectives = objectivesList,
                    steps = stepsList,
                    resources = resourcesList,
                    assignment = assignment,
                    completionCriteria = completionCriteria
                )
            )
        }

        // Validate sequential day numbers and ensure at least 1 lesson exists
        val sortedLessons = if (lessons.isEmpty()) {
            // Safe fallback if empty array returned
            (1..totalDays).map { d ->
                GeneratedLesson(
                    dayNumber = d,
                    title = "Day $d: $title Fundamentals",
                    durationMinutes = dailyDurationMinutes,
                    objectives = listOf("Master core module $d"),
                    steps = listOf("Read study guide", "Execute practice exercises"),
                    resources = listOf("https://developer.android.com"),
                    assignment = "Complete module $d mini-project",
                    completionCriteria = "Verify all exercises pass self-test"
                )
            }
        } else {
            lessons.sortedBy { it.dayNumber }
        }

        return GeneratedRoadmap(
            title = title,
            description = description,
            category = category,
            totalDays = totalDays,
            dailyDurationMinutes = dailyDurationMinutes,
            targetSkillLevel = targetSkillLevel,
            lessons = sortedLessons
        )
    }
}
