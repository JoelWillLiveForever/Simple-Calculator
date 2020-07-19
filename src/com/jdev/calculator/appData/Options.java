package com.jdev.calculator.appData;

import java.math.MathContext;
import java.math.RoundingMode;

public final class Options {
    public static boolean isErrorIgnore = false;    // If you want ignore all errors
    public static boolean isRadian = true; // Calculation mode in radians or degrees
    public static int biasFactor = 50; // Displacement coefficient - the speed of increasing accuracy in complex calculations (to avoid the error of insufficient accuracy).
    public static int precision = 17;  // The accuracy of the calculations in the calculations themselves

    public static RoundingMode roundingMode = RoundingMode.HALF_EVEN;   // Rounding mode in calculations
    public static MathContext mathContext = new MathContext(precision, roundingMode);   // Math Context
}
