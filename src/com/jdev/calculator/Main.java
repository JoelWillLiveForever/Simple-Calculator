package com.jdev.calculator;

import com.jdev.calculator.andromeda.StackCalculator;
import com.jdev.calculator.appData.Options;

public class Main {

    public static void main(String[] args) {
        String testData = "5+5";
        StackCalculator calculator = new StackCalculator(testData, Options.mathContext, Options.biasFactor, Options.isRadian, Options.isErrorIgnore);
        System.out.println("Result = " + calculator.getCalculating());
    }
}
