package com.example.simplex.logic;

import javafx.util.Pair;
import com.example.simplex.tools.Fraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArtificialSimplexTable {
    private Fraction[] target;
    private Fraction[][] restrict;
    private int varCount;
    private int restrictCount;
    private SimplexTable table;
    private boolean ArtificialSolved;
    private boolean solved;
    private Fraction[] ArtificialAnswer;
    private boolean noSolution;
    private SolutionType mode = SolutionType.ARTIFICIAL;

    public void init(
            Fraction[] target,
            Fraction[][] restrict,
            int varCount,
            int restrictCount,
            List<Integer> basisList
    ) {
        setTarget(target);
        setRestrict(restrict);
        setVarCount(varCount);
        setRestrictCount(restrictCount);
        setBasisList(basisList);
        if (!ArtificialSolved) {
            initConvertedConditions();
            if (checkArtificialSolved()) {
                setArtificialSolved(true);
            }
        } else {
            table.init(
                    target, restrict, varCount, restrictCount, basisList
            );
            if (checkSolved()) {
                setSolved(true);
            }
        }
    }

    private void initConvertedConditions() {
        Fraction[] eTarget = convertTarget();
        int eVarCount = getVarCount() + getRestrictCount();
        int eRestrictCount = getRestrictCount();
        List<Integer> eBasisList = convertBasisList();
        Fraction[][] eRestrict = convertRestrict();

        table.init(
                eTarget,
                eRestrict,
                eVarCount,
                eRestrictCount,
                eBasisList
        );
    }

    private Fraction[] convertTarget() {
        Fraction[] eTarget = new Fraction[getVarCount() + getRestrictCount() + 1];
        for (int i = 0; i < eTarget.length; i++) {
            if (i >= getVarCount() + 1) {
                eTarget[i] = Fraction.ONE;
            } else {
                eTarget[i] = Fraction.ZERO;
            }
        }
        return eTarget;
    }

    private List<Integer> convertBasisList() {
        List<Integer> list = new ArrayList<>();
        int newVar = getVarCount() + 1;
        for (int i = 0; i < getRestrictCount(); i++) {
            list.add(newVar++);
        }
        return list;
    }

    private Fraction[][] convertRestrict() {
        int newVar = getVarCount() + 1;
        Fraction[][] eRestrict = new Fraction[getRestrictCount()][];
        for (int i = 0; i < eRestrict.length; i++) {
            eRestrict[i] = new Fraction[getVarCount() + getRestrictCount() + 1];
            for (int j = 0; j < eRestrict[i].length; j++) {
                if (j == newVar) {
                    eRestrict[i][j] = Fraction.ONE;
                } else if (j > getVarCount()) {
                    eRestrict[i][j] = Fraction.ZERO;
                } else {
                    eRestrict[i][j] = restrict[i][j];
                }
                if (restrict[i][0].compareTo(Fraction.ZERO) < 0 && j <= getVarCount()) {
                    eRestrict[i][j] = restrict[i][j].multiply(-1);
                }
            }
            newVar++;
        }
        return eRestrict;
    }

    public ArtificialSimplexTable getPrev() {
        if (getIteration() == 0 && mode == SolutionType.SIMPLEX) {
            setMode(SolutionType.ARTIFICIAL);
        }
        if (getIteration() >= 0) {
            ArtificialSolved = isArtificialSolved();
        } else {
            ArtificialSolved = false;
        }
        table = table.getPrev();
        boolean newSolved = table.isSolved();
        if (!newSolved && ArtificialSolved && mode == SolutionType.ARTIFICIAL) {
            setArtificialSolved(false);
        }
        solved = false;
        return this;
    }

    public int getIteration() {
        return table.getIteration();
    }

    public List<Integer> getBasisList() {
       return table.getBasisList();
    }

    public List<Integer> getFreeList() {
        return table.getFreeList();
    }

    public Fraction get(int basis, int free) {
        return table.get(basis, free);
    }

    public List<Pair<Integer, Integer>> findPivots() {
        return table.findPivots();
    }

    private boolean isOptimalSolution() {
        Fraction[] deltas = calcDeltas();
        return Arrays.stream(deltas).noneMatch(
                delta -> delta.compareTo(Fraction.ZERO) > 0
        );
    }

    private Fraction[] calcDeltas() {
        Fraction[] deltas = new Fraction[getFreeList().size() + 1];
        for (int i = 0; i < deltas.length; i++) {
            deltas[i] = Fraction.ZERO;
        }
        Fraction[] target = getTarget();
        List<Integer> freeList = getFreeList();
        int idxCol = 0;
        for (int col : getFreeList()) {
            Fraction delta = Fraction.ZERO;
            for (int row : getBasisList()) {
                Fraction c = Fraction.ZERO;
                if (row > getVarCount()) {
                    c = new Fraction(1000_000);
                } else {
                    c = row < target.length ? target[row] : Fraction.ZERO;
                }
                Fraction val = get(row, col);
                delta = delta.add(
                        c.multiply(val)
                );
            }
            Fraction c;
            if (col > getVarCount()) {
                c = new Fraction(1000_000);
            } else {
                c = col < target.length ? target[col] : Fraction.ZERO;
            }
            delta = delta.subtract(
                    c
            );
            deltas[idxCol++] = delta;
        }
        return deltas;
    }


    public ArtificialSimplexTable generate(Pair<Integer, Integer> selectedPivot) {
        table = table.generate(selectedPivot);
        return this;
    }

    public void iterate() {
        table.iterate();

        if (checkArtificialSolved() && mode == SolutionType.ARTIFICIAL) {
            setArtificialAnswer();
            setArtificialSolved(true);
            return;
        }
        if (mode == SolutionType.ARTIFICIAL) {
            solved = false;
        }
        if (mode == SolutionType.SIMPLEX) {
            if (checkSolved()) {
                setSolved(true);
            }
        }
    }

    private void setArtificialAnswer() {
        if (noSolution) {
            ArtificialAnswer = null;
            return;
        }
        ArtificialAnswer = new Fraction[getVarCount() + getRestrictCount() + 1];
        ArtificialAnswer[0] = table.get(0, 0).multiply(-1);
        for (int i = 1; i < ArtificialAnswer.length; i++) {
            ArtificialAnswer[i] = getTable().getTable().getOrDefault(i, Map.of(0, Fraction.ZERO)).get(0);
        }
    }

    public Fraction[] getAnswer() {
        if (noSolution) {
            return null;
        }
        if (ArtificialSolved && !isSolved()) {
            return ArtificialAnswer;
        } else {
            return table.getAnswer();
        }
    }


    public ArtificialSimplexTable() {
        table = new SimplexTable();
    }

    public SimplexTable getTable() {
        return table;
    }

    public void setTable(SimplexTable table) {
        this.table = table;
    }

    public Fraction[] getTarget() {
        return target;
    }

    public void setTarget(Fraction[] target) {
        this.target = target;
    }

    public Fraction[][] getRestrict() {
        return restrict;
    }

    public void setRestrict(Fraction[][] restrict) {
        this.restrict = restrict;
    }

    public int getVarCount() {
        return varCount;
    }

    public void setVarCount(int varCount) {
        this.varCount = varCount;
    }

    public int getRestrictCount() {
        return restrictCount;
    }

    public void setRestrictCount(int restrictCount) {
        this.restrictCount = restrictCount;
    }

    public void setBasisList(List<Integer> basisList) {
    }

    public boolean checkArtificialSolved() {
        int countExtraVar = (int) getBasisList().stream().filter(
                basisNum -> basisNum > getVarCount()
        ).count();
        boolean hasSolution = table.isSolved();
        if (hasSolution) {
            ArtificialAnswer = getAnswer();
            if (ArtificialAnswer == null || ArtificialAnswer[0].compareTo(Fraction.ZERO) > 0) {
                noSolution = true;
            }
        }
        if (isArtificialHasNoSolution()) {
            noSolution = true;
        }
        return hasSolution || noSolution;
    }

    private boolean isArtificialHasNoSolution() {
        if (isOptimalSolution()) {
            boolean isAllExtraZero = table.getBasisList().stream().filter(
                    basisNum -> basisNum > getVarCount()
            ).allMatch(
                    extraVar -> get(extraVar, 0).equals(Fraction.ZERO)
            );
            if (isAllExtraZero) {
                return false;
            }

            boolean isAnyExtraNotZero = table.getBasisList().stream().filter(
                    basisNum -> basisNum > getVarCount()
            ).anyMatch(
                    extraVar -> !get(extraVar, 0).equals(Fraction.ZERO)
            );

            if (isAnyExtraNotZero) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }


    public boolean checkSolved() {
        setSolved(table.isSolved());
        return isSolved();
    }

    public boolean isArtificialSolved() {
        return ArtificialSolved;
    }

    public void setArtificialSolved(boolean artificialSolved) {
        this.ArtificialSolved = artificialSolved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isSolved() {
        return solved;
    }

    public void initSimplex() {
        setMode(SolutionType.SIMPLEX);
        Fraction[] extraAnswer = getAnswer();
        List<Integer> basisList = new ArrayList<>();
        for (int i = 1; i < extraAnswer.length; i++) {
            if (!extraAnswer[i].equals(Fraction.ZERO)) {
                basisList.add(i);
            }
        }
        table = table.generate();
    }

    public SolutionType getMode() {
        return mode;
    }

    public void setMode(SolutionType mode) {
        this.mode = mode;
    }

    public enum SolutionType {
        ARTIFICIAL, SIMPLEX
    }
}
