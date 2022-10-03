package com.example.simplex.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import com.example.simplex.tools.HistoryRecord;
import com.example.simplex.tools.Fraction;
import com.example.simplex.tools.Operations;
import com.example.simplex.logic.Problem;
import com.example.simplex.logic.ArtificialSimplexTable;

import java.util.*;

public class ArtificialSimplexController {
    @FXML
    public Button resetBtn;
    @FXML
    private TableView<Map> simplexTable;
    @FXML
    private TextArea answerTextArea;
    @FXML
    private VBox answerPane;
    @FXML
    private Pane ArtificialPane;
    @FXML
    private Pane simplexPane;
    @FXML
    private Button rollBackBtn;
    @FXML
    private Button nextArtificialBtn;
    @FXML
    private Button nextSimplexBtn;
    @FXML
    private Button quickArtificialAnswerBtn;
    @FXML
    private Button quickSimplexAnswerBtn;
    @FXML
    private ImageView myImageView;

    // Показ симплекс панели
    private boolean showSimplex = false;

    // Данные в отображающей таблице
    private ObservableList<Map<String, Object>> simplexItems;

    // Основная таблица по решению задачи
    private ArtificialSimplexTable table;

    // Список потенциальных ведующих элементов
    private List<Pair<Integer, Integer>> pivotList = new ArrayList<>();

    // Ответ вспомогательной задачи
    private List<Integer> eBasisList;

    // История записей для отката
    private List<HistoryRecord> recordList = new ArrayList<>();

    // Текущая запись, отсутствующая в истории пока-что
    private HistoryRecord curRecord = new HistoryRecord();

    @FXML // Запускается после конструктора, но до основой загрузки
    private void initialize() {
        simplexTable.getSelectionModel().setCellSelectionEnabled(true);

    }


    @FXML
    public void nextArtificialTable(ActionEvent actionEvent) {
        nextArtificial(null, false);
        showSimplexTable(table.getIteration());
    }

    @FXML
    private void quickArtificialAnswer(ActionEvent event) {
        do {
            nextArtificial(null, true);
        } while (!table.isArtificialSolved());
        showSimplexTable(table.getIteration());
    }

    @FXML
    public void nextSimplexTable(ActionEvent actionEvent) {
        nextSimplex(actionEvent, false);
        showSimplexTable(table.getIteration());
    }

    @FXML
    private void quickSimplexAnswer(ActionEvent event) {
        do {
            nextSimplex(null, true);
        } while (!table.isSolved());
        showSimplexTable(table.getIteration());
    }

    @FXML
    private void prevTable(ActionEvent event) {
        loadHistory(recordList.size() - 1);
        if (recordList.size() - 1 > 0) {
            table = table.getPrev();
            showSimplexTable(table.getIteration());
        } else {
            reset();
        }
    }

    @FXML
    private void resetTable(ActionEvent event) {
        reset();
    }

    public void reset() {
        loadHistory(0);

        simplexTable.getColumns().clear();
        simplexTable.getItems().clear();
    }


    // pivotSelected - опорный элемент выбирается автоматически или вручную
    private void nextArtificial(ActionEvent actionEvent, boolean pivotSelected) {
        if (table == null) {
            saveHistory();

            table = new ArtificialSimplexTable();
            table.init(
                    Problem.getTarget(),
                    Problem.getRestrict(),
                    Problem.getVarCount(),
                    Problem.getRestrictCount(),
                    Problem.getBaseList()
            );
        } else {
            if (pivotSelected || isPivotSelected()) {
                saveHistory();

                Pair<Integer, Integer> selectedPivot;
                if (pivotSelected) {
                    List<Pair<Integer, Integer>> pivots = table.findPivots();

                    selectedPivot = pivots.stream().filter(pair -> pair.getKey() > Problem.getVarCount())
                            .findFirst().orElse(
                                    pivots.get(0)
                            );
                } else {
                    selectedPivot = getSelectedPivot();
                }
                table = table.generate(selectedPivot);
                table.iterate();
            } else {
                Operations.message(Alert.AlertType.ERROR,
                        "Ошибка опорного элемента",
                        "",
                        "Вы не выделили опорный элемент!");
            }
        }
        if (table.isArtificialSolved()) {
            onExtraAnswer();
            displayAnswer();
            List<Integer> basisList = table.getBasisList();
            basisList.removeIf(num -> num > Problem.getVarCount());
            eBasisList = basisList;
        } else {
            pivotList = table.findPivots();
        }
        onRollBack();
    }

