package com.glt.magikoly.encrypt;

import com.gomo.commons.security.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class FaceDesUtils {

    public static final String FACE_DES_KEY = Base64.encodeBase64URLSafeString("FCSRCT18".getBytes(StandardCharsets.UTF_8));
    public static final String FLASH_AD_DES_KEY = Base64.encodeBase64URLSafeString("YN5HLTT6".getBytes(StandardCharsets.UTF_8));
    public static final String FEEDBACK_DES_KEY = Base64.encodeBase64URLSafeString("K8N9X68T".getBytes(StandardCharsets.UTF_8));
    public static final String VISION_DES_KEY = "8fT4khVbUgE";
    /**
     * 随机生成DES密钥并转换成base64字符串。
     *
     * @return DES密钥
     * @throws NoSuchAlgorithmException DES算法不支持时抛出
     * @see #generate(String)
     */
    public static String generate() throws NoSuchAlgorithmException {
        return generate(null);
    }

    /**
     * 根据指定安全种子生成DES密钥并转换成Base64字符串。
     *
     * @param seed 安全种子，可以使用随机字符串，用户标识...
     * @return DES密钥
     * @throws NoSuchAlgorithmException DES算法不支持时抛出
     * @see #generate()
     */
    public static String generate(final String seed) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = null;
        if (seed != null) {
            secureRandom = new SecureRandom(seed.getBytes());
        } else {
            secureRandom = new SecureRandom();
        }
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(Encrypt.ALGORITHM_DES);
        keyGenerator.init(secureRandom);
        final SecretKey secretKey = keyGenerator.generateKey();
        return Base64.encodeBase64URLSafeString(secretKey.getEncoded());
    }

    private static Key getKey(final byte[] key) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
        DESKeySpec desKey = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(Encrypt.ALGORITHM_DES);
        return keyFactory.generateSecret(desKey);
    }

    private static byte[] getUTF8Bytes(final String text, final boolean shouldDecode) {
        byte[] cipherBytes = null;
        if (shouldDecode/* && Base64.isBase64(text)*/) {
            cipherBytes = Base64.decodeBase64(text);
        } else {
            cipherBytes = text.getBytes(StandardCharsets.UTF_8);
        }

        return cipherBytes;
    }

    /**
     * 使用DES算法加密数据。
     *
     * @param clearText 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(byte[], String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(String, String)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static byte[] encrypt(final String clearText, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (clearText == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }

        return encrypt(getUTF8Bytes(clearText, false), key);
    }

    /**
     * 使用DES算法加密数据。
     *
     * @param clearBytes 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(String, String)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static byte[] encrypt(final byte[] clearBytes, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (clearBytes == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Specified key must not be null");
        }

        final Key secretKey = getKey(getUTF8Bytes(key, true));
        final Cipher cipher = Cipher.getInstance(Encrypt.DES_ECB_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(clearBytes);

    }

    /**
     * 使用DES算法加密数据，在转换成字符串时强制使用UTF-8编码。
     *
     * @param clearText 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encrypt(byte[], String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(String, String)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static String encryptToString(final String clearText, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (clearText == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }

        return encryptToString(getUTF8Bytes(clearText, false), key);
    }

    /**
     * 使用DES算法加密数据，在转换成字符串时强制使用UTF-8编码。
     *
     * @param clearBytes 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encrypt(byte[], String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(String, String)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static String encryptToString(final byte[] clearBytes, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encryptToString(clearBytes, key, StandardCharsets.UTF_8);
    }

    /**
     * 使用DES算法加密数据，在转换成字符串时强制使用指定编码。
     *
     * @param clearBytes 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @param charset 转换成字符串使用的编码
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encrypt(byte[], String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToBase64URLSafeString(String, String)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static String encryptToString(final byte[] data, final String key, final Charset charset)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (charset == null) {
            throw new IllegalArgumentException("Specified charset must not be null");
        }

        return new String(encrypt(data, key), charset);
    }

    /**
     * 使用DES算法加密数据，在转换成字符串时强制使用Base64编码。
     *
     * @param clearText 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encrypt(byte[], String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(byte[], String)
     */
    public static String encryptToBase64URLSafeString(final String clearText, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (clearText == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }

        return encryptToBase64URLSafeString(getUTF8Bytes(clearText, false), key);
    }

    /**
     * 使用DES算法加密数据，在转换成字符串时强制使用Base64编码。
     *
     * @param clearBytes 需要加密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 加密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #encrypt(String, String)
     * @see #encrypt(byte[], String)
     * @see #encryptToString(String, String)
     * @see #encryptToString(byte[], String)
     * @see #encryptToString(byte[], String, Charset)
     * @see #encryptToBase64URLSafeString(String, String)
     */
    public static String encryptToBase64URLSafeString(final byte[] clearBytes, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return Base64.encodeBase64URLSafeString(encrypt(clearBytes, key));
    }

    /**
     * 使用DES算法解密数据。
     *
     * @param cipherText 需要解密的数据，支持Base64编码数据
     * @param key DES密钥，支持Base64编码格式
     * @return 解密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #decrypt(byte[], String)
     * @see #decryptToString(String, String)
     * @see #decryptToString(byte[], String)
     * @see #decryptToString(byte[], String, Charset)
     */
    public static byte[] decrypt(final String cipherText, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (cipherText == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }

        return decrypt(getUTF8Bytes(cipherText, true), key);
    }

    /**
     * 使用DES算法解密数据。
     *
     * @param cipherBytes 需要解密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 解密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #decrypt(String, String)
     * @see #decrypt(byte[], String)
     * @see #decryptToString(String, String)
     * @see #decryptToString(byte[], String, Charset)
     */
    public static byte[] decrypt(final byte[] cipherBytes, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (cipherBytes == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Specified key must not be null");
        }

        final Key secretKey = getKey(getUTF8Bytes(key, true));
        final Cipher cipher = Cipher.getInstance(Encrypt.DES_ECB_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(cipherBytes);
    }

    /**
     * 使用DES算法解密数据，在转换成字符串时强制使用UTF-8编码。
     *
     * @param cipherText 需要解密的数据，支持Base64编码数据
     * @param key DES密钥，支持Base64编码格式
     * @return 解密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #decrypt(String, String)
     * @see #decrypt(byte[], String)
     * @see #decryptToString(byte[], String)
     * @see #decryptToString(byte[], String, Charset)
     */
    public static String decryptToString(final String cipherText, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (cipherText == null) {
            throw new IllegalArgumentException("Specified data must not be null");
        }

        return decryptToString(getUTF8Bytes(cipherText, true), key, StandardCharsets.UTF_8);
    }

    /**
     * 使用DES算法解密数据，在转换成字符串时强制使用UTF-8编码。
     *
     * @param cipherBytes 需要解密的数据
     * @param key DES密钥，支持Base64编码格式
     * @return 解密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #decrypt(String, String)
     * @see #decrypt(byte[], String)
     * @see #decryptToString(String, String)
     * @see #decryptToString(byte[], String, Charset)
     */
    public static String decryptToString(final byte[] cipherBytes, final String key)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return decryptToString(cipherBytes, key, StandardCharsets.UTF_8);

    }

    /**
     * 使用DES算法解密数据，在转换成字符串时强制使用指定编码。
     *
     * @param clearBytes 需要解密的数据
     * @param key DES密钥，支持Base64编码格式
     * @param charset 转换成字符串使用的编码
     * @return 解密后的数据
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @see #decrypt(String, String)
     * @see #decrypt(byte[], String)
     * @see #decryptToString(String, String)
     * @see #decryptToString(byte[], String)
     */
    public static String decryptToString(final byte[] cipherBytes, final String key, final Charset charset)
            throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (charset == null) {
            throw new IllegalArgumentException("Specified charset must not be null");
        }

        return new String(decrypt(cipherBytes, key), charset);
    }
}
