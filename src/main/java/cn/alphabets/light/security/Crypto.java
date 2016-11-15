package cn.alphabets.light.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Crypto
 * Created by lilin on 2016/11/15.
 */
public class Crypto {

    public static String sha256(String message, String secret) {

        String algorithm = "HmacSHA256";
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), algorithm);

        Mac mac;
        try {
            mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] macBytes = mac.doFinal(message.getBytes());

        StringBuilder sb = new StringBuilder(2 * macBytes.length);
        for (byte b : macBytes) {
            sb.append(String.format("%02x", b & 0xff));
        }

        return sb.toString();
    }

}
