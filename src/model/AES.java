package model;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AES {
    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, new SecureRandom());
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: failed secret key generation (AES class)");
            return null;
        }
    }

    // mode = Cipher.ENCRYPT_MODE / Cipher.DECRYPT_MODE
    public static byte[] applyAES(byte[] data, SecretKey key, int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, key);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException e) {
            System.out.println("Error: failed application of the AES algorithm (AES class)");
            return null;
        }
    }
}
