package com.lending.dar360UserService.user.util;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static String decrypt(String encryptedText, String secretKey) throws GeneralSecurityException {

        if (secretKey.length() != 32) {

            throw new IllegalArgumentException("Secret key must be 32 characters long for AES-256.");

        }
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes);

    }

    public static void main(String... args) throws Exception {
        String decryptedValue = decrypt("C2Usz/cY1zy9I7nFUa2bFxMDucea2uDiu9Eo6vsaOBI=", "APPRO_AES_ENCRYPTION_SECRETY_KEY");
        log.info("decryptedValue: {}", decryptedValue);
    }
}
