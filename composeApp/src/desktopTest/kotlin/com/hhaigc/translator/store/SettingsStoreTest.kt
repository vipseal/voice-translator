package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettingsStoreTest {

    private fun getStore(): SettingsStore = SettingsStore.getInstance()

    @Test
    fun settingsStoreInstanceIsNotNull() {
        val store = getStore()
        assertNotNull(store)
    }

    @Test
    fun settingsStoreSingletonReturnsSameInstance() {
        val store1 = getStore()
        val store2 = getStore()
        assertTrue(store1 === store2, "getInstance() should return the same instance")
    }

    @Test
    fun setAndGetThemeMode() = runTest {
        val store = getStore()
        store.setThemeMode("dark")
        assertEquals("dark", store.getThemeMode())
        store.setThemeMode("light")
        assertEquals("light", store.getThemeMode())
        // Reset to auto
        store.setThemeMode("auto")
        assertEquals("auto", store.getThemeMode())
    }

    @Test
    fun setAndGetSourceLanguage() = runTest {
        val store = getStore()
        store.setSourceLanguage("zh")
        assertEquals("zh", store.getSourceLanguage())
        store.setSourceLanguage("en")
        assertEquals("en", store.getSourceLanguage())
    }

    @Test
    fun setAndGetActivated() = runTest {
        val store = getStore()
        store.setActivated(true)
        assertTrue(store.isActivated())
        store.setActivated(false)
        assertFalse(store.isActivated())
    }

    @Test
    fun setAndGetApiKey() = runTest {
        val store = getStore()
        store.setApiKey("test-api-key-12345")
        assertEquals("test-api-key-12345", store.getApiKey())
        // Clean up
        store.setApiKey("")
        assertEquals("", store.getApiKey())
    }

    @Test
    fun getEnabledLanguagesReturnsFlow() = runTest {
        val store = getStore()
        val languages = store.getEnabledLanguages().first()
        assertNotNull(languages)
        assertTrue(languages.isNotEmpty(), "Languages list should not be empty")
    }

    @Test
    fun setLanguageEnabledUpdatesFlow() = runTest {
        val store = getStore()
        // Get initial state
        val initial = store.getEnabledLanguages().first()
        val koLang = initial.find { it.code == "ko" }
        assertNotNull(koLang)

        // Toggle Korean
        val originalState = koLang.isEnabled
        store.setLanguageEnabled("ko", !originalState)
        val updated = store.getEnabledLanguages().first()
        val updatedKo = updated.find { it.code == "ko" }
        assertNotNull(updatedKo)
        assertEquals(!originalState, updatedKo.isEnabled)

        // Restore
        store.setLanguageEnabled("ko", originalState)
    }
}
