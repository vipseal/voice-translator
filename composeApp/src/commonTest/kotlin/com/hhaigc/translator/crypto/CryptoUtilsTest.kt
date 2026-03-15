package com.hhaigc.translator.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CryptoUtilsTest {

    // Helper to convert ByteArray to hex string
    private fun ByteArray.toHex(): String =
        joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    // ---- SHA-256 known test vectors (from NIST) ----

    @Test
    fun sha256EmptyString() {
        val hash = CryptoUtils.sha256(byteArrayOf())
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            hash.toHex()
        )
    }

    @Test
    fun sha256Abc() {
        val hash = CryptoUtils.sha256("abc".encodeToByteArray())
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            hash.toHex()
        )
    }

    @Test
    fun sha256LongerString() {
        // "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        val input = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        val hash = CryptoUtils.sha256(input.encodeToByteArray())
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            hash.toHex()
        )
    }

    @Test
    fun sha256HelloWorld() {
        val hash = CryptoUtils.sha256("Hello, World!".encodeToByteArray())
        assertEquals(
            "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f",
            hash.toHex()
        )
    }

    @Test
    fun sha256ProducesCorrectLength() {
        val hash = CryptoUtils.sha256("test".encodeToByteArray())
        assertEquals(32, hash.size)
    }

    // ---- XOR decryption logic ----

    @Test
    fun sha256DifferentInputsProduceDifferentHashes() {
        val hash1 = CryptoUtils.sha256("input1".encodeToByteArray())
        val hash2 = CryptoUtils.sha256("input2".encodeToByteArray())
        assertTrue(
            !hash1.contentEquals(hash2),
            "Different inputs should produce different hashes"
        )
    }

    @Test
    fun sha256SameInputProducesSameHash() {
        val hash1 = CryptoUtils.sha256("deterministic".encodeToByteArray())
        val hash2 = CryptoUtils.sha256("deterministic".encodeToByteArray())
        assertTrue(
            hash1.contentEquals(hash2),
            "Same input should produce same hash"
        )
    }

    // ---- tryDecryptApiKey ----

    @Test
    fun tryDecryptApiKeyWithInvalidCodeReturnsNull() {
        val result = CryptoUtils.tryDecryptApiKey("wrong-activation-code")
        assertNull(result, "Invalid activation code should return null")
    }

    @Test
    fun tryDecryptApiKeyWithEmptyCodeReturnsNull() {
        val result = CryptoUtils.tryDecryptApiKey("")
        assertNull(result, "Empty activation code should return null")
    }

    @Test
    fun tryDecryptApiKeyWithRandomStringReturnsNull() {
        val result = CryptoUtils.tryDecryptApiKey("some-random-string-12345")
        assertNull(result, "Random string should return null")
    }
}
