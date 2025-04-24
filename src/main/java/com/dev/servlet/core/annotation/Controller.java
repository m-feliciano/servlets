package com.dev.servlet.core.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a controller component in the servlet framework.
 * Classes annotated with {@code @Controller} are automatically detected and registered
 * as HTTP request handlers during application startup.
 * 
 * <p>The controller annotation enables the framework to:
 * <ul>
 *   <li>Register the class as a servlet endpoint</li>
 *   <li>Apply automatic dependency injection</li>
 *   <li>Enable request mapping discovery</li>
 *   <li>Apply security and validation filters</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Controller("UserController")
 * public class UserController {
 *     
 *     @RequestMapping(value = "/users", method = RequestMethod.GET)
 *     public void getUsers(Request request, IHttpResponse<UserDTO> response) {
 *         // Handle GET /users request
 *     }
 * }
 * }
 * </pre>
 * 
 * <p>The value provided becomes the controller's identifier used for:
 * <ul>
 *   <li>Bean resolution and dependency injection</li>
 *   <li>Security configuration and authorization</li>
 *   <li>Request routing and dispatcher mapping</li>
 * </ul>
 * 
 * @since 1.0
 * @see RequestMapping
 * @see com.dev.servlet.core.util.BeanUtil
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    
    /**
     * The controller identifier used for registration and bean resolution.
     * This value should be unique across the application and typically
     * matches the class name.
     * 
     * @return the controller identifier name
     */
    String value();
}
