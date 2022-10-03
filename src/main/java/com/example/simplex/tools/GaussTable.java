package com.example.simplex.tools;

import java.util.List;

public class GaussTable {
    public enum Solution {
        ZERO, ONE, INF
    }
    private List<Integer> swapedCols;
    private Fraction[][] matr;
    private Solution type;

    public GaussTable() {
    }

    public List<Integer> getSwapedCols() {
        return swapedCols;
    }

    public void setSwapedCols(List<Integer> swapedCols) {
        this.swapedCols = swapedCols;
    }

    public Fraction[][] getMatr() {
        return matr;
    }

    public void setMatr(Fraction[][] matr) {
        this.matr = matr;
    }

    public void setType(Solution type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder matrLine = new StringBuilder();
        for (Fraction[] fractions : matr) {
            for (Fraction fraction : fractions) {
                matrLine.append("\t\t").append(fraction).append(" ");
            }
            matrLine.append("\n");
        }
        return "GaussTable{" +
                "\n\tswapedCols=" + swapedCols +
                ",\n\tmatr=[\n" + matrLine +
                "\t],\n\ttype=" + type +
                "\n}";
    }
}
