package com.dev.servlet.core.util;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dev.servlet.domain.transfer.dto.UserDTO;
import com.dev.servlet.domain.model.User;
import lombok.NoArgsConstructor;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive cryptographic utility class providing JWT token management and symmetric encryption.
 * This class handles secure authentication token generation, validation, and data encryption/decryption
 * using industry-standard algorithms and best practices.
 * 
 * <p>Key security features:
 * <ul>
 *   <li><strong>JWT Authentication:</strong> Secure token generation with HMAC-256 signing</li>
 *   <li><strong>Token Validation:</strong> Comprehensive token verification with expiration checks</li>
 *   <li><strong>Symmetric Encryption:</strong> Configurable cipher algorithms for data protection</li>
 *   <li><strong>User Context:</strong> Token-based user information extraction</li>
 *   <li><strong>Configuration-driven:</strong> Security keys and algorithms from properties</li>
 * </ul>
 * 
 * <p>JWT token structure includes:
 * <ul>
 *   <li>User ID and role information</li>
 *   <li>Issuer identification ("Servlet")</li>
 *   <li>7-day expiration period</li>
 *   <li>Unique JWT ID for tracking</li>
 *   <li>HMAC-256 signature for integrity</li>
 * </ul>
 * 
 * <p>Required properties configuration:
 * <ul>
 *   <li>{@code security.encrypt.key} - Symmetric encryption key</li>
 *   <li>{@code security.encrypt.algorithm} - Cipher algorithm (e.g., "AES")</li>
 *   <li>{@code security.jwt.key} - JWT signing secret key</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // JWT Token Operations
 * UserDTO user = new UserDTO(123L, Arrays.asList(1L, 2L)); // user with roles
 * String token = CryptoUtils.generateJwtToken(user);
 * 
 * // Token validation
 * boolean valid = CryptoUtils.isValidToken(token);
 * if (valid) {
 *     User authenticatedUser = CryptoUtils.getUser(token);
 *     // Process authenticated request
 * }
 * 
 * // Data encryption
 * String sensitive = "confidential data";
 * String encrypted = CryptoUtils.encrypt(sensitive);
 * String decrypted = CryptoUtils.decrypt(encrypted);
 * }
 * </pre>
 * 
 * <p><strong>Security Notes:</strong>
 * <ul>
 *   <li>Ensure secure key generation and storage</li>
 *   <li>Use strong, randomly generated secret keys</li>
 *   <li>Rotate keys regularly for enhanced security</li>
 *   <li>Never expose secret keys in logs or client-side code</li>
 *   <li>Validate all tokens before processing requests</li>
 * </ul>
 * 
 * @since 1.0
 * @see PropertiesUtil
 * @see UserDTO
 * @see User
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CryptoUtils {
    
    /** JWT token expiration period: 7 days in milliseconds */
    public static final int SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * Retrieves the symmetric encryption key from configuration.
     * 
     * @return the encryption key as byte array
     * @throws Exception if the security key is not configured
     */
    private static byte[] getSecurityKey() throws Exception {
        String key = PropertiesUtil.getProperty("security.encrypt.key");
        if (key == null) throw new Exception("Security key is not set");
        return key.getBytes();
    }

    /**
     * Retrieves the encryption algorithm from configuration.
     * 
     * @return the cipher algorithm name (e.g., "AES")
     * @throws Exception if the algorithm is not configured
     */
    private static String getSecurityAlgorithm() throws Exception {
        String key = PropertiesUtil.getProperty("security.encrypt.algorithm");
        if (key == null) throw new Exception("Cypher algorithm is not set");
        return key;
    }

    /**
     * Retrieves the JWT signing secret key from configuration.
     * 
     * @return the JWT secret key as byte array
     * @throws Exception if the JWT key is not configured
     */
    private static byte[] getJwtSecretKey() throws Exception {
        String key = PropertiesUtil.getProperty("security.jwt.key");
        if (key == null) throw new Exception("Security key is not set");
        return key.getBytes();
    }

    /**
     * Decrypts a Base64-encoded encrypted text using symmetric encryption.
     * Uses the configured cipher algorithm and security key.
     * 
     * @param text the Base64-encoded encrypted text
     * @return the decrypted plain text
     * @throws RuntimeException if decryption fails or configuration is invalid
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
     * Encrypts plain text using symmetric encryption and Base64 encoding.
     * Uses the configured cipher algorithm and security key.
     * 
     * @param text the plain text to encrypt
     * @return the Base64-encoded encrypted text
     * @throws RuntimeException if encryption fails or configuration is invalid
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
     * Generates a JWT authentication token for a user with embedded role information.
     * The token includes user ID, roles, expiration, and unique identifier.
     * 
     * <p>Token claims:
     * <ul>
     *   <li><code>userId</code> - User's unique identifier</li>
     *   <li><code>roles</code> - Array of user role IDs</li>
     *   <li><code>iss</code> - Issuer ("Servlet")</li>
     *   <li><code>sub</code> - Subject ("Authentication")</li>
     *   <li><code>iat</code> - Issued at timestamp</li>
     *   <li><code>exp</code> - Expiration timestamp (7 days)</li>
     *   <li><code>jti</code> - Unique JWT ID</li>
     * </ul>
     * 
     * @param user the user DTO containing ID and roles
     * @return signed JWT token string
     * @throws RuntimeException if token generation fails or JWT key is invalid
     */
    public static String generateJwtToken(UserDTO user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getJwtSecretKey());
            long currentTimeMillis = System.currentTimeMillis();
            return JWT.create()
                    .withIssuer("Servlet")
                    .withSubject("Authentication")
                    .withClaim("userId", user.getId())
                    .withArrayClaim("roles", user.getPerfis().toArray(new Long[0]))
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(currentTimeMillis + SEVEN_DAYS))
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates a JWT token's signature, issuer, and expiration.
     * Performs comprehensive verification including:
     * <ul>
     *   <li>Null and empty token checks</li>
     *   <li>Signature verification with HMAC-256</li>
     *   <li>Issuer validation</li>
     *   <li>Expiration time validation</li>
     * </ul>
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(getJwtSecretKey());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("Servlet").build();
            DecodedJWT verified = verifier.verify(token);
            return verified.getExpiresAt() != null && !verified.getExpiresAt().before(new Date());
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Extracts user information from a JWT token without verification.
     * This method decodes the token payload to reconstruct the user context
     * including ID, roles, and the original token.
     * 
     * <p><strong>Security Warning:</strong> This method does not verify the token.
     * Always call {@link #isValidToken(String)} before using this method
     * to ensure the token is authentic and not expired.
     * 
     * @param token the JWT token to decode
     * @return User object with ID, roles, and token information
     * @throws RuntimeException if token decoding fails
     */
    public static User getUser(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        List<Long> roles = decodedJWT.getClaim("roles").asList(Long.class);
        User user = new User(userId);
        user.setPerfis(roles);
        user.setToken(token);
        return user;
    }
}
