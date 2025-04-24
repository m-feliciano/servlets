package com.dev.servlet.core.util;
import com.dev.servlet.core.exception.ServiceException;
import lombok.NoArgsConstructor;

/**
 * Utility class for standardized exception handling and error throwing.
 * This class provides convenient methods for creating and throwing service-specific
 * exceptions with proper HTTP status codes and error messages.
 * 
 * <p>The utility centralizes exception handling patterns and ensures consistent
 * error responses across the application by using the framework's ServiceException
 * with its builder pattern for structured error information.
 * 
 * <p>Key benefits:
 * <ul>
 *   <li><strong>Standardized errors:</strong> Consistent exception structure across services</li>
 *   <li><strong>HTTP status codes:</strong> Proper status code mapping for web responses</li>
 *   <li><strong>Fluent API:</strong> Uses ServiceException builder pattern for clarity</li>
 *   <li><strong>Centralized handling:</strong> Single point for service error creation</li>
 * </ul>
 * 
 * <p>Common HTTP status codes used:
 * <ul>
 *   <li>400 - Bad Request (invalid input parameters)</li>
 *   <li>401 - Unauthorized (authentication required)</li>
 *   <li>403 - Forbidden (insufficient permissions)</li>
 *   <li>404 - Not Found (resource not found)</li>
 *   <li>409 - Conflict (resource conflict, e.g., duplicate)</li>
 *   <li>500 - Internal Server Error (unexpected system error)</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // In service methods
 * public User getUserById(Long id) {
 *     if (id == null) {
 *         ThrowableUtils.throwServiceError(400, "User ID cannot be null");
 *     }
 *     
 *     User user = userRepository.findById(id);
 *     if (user == null) {
 *         ThrowableUtils.throwServiceError(404, "User not found with ID: " + id);
 *     }
 *     
 *     return user;
 * }
 * 
 * // In controllers for authorization errors
 * if (!hasPermission(user, resource)) {
 *     ThrowableUtils.throwServiceError(403, "Insufficient permissions to access resource");
 * }
 * 
 * // For business logic violations
 * if (emailAlreadyExists(user.getEmail())) {
 *     ThrowableUtils.throwServiceError(409, "Email address already registered");
 * }
 * }
 * </pre>
 * 
 * <p>The thrown ServiceException integrates with the framework's error handling
 * mechanism to automatically convert exceptions into appropriate HTTP responses
 * with proper status codes and error messages.
 * 
 * @since 1.0
 * @see ServiceException
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ThrowableUtils {
    
    /**
     * Creates and throws a ServiceException with the specified HTTP status code and message.
     * This method uses the ServiceException builder pattern to create a structured
     * exception that can be properly handled by the framework's error handling system.
     * 
     * <p>The method always throws an exception and never returns normally.
     * It's designed to be used in validation and error handling scenarios where
     * immediate termination of the current operation is required.
     * 
     * @param statusCode the HTTP status code to associate with the error
     * @param message the error message describing the problem
     * @throws ServiceException always throws this exception with the provided status and message
     */
    public static void throwServiceError(int statusCode, String message) throws ServiceException {
        ServiceException.builder()
                .code(statusCode).message(message)
                .build()
                .throwError();
    }
}
