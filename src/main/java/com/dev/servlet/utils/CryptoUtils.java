package com.dev.servlet.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dev.servlet.pojo.User;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class CryptoUtils {

    private CryptoUtils() {
    }

    private static byte[] getSecurityKey() throws Exception {
        String key = PropertiesUtil.getProperty("security.encrypt.key");
        if (key == null) throw new Exception("Security key is not set");
        return key.getBytes();
    }

    private static String getSecurityAlgorithm() throws Exception {
        String key = PropertiesUtil.getProperty("security.encrypt.algorithm");
        if (key == null) throw new Exception("Cypher algorithm is not set");
        return key;
    }

    private static byte[] getJWTSecretKey() throws Exception {
        String key = PropertiesUtil.getProperty("security.jwt.key");
        if (key == null) throw new Exception("Security key is not set");
        return key.getBytes();
    }

    /**
     * Decrypt the String
     *
     * @param text
     * @return
     */
    public static String decrypt(String text) {
        try {
            String cryptherAlgorithm = getSecurityAlgorithm();
            byte[] securityKey = getSecurityKey();
            SecretKeySpec key = new SecretKeySpec(securityKey, cryptherAlgorithm);
            Cipher cipher = Cipher.getInstance(cryptherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] encrypted = Base64.getDecoder().decode(text);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt the String
     *
     * @param text
     * @return
     */
    public static String encrypt(String text) {
        try {
            String cypherAlgorithm = getSecurityAlgorithm();
            byte[] securityKey = getSecurityKey();

            SecretKeySpec key = new SecretKeySpec(securityKey, cypherAlgorithm);
            Cipher cipher = Cipher.getInstance(cypherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a JWT Token from a User
     *
     * @param user
     * @return
     */
    public static String generateJWTToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getJWTSecretKey());

            int sevenDays = 7 * 24 * 60 * 60 * 1000;

            long currentTimeMillis = System.currentTimeMillis();
            return JWT.create()
                    .withIssuer("Servlet")
                    .withSubject("Authentication")
                    .withClaim("userId", user.getId())
                    .withArrayClaim("roles", user.getPerfis().toArray(new Long[0]))
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(currentTimeMillis + sevenDays))
                    .withJWTId(UUID.randomUUID().toString())
//                    .withNotBefore(new Date(currentTimeMillis + 1000L))
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verify a JWT Token
     *
     * @param token
     * @return
     */
    public static boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getJWTSecretKey());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("Servlet")
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    /**
     * Get the user from a token
     *
     * @param token
     * @return {@link User}
     */
    public static User getUser(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        List<Long> roles = decodedJWT.getClaim("roles").asList(Long.class);

        User user = new User(userId);
        user.setPerfis(roles);
        return user;
    }
}
