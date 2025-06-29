package dev.airyy.AiryLib.core.utils;

/**
 * A utility class for handling string operations.
 * <p>
 * This class provides static methods for performing common string manipulations, such as trimming,
 * checking for null or empty strings, converting case, and other helpful string-related operations.
 * It is designed to be used as a helper class and does not require instantiation.
 */
public final class Strings {

    /**
     * Checks if a given string can be parsed as a valid numeric value.
     * The string is considered numeric if it can be parsed into a {@link Double}.
     *
     * @param strNum the string to check
     * @return {@code true} if the string is a valid numeric value, {@code false} otherwise
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
