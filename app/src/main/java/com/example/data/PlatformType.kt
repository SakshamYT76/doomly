package com.example.data

/**
 * Supported short-form video platforms tracked by DOOMLY.
 */
enum class PlatformType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val feedName: String,
    val packageNames: List<String>
) {
    INSTAGRAM(
        id = "INSTAGRAM",
        displayName = "Instagram Reels",
        shortName = "Instagram",
        feedName = "Reels",
        packageNames = listOf("com.instagram.android")
    ),
    YOUTUBE(
        id = "YOUTUBE",
        displayName = "YouTube Shorts",
        shortName = "YouTube",
        feedName = "Shorts",
        packageNames = listOf("com.google.android.youtube")
    ),
    TIKTOK(
        id = "TIKTOK",
        displayName = "TikTok",
        shortName = "TikTok",
        feedName = "For You / Following",
        packageNames = listOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill"
        )
    );

    companion object {
        fun fromId(id: String): PlatformType? = entries.find { it.id == id }

        fun fromPackageName(pkg: String): PlatformType? =
            entries.find { pkg in it.packageNames }
    }
}

/**
 * Explicit, honest tracking status for each platform.
 */
enum class PlatformTrackingStatus(val label: String) {
    NOT_INSTALLED("Not installed"),
    PERMISSION_MISSING("Permission missing"),
    SCROLL_TRACKING_OFF("Scroll tracking off"),
    ACTIVE("Active")
}

/**
 * Playful, non-medical Brain Status indicator based on combined daily estimated scrolls.
 * Disclaimer: "These labels are playful awareness indicators, not medical or scientific assessments."
 */
enum class BrainStatus(
    val label: String,
    val rangeText: String,
    val minScrolls: Int,
    val maxScrolls: Int?,
    val headline: String,
    val description: String,
    val mascotTapMessages: List<String>
) {
    FRESH_BRAIN(
        label = "Fresh Brain",
        rangeText = "0–99 scrolls",
        minScrolls = 0,
        maxScrolls = 99,
        headline = "Calm, sharp, and unbothered.",
        description = "Your short-form feed scrolling is light today. Plenty of mental space left for things you actually care about.",
        mascotTapMessages = listOf(
            "Feeling crisp today! Let's keep the scroll count low.",
            "Zero doom-loop detected. Your attention span says thank you.",
            "Fresh as morning air! Go build or read something cool.",
            "Still under 100 scrolls—easy cruising!"
        )
    ),
    A_BIT_BAD(
        label = "A Bit Bad",
        rangeText = "100–199 scrolls",
        minScrolls = 100,
        maxScrolls = 199,
        headline = "Starting to feel the thumb workout.",
        description = "You've crossed 100 estimated scrolls today. The feed algorithm is warming up—good time for a quick stretch.",
        mascotTapMessages = listOf(
            "Whoa, over 100 swipes already! Remember to blink.",
            "My synapses are getting a little dizzy from all the vertical motion.",
            "Do you even remember what the video 5 swipes ago was about?",
            "A quick 5-minute break away from the screen would feel great right now."
        )
    ),
    WORSE(
        label = "Worse",
        rangeText = "200–299 scrolls",
        minScrolls = 200,
        maxScrolls = 299,
        headline = "Heavy scroll territory.",
        description = "Over 200 estimated short-form scrolls today. That's a marathon of rapid context-switching.",
        mascotTapMessages = listOf(
            "200+ scrolls! My eyes are spinning like slot reels.",
            "Time check: how long did you plan to scroll when you opened the app?",
            "Hold up—let's put the phone face down for ten minutes!",
            "Another swipe? Give this tired brain a breather!"
        )
    ),
    BAD(
        label = "Bad",
        rangeText = "300+ scrolls",
        minScrolls = 300,
        maxScrolls = null,
        headline = "Maximum scroll overload.",
        description = "You've passed 300 estimated scrolls today. The endless feed has had plenty of your time—step outside or lock the screen for a bit.",
        mascotTapMessages = listOf(
            "300+ scrolls! I am officially begging for some fresh air.",
            "The algorithm never ends, so you have to be the one to close the app!",
            "Emergency grass-touching protocol recommended.",
            "Put the phone down, take a deep breath, and rest your eyes."
        )
    );

    companion object {
        val MILESTONE_THRESHOLDS = listOf(200, 300, 500)

        fun fromScrollCount(scrolls: Int): BrainStatus {
            val safeScrolls = scrolls.coerceAtLeast(0)
            return when {
                safeScrolls < 100 -> FRESH_BRAIN
                safeScrolls < 200 -> A_BIT_BAD
                safeScrolls < 300 -> WORSE
                else -> BAD
            }
        }
    }
}
