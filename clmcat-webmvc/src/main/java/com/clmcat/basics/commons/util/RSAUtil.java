package com.clmcat.basics.commons.util;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * RSA 工具类（支持 2048位 + SHA256withRSA / RS256）
 * <p>
 * 功能：
 * - 生成 2048 位 RSA 密钥对
 * - 公钥加密、私钥解密（支持长文本分段）
 * - SHA256withRSA 签名与验签
 * </p>
 *
 * @author zxy & (optimized)
 */
public final class RSAUtil {

    // ====================== RSA 2048 正确分段常量 ======================
    /**
     * 2048位RSA最大加密明文大小（PKCS1Padding 占用11字节，256 - 11 = 245）
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;

    /**
     * 2048位RSA最大解密密文大小（密钥长度字节数 2048/8 = 256）
     */
    private static final int MAX_DECRYPT_BLOCK = 256;

    // 标准算法名称
    private static final String RSA_ALGORITHM = "RSA";
    /**
     * 签名算法：对应 JWT 的 RS256
     */
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    /**
     * Cipher 完整算法（保证跨平台兼容性）
     */
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private RSAUtil() {
        // 工具类禁止实例化
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 生成 RSA 2048 位密钥对
     *
     * @return KeyPair 密钥对
     * @throws RuntimeException 若算法不支持或初始化失败
     */
    public static KeyPair getKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA algorithm not available", e);
        }
    }

    /**
     * 从 Base64 字符串加载私钥（PKCS#8 格式）
     *
     * @param privateKey Base64 编码的私钥字符串
     * @return PrivateKey 对象
     * @throws NullPointerException 若 privateKey 为 null
     * @throws RuntimeException     若解码或密钥解析失败
     */
    public static PrivateKey getPrivateKey(String privateKey) {
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            byte[] decodedKey = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            // 校验密钥长度（仅当是 RSAPrivateKey 实例时）
            validateKeyLength(key);
            return key;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    /**
     * 从 Base64 字符串加载公钥（X.509 格式）
     *
     * @param publicKey Base64 编码的公钥字符串
     * @return PublicKey 对象
     * @throws NullPointerException 若 publicKey 为 null
     * @throws RuntimeException     若解码或密钥解析失败
     */
    public static PublicKey getPublicKey(String publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            byte[] decodedKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            PublicKey key = keyFactory.generatePublic(keySpec);
            validateKeyLength(key);
            return key;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    /**
     * 校验密钥是否为 2048 位 RSA（若密钥实现了 RSAPublicKey/RSAPrivateKey 接口）
     *
     * @param key 公钥或私钥
     * @throws RuntimeException 如果密钥长度不是 2048 位
     */
    private static void validateKeyLength(Key key) {
        if (key instanceof RSAPublicKey) {
            int keySize = ((RSAPublicKey) key).getModulus().bitLength();
            if (keySize != 2048) {
                throw new RuntimeException("Unsupported RSA key size: " + keySize + ". Only 2048-bit is supported.");
            }
        } else if (key instanceof RSAPrivateKey) {
            int keySize = ((RSAPrivateKey) key).getModulus().bitLength();
            if (keySize != 2048) {
                throw new RuntimeException("Unsupported RSA key size: " + keySize + ". Only 2048-bit is supported.");
            }
        }
        // 非 RSA Key（理论上不会发生）忽略校验
    }

    /**
     * RSA 公钥加密（支持长文本分段）
     *
     * @param data      原文（UTF-8 编码）
     * @param publicKey 公钥（必须为 2048 位 RSA）
     * @return Base64 编码的密文
     * @throws NullPointerException 若 data 或 publicKey 为 null
     * @throws RuntimeException     若加密失败
     */
    public static String encrypt(String data, PublicKey publicKey) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        validateKeyLength(publicKey);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            int inputLen = dataBytes.length;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int offset = 0;
                while (offset < inputLen) {
                    int blockSize = Math.min(inputLen - offset, MAX_ENCRYPT_BLOCK);
                    byte[] encryptedBlock = cipher.doFinal(dataBytes, offset, blockSize);
                    out.write(encryptedBlock);
                    offset += blockSize;
                }
                return Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("RSA encryption failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during encryption", e);
        }
    }

    /**
     * RSA 公钥加密（支持长文本分段）
     *
     * @param dataBytes      原文字节数组
     * @param publicKey 公钥（必须为 2048 位 RSA）
     * @return Base64 编码的密文
     * @throws NullPointerException 若 data 或 publicKey 为 null
     * @throws RuntimeException     若加密失败
     */
    public static byte[] encrypt(byte[] dataBytes, PublicKey publicKey) {
        Objects.requireNonNull(dataBytes, "data must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        validateKeyLength(publicKey);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLen = dataBytes.length;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int offset = 0;
                while (offset < inputLen) {
                    int blockSize = Math.min(inputLen - offset, MAX_ENCRYPT_BLOCK);
                    byte[] encryptedBlock = cipher.doFinal(dataBytes, offset, blockSize);
                    out.write(encryptedBlock);
                    offset += blockSize;
                }
                return out.toByteArray();
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("RSA encryption failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during encryption", e);
        }
    }


    /**
     * RSA 私钥解密（字符串密文）
     *
     * @param ciphertext Base64 编码的密文
     * @param privateKey 私钥（必须为 2048 位 RSA）
     * @return 解密后的明文字符串（UTF-8）
     * @throws NullPointerException 若 ciphertext 或 privateKey 为 null
     * @throws RuntimeException     若解密失败
     */
    public static String decrypt(String ciphertext, PrivateKey privateKey) {
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        validateKeyLength(privateKey);

        byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
        byte[] decryptedBytes = decryptBytes(cipherBytes, privateKey);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }



    /**
     * RSA 私钥解密（字节数组密文）
     *
     * @param cipherData 密文字节数组
     * @param privateKey 私钥
     * @return 解密后的明文字节数组
     * @throws RuntimeException 若解密失败
     */
    public static byte[] decryptBytes(byte[] cipherData, PrivateKey privateKey) {
        Objects.requireNonNull(cipherData, "cipherData must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        validateKeyLength(privateKey);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int inputLen = cipherData.length;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                int offset = 0;
                while (offset < inputLen) {
                    int blockSize = Math.min(inputLen - offset, MAX_DECRYPT_BLOCK);
                    byte[] decryptedBlock = cipher.doFinal(cipherData, offset, blockSize);
                    out.write(decryptedBlock);
                    offset += blockSize;
                }
                return out.toByteArray();
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("RSA decryption failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during decryption", e);
        }
    }

    /**
     * SHA256withRSA 签名
     *
     * @param data       待签名的原文
     * @param privateKey 私钥
     * @return Base64 编码的签名值
     * @throws NullPointerException 若 data 或 privateKey 为 null
     * @throws RuntimeException     若签名失败
     */
    public static String sign(String data, PrivateKey privateKey) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        validateKeyLength(privateKey);

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("RSA signing failed", e);
        }
    }

    /**
     * SHA256withRSA 验签
     *
     * @param data      原始数据（未签名）
     * @param publicKey 公钥
     * @param sign      Base64 编码的签名值
     * @return true 签名有效，false 无效
     * @throws NullPointerException 若任一参数为 null
     * @throws RuntimeException     若验签过程异常（非签名无效）
     */
    public static boolean verify(String data, PublicKey publicKey, String sign) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        Objects.requireNonNull(sign, "sign must not be null");
        validateKeyLength(publicKey);

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("RSA verification failed", e);
        }
    }

    // ====================== 测试入口 ======================
    public static void main(String[] args) {
        try {
            // 生成 2048 位密钥对
            KeyPair keyPair = getKeyPair();
            String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            System.out.println("私钥: " + privateKeyBase64);
            System.out.println("公钥: " + publicKeyBase64);

            // 加解密测试
            String originalText = "123秘密啊RSA长文本测试...这里是一段超过245字节的文本，用来验证分段加密是否正常。" +
                    "实际上2048位RSA每次只能加密245字节，所以长文本会被自动分段加密，然后分段解密，确保数据完整。" +
                    "这段文字已经足够长，用来验证分段逻辑的正确性。";
            System.out.println("\n原文: " + originalText);

            PublicKey publicKey = getPublicKey(publicKeyBase64);
            PrivateKey privateKey = getPrivateKey(privateKeyBase64);

            String encrypted = encrypt(originalText, publicKey);
            System.out.println("加密后: " + encrypted);

            String decrypted = decrypt(encrypted, privateKey);
            System.out.println("解密后: " + decrypted);

            System.out.println("\n加解密一致性: " + originalText.equals(decrypted));

            // 签名验签测试
            String sign = sign(originalText, privateKey);
            System.out.println("签名: " + sign);

            boolean verified = verify(originalText, publicKey, sign);
            System.out.println("验签结果: " + verified);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}