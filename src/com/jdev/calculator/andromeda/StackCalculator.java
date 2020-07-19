package com.jdev.calculator.andromeda;

import com.jdev.calculator.appData.ErrorCodes;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.LossOfPrecisionException;
import org.apfloat.OverflowException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.StringTokenizer;

public final class StackCalculator {
    // mathematical language is always one

    private final String UNARY_MINUS = "u-";

    private final String EXPONENT = "E";

    private final String ASINH = "asinh";
    private final String ACOSH = "acosh";
    private final String ATANH = "atanh";
    private final String ASIN = "asin";
    private final String ACOS = "acos";
    private final String ATAN = "atan";
    private final String SINH = "sinh";
    private final String COSH = "cosh";
    private final String TANH = "tanh";
    private final String SIN = "sin";
    private final String COS = "cos";
    private final String TAN = "tan";
    private final String LOG = "log";
    private final String EXP = "exp";
    private final String LG = "lg";
    private final String LN = "ln";

    private final String PLUS = "+";
    private final String MINUS = "-";
    private final String POWER = "^";
    private final String DIVIDE = "÷";
    private final String MULTIPLY = "×";
    private final String OLD_DIVIDE = "/";
    private final String OLD_MULTIPLY = "*";

    private final String E = "e";
    private final String PI = "π";
    private final String VOID = "";
    private final String PERCENT = "%";
    private final String RADICAL = "√";
    private final String SEPARATOR = ";";
    private final String FACTORIAL = "!";
    private final String OPEN_BRACKET = "(";
    private final String CLOSE_BRACKET = ")";

    private final String[] FUNCTIONS = {SIN, COS, TAN, LOG, RADICAL, FACTORIAL, LG, LN, EXP, ASIN, ACOS, ATAN, SINH, COSH, TANH, ASINH, ACOSH, ATANH};
    private final String OPERATIONS = PLUS + MINUS + POWER + DIVIDE + MULTIPLY + OLD_DIVIDE + OLD_MULTIPLY + PERCENT + EXPONENT;
    private final String DELIMITERS = OPERATIONS + SEPARATOR + RADICAL + PI + E + OPEN_BRACKET + CLOSE_BRACKET + FACTORIAL;

    private Stack<String> numbers;
    private Stack<String> operations;

    private MathContext mathContext;
    private String expression;
    private boolean isRadian;
    private int biasFactor;

    private boolean isErrorIgnore;

    private AdvancedCalculator advancedCalculator;

    // interfaces
    public StackCalculator() {
        this.expression = "";
        this.isRadian = true;
        this.mathContext = MathContext.DECIMAL128;
        this.isErrorIgnore = false;
        this.biasFactor = 50;
    }

    public StackCalculator(String expression, MathContext mathContext, int biasFactor, boolean isRadian, boolean isErrorIgnore) {
        this.expression = expression;
        this.mathContext = mathContext;
        this.biasFactor = biasFactor;
        this.isRadian = isRadian;
        this.isErrorIgnore = isErrorIgnore;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }

    public void setBiasFactor(int biasFactor) {
        this.biasFactor = biasFactor;
    }

    public void setRadian(boolean radian) {
        isRadian = radian;
    }

    public void setErrorIgnore(boolean errorIgnore) {
        isErrorIgnore = errorIgnore;
    }

    public String getExpression() {
        return this.expression;
    }

    public MathContext getMathContext() {
        return this.mathContext;
    }

    public int getBiasFactor() {
        return this.biasFactor;
    }

    public boolean isRadian() {
        return this.isRadian;
    }

    public boolean isErrorIgnore() {
        return this.isErrorIgnore;
    }

    public String getCalculating() {
        // fucking code, but it works!
        if (!isErrorIgnore) {
            try {
                long brackets = 0;
                for (char symbol : expression.toCharArray()) {
                    if (symbol == OPEN_BRACKET.charAt(0)) brackets++;
                    else if (symbol == CLOSE_BRACKET.charAt(0)) brackets--;
                }
                if (brackets != 0) {
                    StringBuilder builder = new StringBuilder(expression);
                    while (brackets > 0) {
                        builder.append(CLOSE_BRACKET);
                        brackets--;
                    }
                    while (brackets < 0) {
                        builder.insert(0, OPEN_BRACKET);
                        brackets++;
                    }
                    expression = builder.toString();
                }
                return startCalculate();
            } catch (OutOfMemoryError | EmptyStackException exc) {
                return ErrorCodes.getErrorMsg(ErrorCodes.ERROR_BRACKETS);
            } catch (ArithmeticException exc) {
                String ZERO = "0";
                if (expression.contains(DIVIDE + ZERO) || expression.contains(OLD_DIVIDE + ZERO)) {
                    return ErrorCodes.getErrorMsg(ErrorCodes.ERROR_DIVISION_BY_ZERO);
                }
                return ErrorCodes.getErrorMsg(ErrorCodes.ERROR_ARITHMETIC);
            } catch (OverflowException exc) {
                return ErrorCodes.getErrorMsg(ErrorCodes.ERROR_OVERFLOW);
            } catch (LossOfPrecisionException exc) {
                mathContext = new MathContext(mathContext.getPrecision() + biasFactor, mathContext.getRoundingMode());
                return startCalculate();
            } catch (Exception exc) {
                return ErrorCodes.getErrorMsg(-1);
            }
        } else {
            return VOID;
        }
    }

