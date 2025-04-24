package com.dev.servlet.core.annotation;

import com.dev.servlet.domain.model.enums.RequestMethod;
import com.dev.servlet.domain.model.enums.RoleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps HTTP requests to specific controller methods based on URL patterns and HTTP methods.
 * This annotation defines the routing configuration for web endpoints, including
 * authentication requirements, validation rules, and role-based access control.
 * 
 * <p>The framework uses this annotation to:
 * <ul>
 *   <li>Route incoming HTTP requests to appropriate handler methods</li>
 *   <li>Apply authentication and authorization filters</li>
 *   <li>Execute validation rules before method execution</li>
 *   <li>Support API versioning</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Controller("UserController")
 * public class UserController {
 *     
 *     @RequestMapping(
 *         value = "/users", 
 *         method = RequestMethod.GET,
 *         requestAuth = true,
 *         roles = {RoleType.ADMIN, RoleType.USER}
 *     )
 *     public  IHttpResponse<UserDTO> getUsers(Request request) {
 *         // Handle authenticated GET /users request
 *     }
 *     
 *     @RequestMapping(
 *         value = "/users", 
 *         method = RequestMethod.POST,
 *         validators = {
 *             @Validator(values = {"username"}, constraints = @Constraints(notNullOrEmpty = true)),
 *             @Validator(values = {"email"}, constraints = @Constraints(isEmail = true))
 *         },
 *         apiVersion = "v2"
 *     )
 *     public IHttpResponse<UserDTO> createUser(Request request) {
 *         // Handle validated POST /v2/users request
 *     }
 * }
 * }
 * </pre>
 * 
 * @since 1.0
 * @see Controller
 * @see Validator
 * @see RequestMethod
 * @see RoleType
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    
    /**
     * The URL pattern to match for this endpoint.
     * Supports exact matches and path variables.
     * 
     * Examples:
     * <ul>
     *   <li>"/users" - Exact match</li>
     *   <li>"/users/{id}" - Path variable</li>
     *   <li>"/api/products" - Nested path</li>
     * </ul>
     * 
     * @return the URL pattern to match
     */
    String value();
    
    /**
     * The HTTP method this endpoint responds to.
     * 
     * @return the HTTP method, defaults to GET
     * @see RequestMethod
     */
    RequestMethod method() default RequestMethod.GET;
    
    /**
     * Whether authentication is required to access this endpoint.
     * When true, requests must include valid authentication credentials.
     * 
     * @return true if authentication is required, defaults to true
     */
    boolean requestAuth() default true;
    
    /**
     * Array of validation rules to apply to request parameters.
     * Validations are executed before the method is called.
     * If any validation fails, the request is rejected with an error response.
     * 
     * @return array of validation rules, defaults to empty (no validation)
     * @see Validator
     */
    Validator[] validators() default {};
    
    /**
     * API version for this endpoint, used for versioning and routing.
     * The version is typically included in the URL path (e.g., /v1/users, /v2/users).
     * 
     * @return the API version, defaults to "v1"
     */
    String apiVersion() default "v1";
    
    /**
     * Required user roles to access this endpoint.
     * When specified, authenticated users must have at least one of these roles.
     * Empty array means no specific role requirements (any authenticated user).
     * 
     * @return array of required roles, defaults to empty (no role restriction)
     * @see RoleType
     */
    RoleType[] roles() default {};
}
