package com.glt.magikoly.encrypt;

import android.util.Base64;
import com.glt.magikoly.utils.Logcat;

/**
 * Created by dengyuanting on 16-7-5.
 */
public class Signature {
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final char DELIMITER = '\n';

    private static String sign(String secret, String out) {
        byte[] digest = HmacUtils.hmacSha256(secret, out);
        String signature = Base64.encodeToString(digest, Base64.URL_SAFE);
        return signature;
}

    public static String getSign(String queryUri, String secret, String queryString, String payload) {
        Logcat.d("Signature","queryUri : " + queryUri );
        Logcat.d("Signature","secret : " + secret );
        Logcat.d("Signature","queryString : " + queryString );
        Logcat.d("Signature","payload : " + payload );

        StringBuilder valueToDigest = new StringBuilder();
        valueToDigest.append(METHOD_GET)
                .append(DELIMITER)
                .append(queryUri)
                .append(DELIMITER)
                .append(queryString)
                .append(DELIMITER)
                .append(payload);
        return sign(secret, valueToDigest.toString());
    }

    public static String postSign(String queryUri, String secret, String queryString, String payload) {
        Logcat.d("Signature","queryUri : " + queryUri );
        Logcat.d("Signature","secret : " + secret );
        Logcat.d("Signature","queryString : " + queryString );
        Logcat.d("Signature","payload : " + payload );

        StringBuilder valueToDigest = new StringBuilder();
        valueToDigest.append(METHOD_POST)
                .append(DELIMITER)
                .append(queryUri)
                .append(DELIMITER)
                .append(queryString)
                .append(DELIMITER)
                .append(payload);
        return sign(secret, valueToDigest.toString());
    }

    public static String createSignature(final String signatureKey , final String valueToDigest) {
        final byte[] digest = HmacUtils.hmacSha256(signatureKey , valueToDigest);
        return Base64.encodeToString(digest, Base64.URL_SAFE);
    }
}
