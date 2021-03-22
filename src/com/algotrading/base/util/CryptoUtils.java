package com.algotrading.base.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtils {

    private static final String ALGORITHM = "HmacSHA256";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static final ThreadLocal<Mac> SHA256 = ThreadLocal.withInitial(() -> {
        try {
            return Mac.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    });

    public static String getSignature(final String apiSecret, final String message) {
        final Mac sha256 = SHA256.get();
        final Key secretKey = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        try {
            sha256.init(secretKey);
            return bytesToHex(sha256.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (final InvalidKeyException e) {
            return null;
        }
    }

    private static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /* DO not instantiate */
    private CryptoUtils() {
        throw new UnsupportedOperationException();
    }

    public static void main(final String[] args) {
        System.out.println(getSignature(
                "chNOOS4KvNXR_Xq4k4c9qsfoKWvnDecLATCRlcBwyKDYnWgO",
                "GET/api/v1/instrument1518064236"));
        // c7682d435d0cfe87c16098df34ef2eb5a549d4c5a3c2b1f0f77b8af73423bf00
        System.out.println(getSignature(
                "key",
                "The quick brown fox jumps over the lazy dog")
        );
        // f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8
    }
}
