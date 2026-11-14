package com.example

import com.example.data.BrainStatus
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.ui.components.formatUsageDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun brainStatus_mapsCorrectlyAcrossAllRanges() {
        assertEquals(BrainStatus.FRESH_BRAIN, BrainStatus.fromScrollCount(0))
        assertEquals(BrainStatus.FRESH_BRAIN, BrainStatus.fromScrollCount(99))
        assertEquals(BrainStatus.A_BIT_BAD, BrainStatus.fromScrollCount(100))
        assertEquals(BrainStatus.A_BIT_BAD, BrainStatus.fromScrollCount(199))
        assertEquals(BrainStatus.WORSE, BrainStatus.fromScrollCount(200))
        assertEquals(BrainStatus.WORSE, BrainStatus.fromScrollCount(299))
        assertEquals(BrainStatus.BAD, BrainStatus.fromScrollCount(300))
        assertEquals(BrainStatus.BAD, BrainStatus.fromScrollCount(650))
    }

    @Test
    fun platformType_lookupMatchesTargetPackages() {
        assertEquals(PlatformType.INSTAGRAM, PlatformType.fromPackageName("com.instagram.android"))
        assertEquals(PlatformType.YOUTUBE, PlatformType.fromPackageName("com.google.android.youtube"))
        assertEquals(PlatformType.TIKTOK, PlatformType.fromPackageName("com.zhiliaoapp.musically"))
        assertEquals(PlatformType.TIKTOK, PlatformType.fromPackageName("com.ss.android.ugc.trill"))
        assertNull(PlatformType.fromPackageName("com.android.settings"))
    }

    @Test
    fun formatUsageDuration_formatsHonestStrings() {
        assertEquals("Permission missing", formatUsageDuration(null))
        assertEquals("0m", formatUsageDuration(0L))
        assertEquals("< 1m", formatUsageDuration(30_000L))
        assertEquals("15m", formatUsageDuration(15 * 60_000L))
        assertEquals("2h 5m", formatUsageDuration((2 * 60 + 5) * 60_000L))
    }

    @Test
    fun trackingStatus_labelsMatchSpecification() {
        assertEquals("Not installed", PlatformTrackingStatus.NOT_INSTALLED.label)
        assertEquals("Permission missing", PlatformTrackingStatus.PERMISSION_MISSING.label)
        assertEquals("Scroll tracking off", PlatformTrackingStatus.SCROLL_TRACKING_OFF.label)
        assertEquals("Active", PlatformTrackingStatus.ACTIVE.label)
    }
}
