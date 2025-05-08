package gr.rtfm.sql2rest.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeystoreUtilsTest {

    @InjectMocks
    private KeystoreUtils keystoreUtils;

    @Mock
    private KeyStore mockKeyStore;

    @TempDir
    Path tempDir;

    private final String testKeystorePassword = "testPassword";
    private final String testAlias = "testAlias";

    @BeforeEach
    void setUp() {
        keystoreUtils = new KeystoreUtils();
        ReflectionTestUtils.setField(keystoreUtils, "keyStorePassword", testKeystorePassword.toCharArray());
        ReflectionTestUtils.setField(keystoreUtils, "keyStore", mockKeyStore);
    }

    @Test
    void testInitKeyStore_Success() throws Exception {
        // Create a temporary keystore file for testing
        File keystoreFile = tempDir.resolve("test-keystore.jks").toFile();
        KeyStore tempKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        tempKeyStore.load(null, testKeystorePassword.toCharArray());
        try (FileOutputStream fos = new FileOutputStream(keystoreFile)) {
            tempKeyStore.store(fos, testKeystorePassword.toCharArray());
        }

        // Setup the test instance
        KeystoreUtils localKeystoreUtils = new KeystoreUtils();
        ReflectionTestUtils.setField(localKeystoreUtils, "keystoreFilename", keystoreFile.getAbsolutePath());
        ReflectionTestUtils.setField(localKeystoreUtils, "keyStorePassword", testKeystorePassword.toCharArray());
        
        // Execute the method
        localKeystoreUtils.initKeyStore();
        
        // Verify keystore was loaded (indirectly by checking it's not null)
        assertNotNull(ReflectionTestUtils.getField(localKeystoreUtils, "keyStore"), 
                "Keystore should be initialized successfully");
    }

    @Test
    void testInitKeyStore_FileNotFound() {
        // Setup with non-existent file
        KeystoreUtils localKeystoreUtils = new KeystoreUtils();
        ReflectionTestUtils.setField(localKeystoreUtils, "keystoreFilename", "nonexistent-file.jks");
        ReflectionTestUtils.setField(localKeystoreUtils, "keyStorePassword", testKeystorePassword.toCharArray());
        
        // Execute the method - should not throw exception
        localKeystoreUtils.initKeyStore();
        
        // Verify keystore remains null after failed initialization
        assertNull(ReflectionTestUtils.getField(localKeystoreUtils, "keyStore"), 
                "Keystore should remain null after initialization failure");
    }

    @Test
    void testGetPassword_Success() throws Exception {
        // Setup mock key
        String expectedPassword = "secretPassword123";
        Key mockKey = mock(Key.class);
        when(mockKey.getEncoded()).thenReturn(expectedPassword.getBytes("UTF-8"));
        when(mockKeyStore.getKey(eq(testAlias), eq(testKeystorePassword.toCharArray()))).thenReturn(mockKey);
        
        // Execute the method
        String result = keystoreUtils.getPassword(testAlias);
        
        // Verify
        assertEquals(expectedPassword, result, "Retrieved password should match expected value");
        verify(mockKeyStore).getKey(eq(testAlias), eq(testKeystorePassword.toCharArray()));
    }

    @Test
    void testGetPassword_KeyNotFound() throws Exception {
        // Setup mock to throw exception when key not found
        when(mockKeyStore.getKey(eq(testAlias), any())).thenThrow(new java.security.KeyStoreException("Key not found"));
        
        // Execute the method
        String result = keystoreUtils.getPassword(testAlias);
        
        // Verify
        assertNull(result, "Result should be null when key not found");
    }

    @Test
    void testGetPassword_NullKeystore() {
        // Setup with null keystore
        ReflectionTestUtils.setField(keystoreUtils, "keyStore", null);
        
        // Execute the method
        String result = keystoreUtils.getPassword(testAlias);
        
        // Verify
        assertNull(result, "Result should be null when keystore is null");
    }

    @Test
    void testGetPassword_NullAlias() throws Exception {
        // Execute the method with null alias
        String result = keystoreUtils.getPassword(null);
        
        // Verify
        assertNull(result, "Result should be null when alias is null");
    }
}