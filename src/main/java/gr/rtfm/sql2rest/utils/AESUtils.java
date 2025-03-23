package gr.rtfm.sql2rest.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AESUtils {

    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Value("${encryption.key:H3ll0SLMH0wAr3You?}")
    String stringKey;

    private static SecretKeySpec secretKey;
    private static byte[] key;

    public void setKey(String myKey) {
        try {
            key = myKey.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Use only the first 128 bits
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error("Error setting key: " + e.toString());
        }
    }

    public String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);

            // Generate a random IV
            byte[] iv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Combine IV and encrypted data
            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
            byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            log.error("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);

            // Decode Base64 and extract IV
            byte[] encryptedWithIv = Base64.getDecoder().decode(strToDecrypt);
            byte[] iv = Arrays.copyOfRange(encryptedWithIv, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(encryptedWithIv, 16, encryptedWithIv.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            log.error("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public String encrypt(String value) {
        return encrypt(value, stringKey);
    }

    public String decrypt(String value) {
        return decrypt(value, stringKey);
    }
}
