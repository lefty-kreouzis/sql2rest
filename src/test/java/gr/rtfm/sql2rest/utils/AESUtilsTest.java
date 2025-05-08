package gr.rtfm.sql2rest.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AESUtilsTest {

    @InjectMocks
    private AESUtils aesUtils;

    private final String testKey = "TestSecretKey123";
    private final String defaultKey = "H3ll0SLMH0wAr3You?";
    private final String testData = "This is a test message to encrypt";

    @BeforeEach
    void setUp() {
        aesUtils = new AESUtils();
        ReflectionTestUtils.setField(aesUtils, "stringKey", defaultKey);
    }

    @Test
    void testEncryptionDecryptionWithProvidedKey() {
        // Encrypt with custom key
        String encrypted = aesUtils.encrypt(testData, testKey);
        
        // Assert encryption produced non-null result
        assertNotNull(encrypted, "Encryption should not return null");
        
        // Verify encrypted text is different from original
        assertNotEquals(testData, encrypted, "Encrypted text should differ from original");
        
        // Decrypt with the same key
        String decrypted = aesUtils.decrypt(encrypted, testKey);
        
        // Assert decryption succeeded
        assertEquals(testData, decrypted, "Decrypted text should match original");
    }

    @Test
    void testEncryptionDecryptionWithDefaultKey() {
        // Encrypt using default key
        String encrypted = aesUtils.encrypt(testData);
        
        // Assert encryption produced non-null result
        assertNotNull(encrypted, "Encryption should not return null");
        
        // Verify encrypted text is different from original
        assertNotEquals(testData, encrypted, "Encrypted text should differ from original");
        
        // Decrypt using default key
        String decrypted = aesUtils.decrypt(encrypted);
        
        // Assert decryption succeeded
        assertEquals(testData, decrypted, "Decrypted text should match original");
    }

    @Test
    void testEncryptionWithDifferentKeysFailsDecryption() {
        // Encrypt with one key
        String encrypted = aesUtils.encrypt(testData, testKey);
        
        // Try to decrypt with different key
        String decrypted = aesUtils.decrypt(encrypted, "DifferentKey123");
        
        // Should fail and return null
        assertNull(decrypted, "Decryption with wrong key should fail");
    }

    @Test
    void testEncryptionWithEmptyString() {
        // Encrypt empty string
        String encrypted = aesUtils.encrypt("", testKey);
        
        // Assert encryption produced non-null result
        assertNotNull(encrypted, "Encryption of empty string should not return null");
        
        // Decrypt
        String decrypted = aesUtils.decrypt(encrypted, testKey);
        
        // Assert empty string is recovered
        assertEquals("", decrypted, "Decrypted text should be empty string");
    }

    @Test
    void testEncryptionWithSpecialCharacters() {
        String specialData = "!@#$%^&*()_+{}|:<>?~`-=[]\\;',./";
        
        // Encrypt special characters
        String encrypted = aesUtils.encrypt(specialData, testKey);
        
        // Assert encryption produced non-null result
        assertNotNull(encrypted, "Encryption of special characters should not return null");
        
        // Decrypt
        String decrypted = aesUtils.decrypt(encrypted, testKey);
        
        // Assert special characters are recovered
        assertEquals(specialData, decrypted, "Decrypted text should match original with special characters");
    }

    @Test
    void testNullInputs() {
        // Test null string to encrypt
        assertNull(aesUtils.encrypt(null, testKey), "Null input should return null on encryption");
        
        // Test null key
        assertNull(aesUtils.encrypt(testData, null), "Null key should return null on encryption");
        
        // Test null string to decrypt
        assertNull(aesUtils.decrypt(null, testKey), "Null input should return null on decryption");
    }

    @Test
    void testInvalidBase64ForDecryption() {
        // Not valid Base64
        String notBase64 = "This is not Base64!";
        
        // Should handle the exception and return null
        assertNull(aesUtils.decrypt(notBase64, testKey), "Invalid Base64 should return null on decryption");
    }

    @Test
    void testMultipleEncryptionsProduceDifferentResults() {
        // Due to random IV, multiple encryptions of the same data should produce different results
        String encrypted1 = aesUtils.encrypt(testData, testKey);
        String encrypted2 = aesUtils.encrypt(testData, testKey);
        
        // Results should be different
        assertNotEquals(encrypted1, encrypted2, "Multiple encryptions should produce different results due to random IV");
        
        // But both should decrypt to the same original text
        assertEquals(testData, aesUtils.decrypt(encrypted1, testKey), "First encryption should decrypt correctly");
        assertEquals(testData, aesUtils.decrypt(encrypted2, testKey), "Second encryption should decrypt correctly");
    }

    @Test
    void testSetKeyMethod() {
        // This test verifies the key generation functionality
        aesUtils.setKey(testKey);
        
        // Since secretKey is private static, we can only test it indirectly
        // by encrypting and decrypting after setting the key
        
        // Create a secure random IV for testing purposes
        String encrypted = aesUtils.encrypt(testData, testKey);
        String decrypted = aesUtils.decrypt(encrypted, testKey);
        
        assertEquals(testData, decrypted, "After setting the key, encryption and decryption should work");
    }
}