/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Longs
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.bytes.ByteArrays
 */
package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CryptException;

public class Crypt {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";
    public static final String SIGNING_ALGORITHM = "SHA256withRSA";
    public static final int SIGNATURE_BYTES = 256;
    private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
    public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
    private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
    public static final String MIME_LINE_SEPARATOR = "\n";
    public static final Base64.Encoder MIME_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));
    public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Crypt.stringToRsaPublicKey(string));
        }
        catch (CryptException cryptException) {
            return DataResult.error(cryptException::getMessage);
        }
    }, Crypt::rsaPublicKeyToString);
    public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success((Object)Crypt.stringToPemRsaPrivateKey(string));
        }
        catch (CryptException cryptException) {
            return DataResult.error(cryptException::getMessage);
        }
    }, Crypt::pemRsaPrivateKeyToString);

    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
        try {
            return Crypt.digestData(string.getBytes(BYTE_ENCODING), secretKey.getEncoded(), publicKey.getEncoded());
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static byte[] digestData(byte[] ... byArray) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        for (byte[] byArray2 : byArray) {
            messageDigest.update(byArray2);
        }
        return messageDigest.digest();
    }

    private static <T extends Key> T rsaStringToKey(String string, String string2, String string3, ByteArrayToKeyFunction<T> byteArrayToKeyFunction) throws CryptException {
        int n = string.indexOf(string2);
        if (n != -1) {
            int n2 = string.indexOf(string3, n += string2.length());
            string = string.substring(n, n2 + 1);
        }
        try {
            return byteArrayToKeyFunction.apply(Base64.getMimeDecoder().decode(string));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            throw new CryptException(illegalArgumentException);
        }
    }

    public static PrivateKey stringToPemRsaPrivateKey(String string) throws CryptException {
        return Crypt.rsaStringToKey(string, PEM_RSA_PRIVATE_KEY_HEADER, PEM_RSA_PRIVATE_KEY_FOOTER, Crypt::byteToPrivateKey);
    }

    public static PublicKey stringToRsaPublicKey(String string) throws CryptException {
        return Crypt.rsaStringToKey(string, RSA_PUBLIC_KEY_HEADER, RSA_PUBLIC_KEY_FOOTER, Crypt::byteToPublicKey);
    }

    public static String rsaPublicKeyToString(PublicKey publicKey) {
        if (!ASYMMETRIC_ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Public key must be RSA");
        }
        return "-----BEGIN RSA PUBLIC KEY-----\n" + MIME_ENCODER.encodeToString(publicKey.getEncoded()) + "\n-----END RSA PUBLIC KEY-----\n";
    }

    public static String pemRsaPrivateKeyToString(PrivateKey privateKey) {
        if (!ASYMMETRIC_ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("Private key must be RSA");
        }
        return "-----BEGIN RSA PRIVATE KEY-----\n" + MIME_ENCODER.encodeToString(privateKey.getEncoded()) + "\n-----END RSA PRIVATE KEY-----\n";
    }

    private static PrivateKey byteToPrivateKey(byte[] byArray) throws CryptException {
        try {
            PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(byArray);
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            return keyFactory.generatePrivate(pKCS8EncodedKeySpec);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static PublicKey byteToPublicKey(byte[] byArray) throws CryptException {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byArray);
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] byArray) throws CryptException {
        byte[] byArray2 = Crypt.decryptUsingKey(privateKey, byArray);
        try {
            return new SecretKeySpec(byArray2, SYMMETRIC_ALGORITHM);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    public static byte[] encryptUsingKey(Key key, byte[] byArray) throws CryptException {
        return Crypt.cipherData(1, key, byArray);
    }

    public static byte[] decryptUsingKey(Key key, byte[] byArray) throws CryptException {
        return Crypt.cipherData(2, key, byArray);
    }

    private static byte[] cipherData(int n, Key key, byte[] byArray) throws CryptException {
        try {
            return Crypt.setupCipher(n, key.getAlgorithm(), key).doFinal(byArray);
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    private static Cipher setupCipher(int n, String string, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(string);
        cipher.init(n, key);
        return cipher;
    }

    public static Cipher getCipher(int n, Key key) throws CryptException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(n, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        }
        catch (Exception exception) {
            throw new CryptException(exception);
        }
    }

    static interface ByteArrayToKeyFunction<T extends Key> {
        public T apply(byte[] var1) throws CryptException;
    }

    public record SaltSignaturePair(long salt, byte[] signature) {
        public static final SaltSignaturePair EMPTY = new SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

        public SaltSignaturePair(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readLong(), friendlyByteBuf.readByteArray());
        }

        public boolean isValid() {
            return this.signature.length > 0;
        }

        public static void write(FriendlyByteBuf friendlyByteBuf, SaltSignaturePair saltSignaturePair) {
            friendlyByteBuf.writeLong(saltSignaturePair.salt);
            friendlyByteBuf.writeByteArray(saltSignaturePair.signature);
        }

        public byte[] saltAsBytes() {
            return Longs.toByteArray((long)this.salt);
        }
    }

    public static class SaltSupplier {
        private static final SecureRandom secureRandom = new SecureRandom();

        public static long getLong() {
            return secureRandom.nextLong();
        }
    }
}

