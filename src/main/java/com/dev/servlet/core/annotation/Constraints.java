package com.dev.servlet.core.annotation;

/**
 * Annotation for defining validation constraints on parameters, fields, or methods.
 * This annotation provides a comprehensive set of validation rules that can be applied
 * to enforce data integrity and business rules.
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Validator(values = {"username"}, constraints = @Constraints(
 *     minLength = 3, 
 *     maxLength = 20, 
 *     notNullOrEmpty = true,
 *     message = "Username must be between 3 and 20 characters"
 * ))
 * public void registerUser(String username) { ... }
 * }
 * </pre>
 * 
 * @since 1.0
 * @see Validator
 */
public @interface Constraints {
    
    /**
     * Custom validation error message to display when constraints are violated.
     * 
     * @return the error message, defaults to "Invalid value"
     */
    String message() default "Invalid value";
    
    /**
     * Minimum numeric value allowed for integer/long parameters.
     * 
     * @return the minimum value, defaults to Integer.MIN_VALUE (no limit)
     */
    int min() default Integer.MIN_VALUE;
    
    /**
     * Maximum numeric value allowed for integer/long parameters.
     * 
     * @return the maximum value, defaults to Integer.MAX_VALUE (no limit)
     */
    int max() default Integer.MAX_VALUE;
    
    /**
     * Minimum string length required for string parameters.
     * 
     * @return the minimum length, defaults to 0 (no minimum)
     */
    int minLength() default 0;
    
    /**
     * Maximum string length allowed for string parameters.
     * 
     * @return the maximum length, defaults to 512
     */
    int maxLength() default 512;
    
    /**
     * Whether the value must not be null.
     * 
     * @return true if null values are not allowed, defaults to false
     */
    boolean notNull() default false;
    
    /**
     * Whether the value must not be empty (for collections and strings).
     * 
     * @return true if empty values are not allowed, defaults to false
     */
    boolean notEmpty() default false;
    
    /**
     * Whether the value must not be null or empty (combination constraint).
     * 
     * @return true if null or empty values are not allowed, defaults to false
     */
    boolean notNullOrEmpty() default false;
    
    /**
     * Whether the string value must be a valid email format.
     * 
     * @return true if email format validation is required, defaults to false
     */
    boolean isEmail() default false;
    
    /**
     * Whether the string value must be a valid date format (dd/MM/yyyy).
     * 
     * @return true if date format validation is required, defaults to false
     */
    boolean isDate() default false;
}