    @Nullable
    public String getCalculating(@NotNull Object[] objects) {
        if (objects.length != 1) return null;
        setExpression(objects[0].toString());
        return getCalculating();
    }

    // implementation
    @Contract(pure = true)
    private int getPriority(@NotNull String token) {
        return switch (token) {
            case PLUS, MINUS -> 1;
            case OLD_MULTIPLY, OLD_DIVIDE, MULTIPLY, DIVIDE -> 2;
            case POWER -> 3;
            case UNARY_MINUS -> 4;
            case FACTORIAL, PERCENT -> 5;
            case SIN, COS, TAN, LOG, RADICAL, EXP, ASIN, ACOS, ATAN, LG, LN, SINH, COSH, TANH, ASINH, ACOSH, ATANH -> 6;
            case EXPONENT -> 7;
            default -> 0;
        };
    }

    private boolean isFunction(String token) {
        boolean isTrue = false;
        for (String element : FUNCTIONS)
            if (element.equals(token)) {
                isTrue = true;
                break;
            }
        return isTrue;
    }

    private boolean isUnaryMinus(@NotNull String previous) {
        return (isDelimiter(previous) && !previous.equals(CLOSE_BRACKET) && !previous.equals(FACTORIAL) && !previous.equals(PERCENT) && !previous.equals(PI) && !previous.equals(E))
                || previous.equals(VOID) || previous.equals(UNARY_MINUS);
    }

    private boolean isOperation(String token) {
        return OPERATIONS.contains(token);
    }

    private boolean isDelimiter(String token) {
        return DELIMITERS.contains(token);
    }

    private boolean isNumber(@NotNull String token) {
        if (token.equals(PI) || token.equals(E)) return true;
        return !isDelimiter(token) && !isFunction(token) && !token.equals(UNARY_MINUS);
    }

    private void tryInsertNumericFactor(@NotNull String previous) {
        if (previous.equals(CLOSE_BRACKET) || isNumber(previous) ||
                previous.equals(FACTORIAL) || previous.equals(PERCENT)) {
            while (numbers.size() >= 2 && !operations.isEmpty() && getPriority(MULTIPLY) <= getPriority(operations.peek())) {
                calculate();
            }
            operations.add(MULTIPLY);
        }
    }

    private boolean isContainsFunction(String token) {
        for (String function : FUNCTIONS) if (token.contains(function)) return true;
        return false;
    }

    @Contract(pure = true)
    private int getFunctionLength(String token) {
        for (String function : FUNCTIONS) if (token.contains(function)) return function.length();
        return -1;
    }

    @Contract(pure = true)
    private String startCalculate() {
        if (expression.equals(VOID) || expression.length() <= 0) return VOID;
        advancedCalculator = new AdvancedCalculator(mathContext);

        numbers = new Stack<>();
        operations = new Stack<>();

        StringTokenizer tokenizer = new StringTokenizer(expression, DELIMITERS, true);
        String token, previous = VOID;

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();

            // is Delimiter
            if (isDelimiter(token)) {
                switch (token) {
                    case OPEN_BRACKET -> {
                        tryInsertNumericFactor(previous);
                        operations.push(OPEN_BRACKET);
                    }
                    case CLOSE_BRACKET -> {
                        while (!operations.isEmpty() && !operations.peek().equals(OPEN_BRACKET)) {
                            calculate();
                        }
                        operations.pop();
                    }
                }
            }

            // is Operation or Function
            if (isOperation(token) || isFunction(token)) {
                if (operations.isEmpty()) {
                    if (token.equals(MINUS) && isUnaryMinus(previous))
                        token = UNARY_MINUS;
                    else if (isFunction(token) && !token.equals(FACTORIAL))
                        tryInsertNumericFactor(previous);
                } else {
                    // operations not empty
                    if (isFunction(token) && !token.equals(FACTORIAL))
                        tryInsertNumericFactor(previous);
                    else if (token.equals(MINUS) && isUnaryMinus(previous))
                        token = UNARY_MINUS;
                    if (!previous.equals(EXPONENT) && !token.equals(UNARY_MINUS)) {
                        while (!operations.isEmpty() && getPriority(token) <= getPriority(operations.peek()))
                            calculate();
                    }
                }
                operations.push(token);
            }

            // is Number
            if (isNumber(token)) {
                tryInsertNumericFactor(previous);
                if (token.equals(PI)) token = String.valueOf(Math.PI);
                else if (token.equals(E)) token = String.valueOf(Math.E);
                if (isContainsFunction(token)) {
                    int len = getFunctionLength(token);
                    String number = token.substring(0, token.length() - len);
                    String function = token.substring(token.length() - len);
                    numbers.push(number);

                    while (!operations.isEmpty() && getPriority(MULTIPLY) <= getPriority(operations.peek()))
                        calculate();

                    operations.push(MULTIPLY);
                    operations.push(function);
                    token = function;
                } else {
                    numbers.push(token);
                }
            }

            previous = token;
        }

