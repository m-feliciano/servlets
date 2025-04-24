package com.dev.servlet.core.util;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for formatting and parsing common data types with locale-specific rules.
 * This class provides standardized methods for handling currency, dates, and other
 * formatted data types commonly used in web applications.
 * 
 * <p>The formatter uses Brazilian locale conventions by default:
 * <ul>
 *   <li>Currency values use comma as decimal separator</li>
 *   <li>Dates use dd/MM/yyyy format</li>
 *   <li>Numbers are rounded to 2 decimal places using HALF_UP rounding</li>
 * </ul>
 * 
 * <p>All methods in this class are static and null-safe, returning null when
 * provided with null input rather than throwing exceptions.
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Currency formatting
 * BigDecimal price = FormatterUtil.parseCurrency("123,45"); // 123.45
 * String formatted = FormatterUtil.toString(price); // "123,45"
 * 
 * // Date parsing
 * Date date = FormatterUtil.toDate("25/12/2023"); // December 25, 2023
 * Date invalid = FormatterUtil.toDate("invalid"); // null
 * }
 * </pre>
 * 
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FormatterUtil {
    
    /**
     * Parses a currency string in Brazilian format (comma as decimal separator)
     * into a BigDecimal with 2 decimal places.
     * 
     * @param value the currency string to parse (e.g., "123,45")
     * @return BigDecimal with 2 decimal places using HALF_UP rounding, or null if input is null
     */
    public static BigDecimal parseCurrency(String value) {
        if (value == null) return null;
        return new BigDecimal(value.replace(',', '.'))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Converts a BigDecimal to Brazilian currency format string (comma as decimal separator).
     * 
     * @param value the BigDecimal to format
     * @return formatted currency string (e.g., "123,45"), or null if input is null
     */
    public static String toString(BigDecimal value) {
        if (value == null) return null;
        return value.toString().replace('.', ',');
    }

    /**
     * Parses a date string in dd/MM/yyyy format into a Date object.
     * Uses strict parsing that does not allow lenient date interpretation.
     * 
     * @param value the date string to parse (e.g., "25/12/2023")
     * @return parsed Date object, or null if input is null or invalid format
     */
    public static Date toDate(String value) {
        if (value == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
