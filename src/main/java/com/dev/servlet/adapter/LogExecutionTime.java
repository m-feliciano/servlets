package com.dev.servlet.adapter;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding annotation for automatic execution time logging.
 * When applied to methods or classes, this annotation enables automatic measurement
 * and logging of execution time for performance monitoring and optimization.
 * 
 * <p>The interceptor framework uses this annotation to:
 * <ul>
 *   <li>Wrap method execution with timing logic</li>
 *   <li>Log execution duration with method details</li>
 *   <li>Support both method-level and class-level application</li>
 *   <li>Provide performance metrics for monitoring</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Controller("UserController")
 * @LogExecutionTime  // Apply to all methods in the class
 * public class UserController {
 *     
 *     public void quickMethod() {
 *         // Execution time will be logged automatically
 *     }
 *     
 *     @LogExecutionTime  // Can also be applied to specific methods
 *     public void expensiveOperation() {
 *         // This method's execution time will be logged
 *     }
 * }
 * }
 * </pre>
 * 
 * <p>When applied at the class level, all public methods in the class will have
 * their execution time logged. Method-level annotations can be used for more
 * granular control over which methods are monitored.
 * 
 * <p>Typical log output:</p>
 * <pre>
 * INFO - Method UserController.createUser executed in 145ms
 * DEBUG - Method ProductService.findById executed in 23ms
 * </pre>
 * 
 * @since 1.0
 * @see javax.interceptor.InterceptorBinding
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogExecutionTime {
}
