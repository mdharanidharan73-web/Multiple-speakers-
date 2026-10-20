package com.example

import com.example.bluetooth.BrandDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandDetectorTest {

    @Test
    fun testDetectBrand_boAt() {
        assertEquals("boAt", BrandDetector.detectBrand("boAt Stone 650"))
        assertEquals("boAt", BrandDetector.detectBrand("boAt Rugby"))
        assertEquals("boAt", BrandDetector.detectBrand("BOAT Airdopes 441"))
    }

    @Test
    fun testDetectBrand_Sony() {
        assertEquals("Sony", BrandDetector.detectBrand("Sony SRS-XB13"))
        assertEquals("Sony", BrandDetector.detectBrand("SRS-XB23 Portable"))
        assertEquals("Sony", BrandDetector.detectBrand("WH-1000XM5"))
    }

    @Test
    fun testDetectBrand_JBL() {
        assertEquals("JBL", BrandDetector.detectBrand("JBL Flip 6"))
        assertEquals("JBL", BrandDetector.detectBrand("Charge 5"))
        assertEquals("JBL", BrandDetector.detectBrand("JBL Pulse 4"))
    }

    @Test
    fun testDetectBrand_Bose() {
        assertEquals("Bose", BrandDetector.detectBrand("Bose SoundLink Flex"))
        assertEquals("Bose", BrandDetector.detectBrand("QuietComfort 45"))
    }

    @Test
    fun testDetectBrand_Marshall() {
        assertEquals("Marshall", BrandDetector.detectBrand("Marshall Emberton II"))
        assertEquals("Marshall", BrandDetector.detectBrand("Kilburn II Portable"))
    }

    @Test
    fun testDetectBrand_OtherKnownBrands() {
        assertEquals("Soundcore", BrandDetector.detectBrand("Soundcore Motion+"))
        assertEquals("Soundcore", BrandDetector.detectBrand("Anker Flare 2"))
        assertEquals("Samsung", BrandDetector.detectBrand("Galaxy Buds Pro"))
        assertEquals("Harman Kardon", BrandDetector.detectBrand("Harman Kardon Onyx"))
        assertEquals("Realme", BrandDetector.detectBrand("Realme Cobble Speaker"))
        assertEquals("OnePlus", BrandDetector.detectBrand("OnePlus Bullets Wireless"))
        assertEquals("Xiaomi", BrandDetector.detectBrand("Mi Portable Bluetooth Speaker"))
        assertEquals("Ultimate Ears", BrandDetector.detectBrand("UE Wonderboom 3"))
    }

    @Test
    fun testDetectBrand_Unknown() {
        assertEquals("Unknown", BrandDetector.detectBrand("Generic BT Device"))
        assertEquals("Unknown", BrandDetector.detectBrand(""))
        assertEquals("Unknown", BrandDetector.detectBrand(null))
    }

    @Test
    fun testIsAudioDevice() {
        assertTrue(BrandDetector.isAudioDevice("boAt Stone 650"))
        assertTrue(BrandDetector.isAudioDevice("Generic Wireless Speaker"))
        assertTrue(BrandDetector.isAudioDevice("Desk Soundbar"))
        assertFalse(BrandDetector.isAudioDevice("Smart Watch 4"))
        assertFalse(BrandDetector.isAudioDevice("OBD2 Car Scanner"))
    }
}
