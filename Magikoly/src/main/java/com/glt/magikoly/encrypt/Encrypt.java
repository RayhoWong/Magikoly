package com.glt.magikoly.encrypt;

import java.nio.charset.Charset;

/**
 * Created by yuanzhiwu on 2018/5/9.
 */

public final class Encrypt {

    private Encrypt() {
    }

    public static final String ALGORITHM_DES = "DES";
    public static final String ALGORITHM_AES = "AES";
    public static final String ALGORITHM_DH = "DH";

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    public static final int DEFAULT_AES_KEY_SIZE = 128;

    public static final int DEFAULT_DH_KEY_SIZE = 1024;

    public static final String AES_CBC_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String AES_ECB_ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static final String DES_CBC_ENCRYPTION_ALGORITHM = "DES/CBC/PKCS5Padding";
    public static final String DES_ECB_ENCRYPTION_ALGORITHM = "DES/ECB/PKCS5Padding";

}
