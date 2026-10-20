package com.example

import android.media.AudioDeviceInfo
import com.example.audio.DualOutputCheckerImpl
import com.example.model.AudioRouteInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DualOutputCheckerTest {

    private val defaultChecker = DualOutputCheckerImpl()
    private val samsungChecker = DualOutputCheckerImpl(manufacturerOverride = "samsung", sdkIntOverride = 34)
    private val xiaomiChecker = DualOutputCheckerImpl(manufacturerOverride = "xiaomi", sdkIntOverride = 34)

    @Test
    fun testCheckDualOutputSupport_NoDevices() {
        val routes = listOf(
            AudioRouteInfo(
                id = 1,
                productName = "Phone Speaker",
                type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                typeName = "Phone Built-in Speaker",
                isBluetooth = false,
                isBle = false,
                isSink = true
            )
        )

        val result = defaultChecker.checkDualOutputSupport(routes)
        assertFalse(result.isDualActive)
        assertEquals(0, result.activeCount)
        assertTrue(result.activeDeviceNames.isEmpty())
        assertEquals("No Active Bluetooth Speakers", result.statusTitle)
    }

    @Test
    fun testCheckDualOutputSupport_SingleBluetoothDevice() {
        val routes = listOf(
            AudioRouteInfo(
                id = 1,
                productName = "boAt Stone 650",
                type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                typeName = "Bluetooth A2DP Speaker",
                isBluetooth = true,
                isBle = false,
                isSink = true,
                address = "00:11:22:33:44:55"
            ),
            AudioRouteInfo(
                id = 2,
                productName = "Phone Speaker",
                type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                typeName = "Phone Built-in Speaker",
                isBluetooth = false,
                isBle = false,
                isSink = true
            )
        )

        val result = defaultChecker.checkDualOutputSupport(routes)
        assertFalse(result.isDualActive)
        assertEquals(1, result.activeCount)
        assertEquals(listOf("boAt Stone 650"), result.activeDeviceNames)
        assertTrue(result.statusTitle.contains("Single Output Active"))

        val samsungResult = samsungChecker.checkDualOutputSupport(routes)
        assertTrue(samsungResult.oemFeatureName.contains("Samsung Dual Audio"))
        assertTrue(samsungResult.recommendation.contains("Media Output"))

        val xiaomiResult = xiaomiChecker.checkDualOutputSupport(routes)
        assertTrue(xiaomiResult.oemFeatureName.contains("Xiaomi Sound Assistant"))
    }

    @Test
    fun testCheckDualOutputSupport_TwoBluetoothDevices_DualOutputActive() {
        val routes = listOf(
            AudioRouteInfo(
                id = 1,
                productName = "boAt Stone 650",
                type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                typeName = "Bluetooth A2DP Speaker",
                isBluetooth = true,
                isBle = false,
                isSink = true,
                address = "00:11:22:33:44:55"
            ),
            AudioRouteInfo(
                id = 2,
                productName = "Sony SRS-XB13",
                type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                typeName = "Bluetooth A2DP Speaker",
                isBluetooth = true,
                isBle = false,
                isSink = true,
                address = "66:77:88:99:AA:BB"
            )
        )

        val result = defaultChecker.checkDualOutputSupport(routes)
        assertTrue(result.isDualActive)
        assertEquals(2, result.activeCount)
        assertEquals(listOf("boAt Stone 650", "Sony SRS-XB13"), result.activeDeviceNames)
        assertEquals("Dual Output Active", result.statusTitle)
    }
}
