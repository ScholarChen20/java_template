package com.example.yoyo_data.util;

import com.example.yoyo_data.common.exception.ValidationException;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    private static final Pattern IP_PATTERN = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ValidationException(message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new ValidationException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new ValidationException(message);
        }
    }

    public static void isEmpty(String str, String message) {
        if (StringUtils.hasText(str)) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static void isEmpty(Collection<?> collection, String message) {
        if (collection != null && !collection.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static void isEmpty(Map<?, ?> map, String message) {
        if (map != null && !map.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new ValidationException(message);
        }
    }

    public static void isEmpty(Object[] array, String message) {
        if (array != null && array.length > 0) {
            throw new ValidationException(message);
        }
    }

    public static void isEmail(String email, String message) {
        notEmpty(email, message);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void isPhone(String phone, String message) {
        notEmpty(phone, message);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void isUsername(String username, String message) {
        notEmpty(username, message);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void isUrl(String url, String message) {
        notEmpty(url, message);
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void isIp(String ip, String message) {
        notEmpty(ip, message);
        if (!IP_PATTERN.matcher(ip).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void minLength(String str, int min, String message) {
        notNull(str, message);
        if (str.length() < min) {
            throw new ValidationException(message);
        }
    }

    public static void maxLength(String str, int max, String message) {
        notNull(str, message);
        if (str.length() > max) {
            throw new ValidationException(message);
        }
    }

    public static void length(String str, int min, int max, String message) {
        notNull(str, message);
        if (str.length() < min || str.length() > max) {
            throw new ValidationException(message);
        }
    }

    public static void minSize(Collection<?> collection, int min, String message) {
        notNull(collection, message);
        if (collection.size() < min) {
            throw new ValidationException(message);
        }
    }

    public static void maxSize(Collection<?> collection, int max, String message) {
        notNull(collection, message);
        if (collection.size() > max) {
            throw new ValidationException(message);
        }
    }

    public static void size(Collection<?> collection, int min, int max, String message) {
        notNull(collection, message);
        if (collection.size() < min || collection.size() > max) {
            throw new ValidationException(message);
        }
    }

    public static void min(Number number, long min, String message) {
        notNull(number, message);
        if (number.longValue() < min) {
            throw new ValidationException(message);
        }
    }

    public static void max(Number number, long max, String message) {
        notNull(number, message);
        if (number.longValue() > max) {
            throw new ValidationException(message);
        }
    }

    public static void range(Number number, long min, long max, String message) {
        notNull(number, message);
        long value = number.longValue();
        if (value < min || value > max) {
            throw new ValidationException(message);
        }
    }

    public static void positive(Number number, String message) {
        notNull(number, message);
        if (number.longValue() <= 0) {
            throw new ValidationException(message);
        }
    }

    public static void negative(Number number, String message) {
        notNull(number, message);
        if (number.longValue() >= 0) {
            throw new ValidationException(message);
        }
    }

    public static void nonNegative(Number number, String message) {
        notNull(number, message);
        if (number.longValue() < 0) {
            throw new ValidationException(message);
        }
    }

    public static void equals(Object obj1, Object obj2, String message) {
        if (obj1 == null && obj2 == null) {
            return;
        }
        if (obj1 == null || !obj1.equals(obj2)) {
            throw new ValidationException(message);
        }
    }

    public static void notEquals(Object obj1, Object obj2, String message) {
        if (obj1 == null && obj2 == null) {
            throw new ValidationException(message);
        }
        if (obj1 != null && obj1.equals(obj2)) {
            throw new ValidationException(message);
        }
    }

    public static void isInstanceOf(Object obj, Class<?> type, String message) {
        notNull(obj, message);
        if (!type.isInstance(obj)) {
            throw new ValidationException(message);
        }
    }

    public static boolean isEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhone(String phone) {
        return StringUtils.hasText(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isUsername(String username) {
        return StringUtils.hasText(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isUrl(String url) {
        return StringUtils.hasText(url) && URL_PATTERN.matcher(url).matches();
    }

    public static boolean isIp(String ip) {
        return StringUtils.hasText(ip) && IP_PATTERN.matcher(ip).matches();
    }

    public static boolean isEmpty(String str) {
        return !StringUtils.hasText(str);
    }

    public static boolean isNotEmpty(String str) {
        return StringUtils.hasText(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }
}
