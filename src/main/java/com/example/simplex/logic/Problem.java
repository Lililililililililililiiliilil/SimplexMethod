package com.example.simplex.logic;

import com.example.simplex.tools.Fraction;

import java.util.Arrays;
import java.util.List;

public class Problem {
    private static Fraction[] target;
    private static Fraction[][] restrict;
    private static List<Integer> baseList;
    private static boolean rational = true;
    private static boolean min = true;
    private static int varCount;
    private static int restrictCount;

    public static Fraction[] getTarget() {
        Fraction[] target = new Fraction[Problem.target.length];

            for (int i = 0; i < target.length; i++) {
                if (isMin()) {
                    target[i] = Problem.target[i];
                } else {
                    target[i] = Problem.target[i].multiply(-1);
                }
            }

        return target;
    }

    public static void setTarget(Fraction[] target) {
        Problem.target = target;
        Problem.varCount = target.length - 1;
    }

    public static Fraction[] getSourceTarget() {
        return target;
    }

    public static Fraction[][] getRestrict() {
        return restrict;
    }

    public static void setRestrict(Fraction[][] restrict) {
        Problem.restrict = restrict;
    }

    public static List<Integer> getBaseList() {
        return baseList;
    }

    public static void setBaseList(List<Integer> baseList) {
        Problem.baseList = baseList;
    }

    public static boolean isRational() {
        return rational;
    }

    public static void setRational(boolean rational) {
        Problem.rational = rational;

    }

    public static boolean isMin() {
        return min;
    }

    public static void setMin(boolean min) {
        Problem.min = min;

    }

    public static int getVarCount() {
        return varCount;
    }

    public static void setVarCount(int varCount) {
        Problem.varCount = varCount;
    }

    public static int getRestrictCount() {
        return restrictCount;
    }

    public static void setRestrictCount(int restrictCount) {
        Problem.restrictCount = restrictCount;

    }

    public static String verbose() {
        StringBuilder restrictLine = new StringBuilder();
        for (Fraction[] fractions : restrict) {
            for (Fraction fraction : fractions) {
                restrictLine.append("\t\t").append(fraction).append(" ");
            }
            restrictLine.append("\n");
        }
        return "Conditions{\n" +
                "\ttarget= " + Arrays.toString(target) +
                ", \n\trestrict=[\n" + restrictLine +
                "\t], \n\tbaseList= " + baseList +
                ", \n\trational= " + rational +
                ", \n\tmin= " + min +
                "\n}";

    }
}
