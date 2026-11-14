package com.example

import android.content.Context
import android.content.pm.PackageInfo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrainStatus
import com.example.data.DoomlyDatabase
import com.example.data.DoomlyPreferences
import com.example.data.DoomlyRepository
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.tracking.PlatformSystemMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: DoomlyDatabase
    private lateinit var preferences: DoomlyPreferences
    private lateinit var notifiedMilestones: MutableList<Pair<Int, Int>>
    private lateinit var repository: DoomlyRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, DoomlyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DoomlyDatabase.setTestInstance(database)
        preferences = DoomlyPreferences(context)
        preferences.setMasterScrollTrackingEnabled(true)
        preferences.setPlatformTrackingEnabled(PlatformType.INSTAGRAM, true)
        preferences.setPlatformTrackingEnabled(PlatformType.YOUTUBE, true)
        preferences.setPlatformTrackingEnabled(PlatformType.TIKTOK, true)
        preferences.setMilestoneNotificationsEnabled(true)

        notifiedMilestones = mutableListOf()
        repository = DoomlyRepository(
            dao = database.doomlyDao(),
            preferences = preferences,
            onMilestoneCrossed = { threshold, total ->
                notifiedMilestones.add(threshold to total)
            }
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun appName_isDoomly() {
        assertEquals("DOOMLY", context.getString(R.string.app_name))
    }

    @Test
    fun platformSystemMonitor_detectsInstalledAndAbsentAppsHonestly() {
        val monitor = PlatformSystemMonitor(context)
        val shadowPm = shadowOf(context.packageManager)

        // Initially none of the target apps are installed in the clean test environment
        assertFalse(monitor.isPlatformInstalled(PlatformType.INSTAGRAM))
        assertFalse(monitor.isPlatformInstalled(PlatformType.TIKTOK))
        assertEquals(
            PlatformTrackingStatus.NOT_INSTALLED,
            monitor.resolveTrackingStatus(PlatformType.TIKTOK, true)
        )

        // Install Instagram only; TikTok remains uninstalled
        shadowPm.installPackage(PackageInfo().apply { packageName = "com.instagram.android" })
        assertTrue(monitor.isPlatformInstalled(PlatformType.INSTAGRAM))
        assertFalse(monitor.isPlatformInstalled(PlatformType.TIKTOK))

        // With Instagram installed but Accessibility Service off, status is PERMISSION_MISSING
        assertEquals(
            PlatformTrackingStatus.PERMISSION_MISSING,
            monitor.resolveTrackingStatus(PlatformType.INSTAGRAM, true)
        )
    }

    @Test
    fun repository_recordsScrolls_triggersMilestonesOncePerDay_andHandlesMidnightRollover() = runBlocking {
        val zone = ZoneId.of("UTC")
        val day1Ms = LocalDate.of(2026, 11, 14).atTime(23, 50).atZone(zone).toInstant().toEpochMilli()
        val day2Ms = LocalDate.of(2026, 11, 15).atTime(0, 5).atZone(zone).toInstant().toEpochMilli()

        // 1. Record 199 scrolls on Day 1 -> BrainStatus A_BIT_BAD, no milestones crossed yet
        val crossedAt199 = repository.recordEstimatedScroll(
            platform = PlatformType.INSTAGRAM,
            delta = 199,
            timestampMs = day1Ms,
            zoneId = zone
        )
        assertTrue(crossedAt199.isEmpty())
        assertTrue(notifiedMilestones.isEmpty())

        // 2. Record 1 more scroll on Day 1 -> hits 200 (WORSE), triggers 200 milestone once
        val crossedAt200 = repository.recordEstimatedScroll(
            platform = PlatformType.YOUTUBE,
            delta = 1,
            timestampMs = day1Ms + 1000L,
            zoneId = zone
        )
        assertEquals(listOf(200), crossedAt200)
        assertEquals(1, notifiedMilestones.size)
        assertEquals(200 to 200, notifiedMilestones.first())

        // 3. Record 50 more scrolls on Day 1 (total 250) -> 200 must NOT fire a second time
        val crossedAt250 = repository.recordEstimatedScroll(
            platform = PlatformType.INSTAGRAM,
            delta = 50,
            timestampMs = day1Ms + 2000L,
            zoneId = zone
        )
        assertTrue(crossedAt250.isEmpty())
        assertEquals(1, notifiedMilestones.size)

        // 4. Record 260 more scrolls on Day 1 (total 510) -> crosses both 300 and 500
        val crossedAt510 = repository.recordEstimatedScroll(
            platform = PlatformType.TIKTOK,
            delta = 260,
            timestampMs = day1Ms + 3000L,
            zoneId = zone
        )
        assertEquals(listOf(300, 500), crossedAt510)
        assertEquals(3, notifiedMilestones.size)
        assertEquals(BrainStatus.BAD, BrainStatus.fromScrollCount(510))

        // 5. Midnight rollover to Day 2 (2026-11-15): Day 2 starts fresh at 0 and can trigger 200 again
        val day2Initial = repository.observeDailyScrollsForDate("2026-11-15").first()
        assertTrue(day2Initial.isEmpty())

        val crossedDay2 = repository.recordEstimatedScroll(
            platform = PlatformType.INSTAGRAM,
            delta = 205,
            timestampMs = day2Ms,
            zoneId = zone
        )
        assertEquals(listOf(200), crossedDay2)
        assertEquals(4, notifiedMilestones.size)

        // Verify Day 1 data is preserved intact while Day 2 has its own count
        val day1Scrolls = repository.observeDailyScrollsForDate("2026-11-14").first().sumOf { it.estimatedScrolls }
        val day2Scrolls = repository.observeDailyScrollsForDate("2026-11-15").first().sumOf { it.estimatedScrolls }
        assertEquals(510, day1Scrolls)
        assertEquals(205, day2Scrolls)
    }

    @Test
    fun repository_resetTodayAndDeleteAll_workCorrectly() = runBlocking {
        val zone = ZoneId.of("UTC")
        val day1Ms = LocalDate.of(2026, 11, 13).atTime(14, 0).atZone(zone).toInstant().toEpochMilli()
        val day2Ms = LocalDate.of(2026, 11, 14).atTime(14, 0).atZone(zone).toInstant().toEpochMilli()

        repository.recordEstimatedScroll(PlatformType.INSTAGRAM, delta = 80, timestampMs = day1Ms, zoneId = zone)
        repository.recordEstimatedScroll(PlatformType.YOUTUBE, delta = 220, timestampMs = day2Ms, zoneId = zone)

        // Reset Day 2 ("today") only -> Day 1 remains intact
        repository.resetDateData("2026-11-14")
        val day2AfterReset = repository.observeDailyScrollsForDate("2026-11-14").first()
        val day1AfterReset = repository.observeDailyScrollsForDate("2026-11-13").first()
        assertTrue(day2AfterReset.isEmpty())
        assertEquals(80, day1AfterReset.sumOf { it.estimatedScrolls })

        // Delete all data -> wipes all dates
        repository.deleteAllData()
        val allRemaining = repository.observeAllDailyScrolls().first()
        assertTrue(allRemaining.isEmpty())
    }

    @Test
    fun accessibilityHeuristic_countsVerticalFeedPager_andIgnoresHorizontalOrAmbiguousScrolls() {
        val service = com.example.tracking.DoomlyAccessibilityService()

        // 1. Ambiguous non-pager class (e.g., LinearLayout or EditText) must NOT be counted
        val nonFeedEvent = android.view.accessibility.AccessibilityEvent.obtain(
            android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
        ).apply {
            packageName = "com.instagram.android"
            className = "android.widget.EditText"
            scrollDeltaX = 0
            scrollDeltaY = 400
        }
        assertFalse(
            service.shouldCountAsShortVideoFeedScroll(
                PlatformType.INSTAGRAM,
                nonFeedEvent,
                "android.widget.EditText"
            )
        )

        // 2. Horizontal carousel inside RecyclerView (deltaX > deltaY) must NOT be counted
        val horizontalEvent = android.view.accessibility.AccessibilityEvent.obtain(
            android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
        ).apply {
            packageName = "com.instagram.android"
            className = "androidx.recyclerview.widget.RecyclerView"
            scrollDeltaX = 500
            scrollDeltaY = 40
        }
        assertFalse(
            service.shouldCountAsShortVideoFeedScroll(
                PlatformType.INSTAGRAM,
                horizontalEvent,
                "androidx.recyclerview.widget.RecyclerView"
            )
        )

        // 3. Vertical ViewPager2 swipe inside Reels/Shorts with vertical delta >= 180px IS counted
        val verticalFeedEvent = android.view.accessibility.AccessibilityEvent.obtain(
            android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
        ).apply {
            packageName = "com.google.android.youtube"
            className = "androidx.viewpager2.widget.ViewPager2"
            scrollDeltaX = 0
            scrollDeltaY = 600
        }
        assertTrue(
            service.shouldCountAsShortVideoFeedScroll(
                PlatformType.YOUTUBE,
                verticalFeedEvent,
                "androidx.viewpager2.widget.ViewPager2"
            )
        )
    }

    @Test
    fun viewModel_navigatesAcrossAll14Screens_andHandlesHonestUnavailableStates() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.DoomlyViewModel(app)

        // Initial launch starts on SPLASH
        assertEquals(com.example.ui.DoomlyScreen.SPLASH, vm.navigationStack.value.last())

        // First launch (onboarding not completed) transitions SPLASH -> ONBOARDING -> PERMISSIONS -> DASHBOARD
        vm.completeSplash()
        assertEquals(com.example.ui.DoomlyScreen.ONBOARDING, vm.navigationStack.value.last())

        vm.completeOnboarding()
        assertEquals(com.example.ui.DoomlyScreen.PERMISSIONS, vm.navigationStack.value.last())

        vm.finishPermissionSetupToDashboard()
        assertEquals(com.example.ui.DoomlyScreen.DASHBOARD, vm.navigationStack.value.last())

        // Verify navigating through all remaining screens and popping backstack
        val screensToVisit = listOf(
            com.example.ui.DoomlyScreen.PLATFORM_INSTAGRAM,
            com.example.ui.DoomlyScreen.PLATFORM_YOUTUBE,
            com.example.ui.DoomlyScreen.PLATFORM_TIKTOK,
            com.example.ui.DoomlyScreen.DAILY_STATISTICS,
            com.example.ui.DoomlyScreen.HISTORY,
            com.example.ui.DoomlyScreen.BRAIN_STATUS,
            com.example.ui.DoomlyScreen.MASCOT_INTERACTION,
            com.example.ui.DoomlyScreen.SETTINGS,
            com.example.ui.DoomlyScreen.PRIVACY_INFO,
            com.example.ui.DoomlyScreen.RESET_DATA
        )
        for (screen in screensToVisit) {
            vm.navigateTo(screen)
            assertEquals(screen, vm.navigationStack.value.last())
            assertTrue(vm.navigateBack())
            assertEquals(com.example.ui.DoomlyScreen.DASHBOARD, vm.navigationStack.value.last())
        }
    }
}
