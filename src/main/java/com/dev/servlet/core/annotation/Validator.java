package com.dev.servlet.core.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines validation rules for method parameters in controller endpoints.
 * This annotation specifies which request parameters to validate and what
 * constraints to apply, enabling automatic input validation before method execution.
 * 
 * <p>The validation framework processes these annotations to:
 * <ul>
 *   <li>Extract specified parameters from HTTP requests</li>
 *   <li>Apply defined constraints (length, format, nullability, etc.)</li>
 *   <li>Throw specified exceptions when validation fails</li>
 *   <li>Pass validated parameters to controller methods</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @RequestMapping(
 *     value = "/users", 
 *     method = RequestMethod.POST,
 *     validators = {
 *         @Validator(
 *             values = {"username", "email"}, 
 *             constraints = @Constraints(
 *                 notNullOrEmpty = true,
 *                 minLength = 3,
 *                 maxLength = 50,
 *                 message = "Username and email are required"
 *             ),
 *             exception = ValidationException.class
 *         ),
 *         @Validator(
 *             values = {"email"}, 
 *             constraints = @Constraints(
 *                 isEmail = true,
 *                 message = "Invalid email format"
 *             )
 *         )
 *     }
 * )
 * public void createUser(Request request) {
 *     // Method receives validated parameters
 * }
 * }
 * </pre>
 * 
 * <p>Multiple validators can be applied to the same parameter for complex validation rules.
 * Each validator is processed independently, and all must pass for the request to proceed.
 * 
 * @since 1.0
 * @see Constraints
 * @see RequestMapping
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {
    
    /**
     * Names of request parameters to validate.
     * These parameter names must match exactly the parameter names in the HTTP request
     * (form data, query parameters, or JSON body fields).
     * 
     * Multiple parameter names can be specified to apply the same validation
     * rules to multiple parameters.
     * 
     * @return array of parameter names to validate
     */
    String[] values();
    
    /**
     * Validation constraints to apply to the specified parameters.
     * These constraints define the specific validation rules such as
     * length limits, format requirements, and nullability checks.
     * 
     * @return array of constraint rules, defaults to empty constraints
     * @see Constraints
     */
    Constraints[] constraints() default @Constraints();
    
    /**
     * Exception class to throw when validation fails.
     * The specified exception will be instantiated and thrown with the
     * constraint's error message when any validation rule fails.
     * 
     * The exception class must have a constructor that accepts a String message.
     * 
     * @return exception class to throw on validation failure, defaults to RuntimeException
     */
    Class<? extends Throwable> exception() default RuntimeException.class;
}
