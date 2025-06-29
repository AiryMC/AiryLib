package dev.airyy.AiryLib.core.utils;

/**
 * A utility class for measuring elapsed time in different time units, such as milliseconds.
 * The timer uses {@link System#nanoTime()} for accurate time measurement.
 * <p>
 * Usage:
 * <pre>
 * Timer timer = Timer.start();
 * // Do some work...
 * long elapsed = timer.stop(Timer.TimerUnit.MILLISECONDS);
 * </pre>
 */
public final class Timer {

    /**
     * Constant representing the number of nanoseconds in one millisecond.
     */
    public static final int MILLISECONDS = 1_000_000;

    private long startTime = 0L;

    /**
     * Starts a new timer and returns the instance.
     *
     * @return a new {@link Timer} instance with the start time set to the current system time in nanoseconds
     */
    public static Timer start() {
        Timer timer = new Timer();
        timer.startTime = System.nanoTime();
        return timer;
    }

    /**
     * Stops the timer and returns the elapsed time in the specified unit.
     *
     * @param unit the time unit to convert the elapsed time into
     * @return the elapsed time in the specified unit
     */
    public long stop(TimerUnit unit) {
        long endTime = System.nanoTime();
        return (endTime - startTime) / unit.getUnit();
    }

    /**
     * Returns a string representation of the elapsed time in the specified unit.
     *
     * @param unit the time unit to convert the elapsed time into
     * @return a string representing the elapsed time with the unit name (e.g., "150ms")
     */
    public String getString(TimerUnit unit) {
        long endTime = System.nanoTime();
        return (endTime - startTime) / unit.getUnit() + unit.getUnitName();
    }

    /**
     * Enum representing different time units that can be used to measure elapsed time.
     */
    public enum TimerUnit {
        /**
         * Milliseconds unit.
         */
        Milliseconds(MILLISECONDS, "ms");

        private final int unit;
        private final String unitName;

        TimerUnit(int unit, String unitName) {
            this.unit = unit;
            this.unitName = unitName;
        }

        /**
         * Gets the unit value (number of nanoseconds per unit).
         *
         * @return the number of nanoseconds per unit
         */
        public int getUnit() {
            return unit;
        }

        /**
         * Gets the name of the unit (e.g., "ms").
         *
         * @return the unit name
         */
        public String getUnitName() {
            return unitName;
        }
    }
}
