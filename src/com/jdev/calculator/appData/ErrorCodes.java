package com.jdev.calculator.appData;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ErrorCodes {
    public static final int ERROR_PRECISION = 0x0;
    public static final int ERROR_OVERFLOW = 0x1;
    public static final int ERROR_BRACKETS = 0x2;
    public static final int ERROR_DIVISION_BY_ZERO = 0x3;
    public static final int ERROR_ARITHMETIC = 0x4;

    @NotNull
    @Contract(pure = true)
    public static String getErrorMsg(int code) {
        return switch (code) {
            case ERROR_PRECISION -> StringConstants.MSG_ERROR_PRECISION;
            case ERROR_OVERFLOW -> StringConstants.MSG_ERROR_OVERFLOW;
            case ERROR_BRACKETS -> StringConstants.MSG_ERROR_BRACKETS;
            case ERROR_DIVISION_BY_ZERO -> StringConstants.MSG_ERROR_DIVISION_BY_ZERO;
            case ERROR_ARITHMETIC -> StringConstants.MSG_ARITHMETIC;
            default -> StringConstants.MSG_ERROR;
        };
    }
}
