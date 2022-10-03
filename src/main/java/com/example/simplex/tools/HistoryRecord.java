package com.example.simplex.tools;

import javafx.collections.ObservableList;
import javafx.util.Pair;
import com.example.simplex.logic.ArtificialSimplexTable;

import java.util.List;
import java.util.Map;

public class HistoryRecord {
    private ArtificialSimplexTable table;
    private boolean rollBackBtn;
    private boolean ArtificialPane;
    private boolean simplexPane;
    private boolean nextArtificialBtn;
    private boolean quickArtificialAnswerBtn;
    private boolean nextSimplexBtn;
    private boolean quickSimplexAnswerBtn;
    private List<Pair<Integer, Integer>> pivotList;
    private List<Integer> eBasisList;
    private ObservableList<Map<String, Object>> simplexItems;
    private boolean showSimplex = false;
    private boolean answerPane;
    private String answerTextAreaText;
    private String answerTextAreaStyle;

    public HistoryRecord() {
    }

    public ArtificialSimplexTable getTable() {
        return table;
    }

    public void setTable(ArtificialSimplexTable table) {
        this.table = table;
    }

    public boolean isRollBackBtn() {
        return rollBackBtn;
    }

    public boolean isArtificialPane() {
        return ArtificialPane;
    }

    public void setArtificialPane(boolean artificialPane) {
        this.ArtificialPane = artificialPane;
    }

    public void setRollBackBtn(boolean rollBackBtn) {
        this.rollBackBtn = rollBackBtn;
    }

    public boolean isNextArtificialBtn() {
        return nextArtificialBtn;
    }

    public void setNextArtificialBtn(boolean nextArtificialBtn) {
        this.nextArtificialBtn = nextArtificialBtn;
    }

    public boolean isQuickArtificialAnswerBtn() {
        return quickArtificialAnswerBtn;
    }

    public void setQuickArtificialAnswerBtn(boolean quickArtificialAnswerBtn) {
        this.quickArtificialAnswerBtn = quickArtificialAnswerBtn;
    }

    public boolean isNextSimplexBtn() {
        return nextSimplexBtn;
    }

    public void setNextSimplexBtn(boolean nextSimplexBtn) {
        this.nextSimplexBtn = nextSimplexBtn;
    }

    public boolean isQuickSimplexAnswerBtn() {
        return quickSimplexAnswerBtn;
    }

    public void setQuickSimplexAnswerBtn(boolean quickSimplexAnswerBtn) {
        this.quickSimplexAnswerBtn = quickSimplexAnswerBtn;
    }

    public boolean isSimplexPane() {
        return simplexPane;
    }

    public void setSimplexPane(boolean simplexPane) {
        this.simplexPane = simplexPane;
    }

    public List<Pair<Integer, Integer>> getPivotList() {
        return pivotList;
    }

    public void setPivotList(List<Pair<Integer, Integer>> pivotList) {
        this.pivotList = pivotList;
    }

    public List<Integer> geteBasisList() {
        return eBasisList;
    }

    public void seteBasisList(List<Integer> eBasisList) {
        this.eBasisList = eBasisList;
    }

    public ObservableList<Map<String, Object>> getSimplexItems() {
        return simplexItems;
    }

    public void setSimplexItems(ObservableList<Map<String, Object>> simplexItems) {
        this.simplexItems = simplexItems;
    }

    public boolean isShowSimplex() {
        return showSimplex;
    }

    public void setShowSimplex(boolean showSimplex) {
        this.showSimplex = showSimplex;
    }


    public boolean isAnswerPane() {
        return answerPane;
    }

    public void setAnswerPane(boolean answerPane) {
        this.answerPane = answerPane;
    }

    public String getAnswerTextAreaText() {
        return answerTextAreaText;
    }

    public void setAnswerTextAreaText(String answerTextAreaText) {
        this.answerTextAreaText = answerTextAreaText;
    }

    public String getAnswerTextAreaStyle() {
        return answerTextAreaStyle;
    }

    public void setAnswerTextAreaStyle(String answerTextAreaStyle) {
        this.answerTextAreaStyle = answerTextAreaStyle;
    }
}
