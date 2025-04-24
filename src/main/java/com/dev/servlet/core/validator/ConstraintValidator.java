package com.dev.servlet.core.validator;
import com.dev.servlet.core.annotation.Constraints;
import com.dev.servlet.core.util.ClassUtil;
import java.util.ArrayList;
import java.util.List;

public final class ConstraintValidator {
    public static final String EMAIL_REGEXP = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    public static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private final Constraints[] constraints;
    public ConstraintValidator(Constraints[] constraints) {
        this.constraints = constraints;
    }

    public List<String> validate(Object valueToCheck) {
        List<String> errors = new ArrayList<>();
        for (Constraints constraint : constraints) {
            if (!validateNotNull(constraint, valueToCheck)) {
                errors.add(constraint.message());
            }
            if (!validateNotEmpty(constraint, valueToCheck)) {
                errors.add(constraint.message());
            }
            if (!validateEmail(constraint, valueToCheck)) {
                errors.add(constraint.message());
            }
            if (!validateDate(constraint, valueToCheck)) {
                errors.add(constraint.message());
            }
            if (!validateRange(constraint, valueToCheck)) {
                String message = constraint.message();
                message = message.replace("{0}", String.valueOf(constraint.min()));
                message = message.replace("{1}", String.valueOf(constraint.max()));
                errors.add(message);
            }
            if (!validateLength(constraint, valueToCheck)) {
                String message = constraint.message();
                message = message.replace("{0}", String.valueOf(constraint.minLength()));
                message = message.replace("{1}", String.valueOf(constraint.maxLength()));
                errors.add(message);
            }
        }
        return errors;
    }

    private boolean validateNotNull(Constraints constraint, Object value) {
        return !(value == null && constraint.notNull());
    }

    private boolean validateNotEmpty(Constraints constraint, Object value) {
        return !(constraint.notEmpty() && value instanceof String string && string.isEmpty());
    }

    private boolean validateEmail(Constraints constraint, Object value) {
        return !(constraint.isEmail() && !(value instanceof String string && string.matches(EMAIL_REGEXP)));
    }

    private boolean validateDate(Constraints constraint, Object value) {
        return !(constraint.isDate() && !(value instanceof String string && string.matches(DATE_REGEX)));
    }

    private boolean validateRange(Constraints constraint, Object value) {
        if (constraint.min() == Integer.MIN_VALUE && constraint.max() == Integer.MAX_VALUE) {
            return true;
        }
        try {
            Double castedValue = ClassUtil.castWrapper(Double.class, value);
            return castedValue != null && castedValue >= constraint.min() && castedValue <= constraint.max();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateLength(Constraints constraint, Object value) {
        if (value instanceof String string) {
            int length = string.length();
            return length >= constraint.minLength() && length <= constraint.maxLength();
        } else if (value instanceof byte[] bytes) {
            int length = bytes.length;
            return length >= constraint.minLength() && length <= constraint.maxLength();
        }
        return true;
    }
}