    private void nextSimplex(ActionEvent actionEvent, boolean pivotSelected) {

        if (table.isArtificialSolved() && !showSimplex) {
            saveHistory();

            table.initSimplex();
            showSimplex = true;
            offExtraAnswer();
        }
        if (table.getIteration() < 0) {
            eBasisList.removeIf(num -> num > Problem.getVarCount());
            table.init(
                    Problem.getTarget(),
                    Problem.getRestrict(),
                    Problem.getVarCount(),
                    Problem.getRestrictCount(),
                    eBasisList
            );
        } else {
            if (pivotSelected || isPivotSelected()) {
                saveHistory();

                Pair<Integer, Integer> selectedPivot;
                if (pivotSelected) {
                    selectedPivot = table.findPivots().get(0);
                } else {
                    selectedPivot = getSelectedPivot();
                }
                table = table.generate(selectedPivot);
                table.iterate();
            } else {
                Operations.message(Alert.AlertType.ERROR,
                        "Ошибка опорного элемента",
                        "",
                        "Вы не выделили опорный элемент!");
            }
        }
        if (table.isSolved()) {
            displayAnswer();
            onSimplexAnswer();
        } else {
            pivotList = table.findPivots();
        }
        onRollBack();
    }

    // Отображение главной таблицы
    private void showSimplexTable(int iteration) {
        {
            simplexTable.getColumns().clear();
            simplexTable.getItems().clear();


            String tableName = showSimplex ? "X" : "X̃";
            TableColumn<Map, String> simplexColumn = new TableColumn<>(tableName + "^" + iteration);
            simplexColumn.setSortable(false);
            simplexColumn.setCellValueFactory(new MapValueFactory<>("F"));
            simplexTable.getColumns().add(simplexColumn);

            simplexColumn.getStyleClass().add("first-column");
            simplexColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("F", e.getNewValue()));
        }

