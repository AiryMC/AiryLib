package dev.airyy.AiryLib.core.utils;

/**
 * A utility class for mathematical operations.
 * <p>
 * This class provides various static methods to simplify common mathematical operations.
 * It is designed to be used for utility purposes and does not require instantiation.
 */
public final class Maths {

    /**
     * Checks whether a number is a multiple of another number.
     *
     * @param multiple the number to check divisibility by
     * @param number   the number to be tested
     * @return {@code true} if {@code number} is a multiple of {@code multiple}, {@code false} otherwise
     */
    public static boolean isMultipleOf(int multiple, int number) {
        return number % multiple == 0;
    }

    /**
     * Checks whether a number is a power of a given base.
     * For example, isPowerOf(3, 27) returns true because 3^3 = 27.
     *
     * @param base   the base to check against (must be greater than 1)
     * @param number the number to check (must be greater than 0)
     * @return {@code true} if {@code number} is a power of {@code base}, {@code false} otherwise
     */
    public static boolean isPowerOf(int base, int number) {
        if (base <= 1 || number <= 0) {
            return false;
        }

        while (number % base == 0) {
            number /= base;
        }

        return number == 1;
    }
}