        while (!operations.isEmpty()) {
            calculate();
        }

        return numbers.pop();
    }

    private void calculate() {
        String temp = null;
        String myStringNumber;
        Apfloat apfloat1, apfloat2;
        switch (operations.peek()) {
            case PLUS, MINUS, MULTIPLY, DIVIDE, OLD_MULTIPLY, OLD_DIVIDE, POWER, LOG, EXPONENT -> {
                apfloat2 = new Apfloat(numbers.pop(), mathContext.getPrecision());  // 56
                apfloat1 = new Apfloat(numbers.pop(), mathContext.getPrecision());  // 23
                switch (operations.pop()) {
                    case PLUS -> temp = ApfloatMath.round(apfloat1.add(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case MINUS -> temp = ApfloatMath.round(apfloat1.subtract(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case MULTIPLY, OLD_MULTIPLY -> temp = ApfloatMath.round(apfloat1.multiply(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case DIVIDE, OLD_DIVIDE -> temp = ApfloatMath.round(apfloat1.divide(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case POWER -> temp = ApfloatMath.round(ApfloatMath.pow(apfloat1, apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case LOG -> temp = ApfloatMath.round(ApfloatMath.log(apfloat1, apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    case EXPONENT -> {
                        Apfloat tempNum = new Apfloat("10", mathContext.getPrecision());
                        apfloat2 = ApfloatMath.round(ApfloatMath.pow(tempNum, apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode());
                        apfloat1 = ApfloatMath.round(apfloat1.multiply(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode());
                        temp = apfloat1.toString(true);
                    }
                }
            }
            case FACTORIAL, PERCENT, SIN, COS, TAN, RADICAL, UNARY_MINUS, ASIN, ACOS, ATAN, EXP, LN, LG, SINH, COSH, TANH -> {
                myStringNumber = numbers.pop();
                switch (operations.pop()) {
                    case FACTORIAL -> {
                        BigDecimal myNumber = new BigDecimal(myStringNumber, mathContext);
                        if (myNumber.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
                            temp = advancedCalculator.slowCalculateFactorial(myNumber.toBigInteger());
                        } else {
                            temp = advancedCalculator.fastCalculateFactorial(myNumber.longValue());
                        }
                    }
                    case PERCENT -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        apfloat2 = new Apfloat(100, mathContext.getPrecision());
                        temp = ApfloatMath.round(apfloat1.divide(apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    }
                    case RADICAL -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        temp = ApfloatMath.round(ApfloatMath.sqrt(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    }
                    case LG -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        apfloat2 = new Apfloat(10, mathContext.getPrecision());
                        temp = ApfloatMath.round(ApfloatMath.log(apfloat1, apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    }
                    case LN -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        apfloat2 = new Apfloat(Math.E, mathContext.getPrecision());
                        temp = ApfloatMath.round(ApfloatMath.log(apfloat1, apfloat2), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    }
                    case EXP -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        temp = ApfloatMath.round(ApfloatMath.exp(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                    }
                    case SIN -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.sin(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.sin(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case COS -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.cos(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.cos(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case TAN -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.tan(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.tan(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ASIN -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.asin(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.asin(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ACOS -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.acos(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.acos(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ATAN -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.atan(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.atan(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case SINH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.sinh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.sinh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case COSH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.cosh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.cosh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case TANH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.tanh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.tanh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ASINH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.asinh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.asinh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ACOSH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.acosh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.acosh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case ATANH -> {
                        apfloat1 = new Apfloat(myStringNumber, mathContext.getPrecision());
                        if (isRadian) {
                            temp = ApfloatMath.round(ApfloatMath.atanh(apfloat1), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        } else {
                            temp = ApfloatMath.round(ApfloatMath.atanh(ApfloatMath.toRadians(apfloat1)), mathContext.getPrecision(), mathContext.getRoundingMode()).toString(true);
                        }
                    }
                    case UNARY_MINUS -> {
                        BigDecimal myNumber = new BigDecimal(myStringNumber, mathContext);
                        temp = myNumber.negate().stripTrailingZeros().toPlainString();
                    }
                }
            }
        }
        numbers.push(temp);
    }
}