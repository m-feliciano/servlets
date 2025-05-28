package com.dev.servlet.util;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FormatterUtil {

    /**
     * This method is used to format a string to a currency value. The value is
     * formatted to 2 decimal places. The value is rounded to the nearest integer.
     * The value is formatted to a currency format.
     *
     * @param value - the string to be converted
     * @return String with currency format
     */

    public static BigDecimal parseCurrency(String value) {
        if (value == null) return null;

        return new BigDecimal(value.replace(',', '.'))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formats a currency value to a string.
     *
     * @param value The value to format.
     * @return The formatted value.
     */
    public static String toString(BigDecimal value) {
        if (value == null) return null;

        return value.toString().replace('.', ',');
    }

    /**
     * Formats a string to a date.
     *
     * @param value The value to format.
     * @return The formatted value.
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
