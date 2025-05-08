package gr.rtfm.sql2rest.utils;

import java.io.FileInputStream;
import java.security.KeyStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class KeystoreUtils {
    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    
    @Value("${keystore.filename:mykeystore.jks}")
    String keystoreFilename;

    private KeyStore keyStore;
 
    @Value("${keystore.password:changeit}")
    private char[] keyStorePassword;
 
    /**
     * Loads the key store from the file specified by the "keystore.filename"
     * property. If not set, the default value is "mykeystore.jks".
     *
     * The keystore is loaded using the password specified by the
     * "keystore.password" property. If not set, the default value is also
     * "changeit".
     *
     * @throws Exception
     *             if the keystore cannot be loaded
     */
    @PostConstruct
    public void initKeyStore()
    {
        try {
            String keystoreType = KeyStore.getDefaultType();
            log.info("Loading keystore of type {} from file {}", keystoreType, keystoreFilename);
            keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(new FileInputStream(keystoreFilename), keyStorePassword);
            log.info("Keystore loaded successfully");
        } catch (Exception e) {
            log.error("Error loading keystore", e);
            keyStore = null;
        }
    }
 
    /**
     * Retrieves a password from the keystore by alias.
     *
     * @param alias
     *            the alias to use for retrieving the password
     * @return the password if found, otherwise null
     */
    public String getPassword(String alias)
    {
        log.info("Getting password for alias {}", alias);
        try {
 
            byte[] passwordFromKeystore = keyStore.getKey(alias, keyStorePassword).getEncoded();
            // convert byte[] to String encoded in UTF-8
            return new String(passwordFromKeystore, "UTF-8");
        } catch (Exception e) {
            log.error("Error getting password for alias {}", alias, e);
            return null;
        }
    }


}
