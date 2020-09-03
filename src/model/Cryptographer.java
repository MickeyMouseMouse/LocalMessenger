package model;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Cryptographer {
    private final KeyPair keyPair;
    private final SecretKey secretKey;

    private PublicKey interlocutorPublicKey;
    private SecretKey interlocutorSecretKey;

    Cryptographer() {
        keyPair = RSA.generateKeyPair();
        secretKey = AES.generateSecretKey();
    }

    public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }

    public void setInterlocutorPublicKey(byte[] data) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            interlocutorPublicKey = kf.generatePublic(new X509EncodedKeySpec(data));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error: failed public key generation (Cryptographer class)");
        }
    }

    public byte[] getEncodedSecretKey() {
        return RSA.encrypt(secretKey.getEncoded(), interlocutorPublicKey);
    }

    public void setInterlocutorSecretKey(byte[] data) {
        byte[] secretKeyBytes = RSA.decrypt(data, keyPair.getPrivate());
        if (secretKeyBytes == null) return;
        interlocutorSecretKey = new SecretKeySpec(secretKeyBytes, "AES");
    }

    public byte[] encode(byte[] data) {
        return AES.applyAES(data, secretKey, Cipher.ENCRYPT_MODE);
    }

    public byte[] decode(byte[] data) {
        return AES.applyAES(data, interlocutorSecretKey, Cipher.DECRYPT_MODE);
    }
}