        List<Integer> freeList = table.getFreeList();
        freeList.add(0);
        for (int i : freeList) {
            if (i != 0) {
                TableColumn<Map, Pair<String, Fraction>> simplexColumn = new TableColumn<>("X" + i);
                simplexColumn.setSortable(false);
                simplexColumn.setCellValueFactory(new MapValueFactory<>("X" + i));
                simplexTable.getColumns().add(simplexColumn);

                // Отображение опорных элементов
                simplexColumn.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Pair<String, Fraction> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Fraction pivotRel = getPivotRel(i);
                            setText(item.getKey());
                            List<Integer> basisList = table.getBasisList();
                            Collections.sort(basisList);
                            int idx = this.getIndex();

                            if (
                                    this.getIndex() < table.getBasisList().size() && (
                                            pivotRel.equals(item.getValue()) &&
                                                    table.get(0, i).compareTo(Fraction.ZERO) < 0 &&
                                                    !table.isArtificialSolved() && !table.isSolved() ||

                                                    pivotRel.equals(item.getValue()) &&
                                                            table.get(0, i).compareTo(Fraction.ZERO) < 0 &&
                                                            !table.isSolved() &&
                                                            showSimplex
                                    )
                            ) {
                                setTextFill(Color.GREEN);
                                setStyle("-fx-border-color: #00ff00;");
                            } else {
                                setTextFill(Color.BLACK);
                            }

                            if (pivotRel.equals(item.getValue()) && !table.isSolved()
                                    && Operations.stringToFraction(item.getKey()).compareTo(Fraction.ZERO) < 0 &&
                                    this.getIndex() < table.getBasisList().size() &&
                                    table.get(basisList.get(idx), 0).compareTo(Fraction.ZERO) < 0
                            ) {
                                setTextFill(Color.GREEN);
                                setStyle("-fx-border-color: #00ff00;");
                            }
                        }
                    }
                });
                simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + i, e.getNewValue()));
            }
        }

        // Столбец свободных коэффициентов
        TableColumn<Map, String> simplexColumn = new TableColumn<>("C");
        simplexColumn.setSortable(false);
        simplexColumn.setCellValueFactory(new MapValueFactory<>("C"));
        simplexTable.getColumns().add(simplexColumn);
        simplexColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        simplexColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C", e.getNewValue()));
        simplexItems = FXCollections.observableArrayList();
        List<Integer> basisList = table.getBasisList();
        basisList.add(0);

        for (int i : basisList) {
            Map<String, Object> simplexItem = new HashMap<>();
            String rowName = i == 0 ? " " : "X" + i;
            simplexItem.put("F", rowName);

            for (int j : freeList) {
                String colName = j == 0 ? "C" : "X" + j;
                if (colName.equals("C")) {
                    simplexItem.put(colName, table.get(i, j).toString());
                } else {
                    Fraction rel = null;
                    if (table.get(i, j).compareTo(Fraction.ZERO) != 0) {
                        rel = table.get(i, 0).divide(table.get(i, j));
                    }

                    simplexItem.put(colName, new Pair(table.get(i, j).toString(), rel));
                }
            }
            simplexItems.add(simplexItem);
        }

        simplexTable.getItems().setAll(simplexItems);
    }

    //  Отношения опорного элемента и свободного члена по столбцу
    public Fraction getPivotRel(int col) {
        Fraction pivotRel = Fraction.ZERO;
        pivotList = table.findPivots();
        for (Pair<Integer, Integer> pair : pivotList) {
            if (pair.getValue() == col) {
                Fraction val = table.get(pair.getKey(), pair.getValue());
                pivotRel = table.get(pair.getKey(), 0).divide(val);
            }
        }
        return pivotRel;
    }

    // Отображение ответа
    private void displayAnswer() {
        onAnswer();
        Fraction[] answer = table.getAnswer();
        StringBuilder line = new StringBuilder();
        if (answer == null) {
            onNoSolutionAnswer();
            line.append("НЕТ РЕШЕНИЯ");
        } else {
            answerTextArea.setStyle("-fx-border-color: #00ff00; -fx-font-size: 16;");
            if (table.isArtificialSolved() && !table.isSolved()) {
                answerTextArea.setStyle("-fx-border-color: #FFA500; -fx-font-size: 14;");
                line.append("Решение вспомогательной задачи:\n");
            }
            line.append("X(");
            for (int i = 1; i < answer.length; i++) {
                line.append(answer[i].toString()).append(", ");
            }
            line.deleteCharAt(line.length() - 2);
            line.append(")\n");
            line.append("F = ");
            if (Problem.isMin()) {
                line.append(answer[0]);
            } else {
                line.append(answer[0].multiply(-1));
            }
        }
        answerTextArea.setText(line.toString());
    }

    private boolean isPivotSelected() {
        ObservableList<TablePosition> selectedCells = simplexTable.getSelectionModel().getSelectedCells();
        if (selectedCells.size() > 0) {
            int selectedCol = selectedCells.get(0).getColumn() - 1;
            int selectedRow = selectedCells.get(0).getRow();
            if (selectedRow >= table.getBasisList().size() ||
                    selectedCol >= table.getFreeList().size()) {
                return false;
            }
            int col = table.getFreeList().get(
                    selectedCol
            );
            int row = table.getBasisList().get(
                    selectedRow
            );
            return table.findPivots().contains(new Pair(row, col));
        }
        return false;
    }

    private Pair<Integer, Integer> getSelectedPivot() {
        ObservableList<TablePosition> selectedCells = simplexTable.getSelectionModel().getSelectedCells();
        if (selectedCells.size() > 0) {
            int col = table.getFreeList().get(
                    selectedCells.get(0).getColumn() - 1
            );
            int row = table.getBasisList().get(
                    selectedCells.get(0).getRow()
            );
            return new Pair(row, col);
        }
        return null;
    }

    private void saveHistory() {
        curRecord.setTable(table);
        curRecord.setArtificialPane(ArtificialPane.isVisible());
        curRecord.setNextArtificialBtn(nextArtificialBtn.isVisible());
        curRecord.setNextSimplexBtn(nextSimplexBtn.isVisible());
        curRecord.seteBasisList(eBasisList == null ? new ArrayList<>() : new ArrayList<>(eBasisList));
        curRecord.setSimplexPane(simplexPane.isVisible());
        curRecord.setArtificialPane(ArtificialPane.isVisible());
        curRecord.setShowSimplex(showSimplex);
        curRecord.setPivotList(pivotList == null ? new ArrayList<>() : new ArrayList<>(pivotList));
        curRecord.setQuickArtificialAnswerBtn(quickArtificialAnswerBtn.isVisible());
        curRecord.setQuickSimplexAnswerBtn(quickSimplexAnswerBtn.isVisible());
        curRecord.setRollBackBtn(rollBackBtn.isVisible());
        curRecord.setSimplexItems(simplexItems == null ? FXCollections.observableArrayList() : FXCollections.observableArrayList(simplexItems));
        curRecord.setAnswerPane(answerPane.isVisible());
        curRecord.setAnswerTextAreaText(answerTextArea.getText());
        curRecord.setAnswerTextAreaStyle(answerTextArea.getStyle());
        recordList.add(curRecord);

        curRecord = new HistoryRecord();
    }

    private void loadHistory(int recordNum) {
        if (recordNum < 0 || recordList.size() == 0) {
            return;
        }

        HistoryRecord record = recordList.get(recordNum);
        curRecord = record;

        List<HistoryRecord> newRecordList = new ArrayList<>();
        for (int i = 0; i < recordNum; i++) {
            newRecordList.add(recordList.get(i));
        }

        if (recordNum == 0) {
            newRecordList = new ArrayList<>(List.of(record));
        }
        recordList = newRecordList;

        applyRecord();
    }

    private void applyRecord() {
        table = curRecord.getTable();
        ArtificialPane.setVisible(curRecord.isArtificialPane());
        simplexPane.setVisible(curRecord.isSimplexPane());
        nextArtificialBtn.setVisible(curRecord.isNextArtificialBtn());
        nextSimplexBtn.setVisible(curRecord.isNextSimplexBtn());
        eBasisList = curRecord.geteBasisList();
        showSimplex = curRecord.isShowSimplex();
        pivotList = curRecord.getPivotList();
        quickArtificialAnswerBtn.setVisible(curRecord.isQuickArtificialAnswerBtn());
        quickSimplexAnswerBtn.setVisible(curRecord.isQuickSimplexAnswerBtn());
        rollBackBtn.setVisible(curRecord.isRollBackBtn());
        simplexItems = curRecord.getSimplexItems();
        answerPane.setVisible(curRecord.isAnswerPane());
        answerTextArea.setStyle(curRecord.getAnswerTextAreaStyle());
        answerTextArea.setText(curRecord.getAnswerTextAreaText());
    }

    private void onNextSimplexBtn() {
        nextSimplexBtn.setVisible(true);
    }

    private void offNextSimplexBtn() {
        nextSimplexBtn.setVisible(false);
    }

    private void onQuickSimplexAnswerBtn() {
        quickSimplexAnswerBtn.setVisible(true);
    }

    private void offQuickSimplexAnswerBtn() {
        quickSimplexAnswerBtn.setVisible(false);
    }

    private void onSimplexAnswer() {
        offSimplexPane();
        offExtraPane();
        onAnswer();
        offNextSimplexBtn();
        offQuickSimplexAnswerBtn();
    }

    private void onExtraAnswer() {
        offExtraPane();
        onSimplexPane();
        onAnswer();
        onNextSimplexBtn();
        onQuickSimplexAnswerBtn();
    }

    private void offExtraAnswer() {
        offAnswer();
    }

    private void onNoSolutionAnswer() {
        offExtraPane();
        offSimplexPane();
        answerTextArea.setStyle("-fx-border-color: #ff0000; -fx-font-size: 16;");
    }

    private void offExtraPane() {
        ArtificialPane.setVisible(false);
    }

    private void onExtraPane() {
        ArtificialPane.setVisible(true);
    }

    private void onSimplexPane() {
        simplexPane.setVisible(true);
    }

    private void offSimplexPane() {
        simplexPane.setVisible(false);
    }

    private void onRollBack() {
        rollBackBtn.setVisible(true);
    }

    private void offAnswer() {
        answerPane.setVisible(false);
    }

    private void onAnswer() {
        answerPane.setVisible(true);
    }
}
