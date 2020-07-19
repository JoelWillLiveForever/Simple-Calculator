package com.jdev.calculator.andromeda;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

final class AdvancedCalculator {
    private MathContext mathContext;

    // interfaces
    public AdvancedCalculator() {
        this.mathContext = MathContext.DECIMAL128;
    }

    public AdvancedCalculator(MathContext mathContext) {
        this.mathContext = mathContext;
    }

    public void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }

    public MathContext getMathContext() {
        return this.mathContext;
    }

    public String fastCalculateFactorial(long num) {
        return convertType(fastFactTree(num));
    }

    public String slowCalculateFactorial(BigInteger num) {
        return convertType(slowFactTree(num));
    }

    // implementation
    private String convertType(BigInteger val) {
        return new BigDecimal(val, mathContext).stripTrailingZeros().toPlainString();
    }

    // factorials
    private BigInteger slowProdTree(@NotNull BigInteger l, BigInteger r) {
        if (l.compareTo(r) > 0) return BigInteger.ONE;
        if (l.compareTo(r) == 0) return l;
        if (r.subtract(l).compareTo(BigInteger.ONE) == 0) return l.multiply(r);
        BigInteger m = (l.add(r)).divide(BigInteger.TWO);
        return slowProdTree(l, m).multiply(slowProdTree(m.add(BigInteger.ONE), r));
    }

    private BigInteger slowFactTree(@NotNull BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) < 0) return BigInteger.ZERO;
        if (n.compareTo(BigInteger.ZERO) == 0) return BigInteger.ONE;
        if (n.compareTo(BigInteger.ONE) == 0 || n.compareTo(BigInteger.TWO) == 0) return n;
        return slowProdTree(BigInteger.TWO, n);
    }

    private BigInteger fastProdTree(long l, long r) {
        if (l > r) return BigInteger.ONE;
        if (l == r) return BigInteger.valueOf(l);
        if (r - l == 1) return BigInteger.valueOf(l * r);
        long m = (l + r) / 2;
        return fastProdTree(l, m).multiply(fastProdTree(m + 1, r));
    }

    private BigInteger fastFactTree(long n) {
        if (n < 0) return BigInteger.ZERO;
        if (n == 0) return BigInteger.ONE;
        if (n == 1 || n == 2) return BigInteger.valueOf(n);
        return fastProdTree(2, n);
    }
}
