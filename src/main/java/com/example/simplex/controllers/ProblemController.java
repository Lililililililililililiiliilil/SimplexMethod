package com.example.simplex.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import com.example.simplex.tools.EditingCell;
import com.example.simplex.tools.Fraction;
import com.example.simplex.tools.Operations;
import com.example.simplex.tools.Observer;
import com.example.simplex.logic.Problem;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.example.simplex.tools.Operations.stringToFraction;

public class ProblemController {
    final int MIN_FIELD_VALUE = 1;
    final int MAX_FIELD_VALUE = 16;
    final int INITIAL_FIELD_VALUE = 2;

    @FXML
    public Button applyBtn;
    @FXML
    public Button CancelBtn;
    @FXML
    private Spinner<Integer> varCount;
    @FXML
    private Spinner<Integer> restrictCount;
    @FXML
    private ComboBox<String> targetCombo;
    @FXML
    private ComboBox<String> fractionCombo;
    @FXML
    private TableView targetTable;
    @FXML
    private TableView restrictTable;

    private ObservableList<Map<String, Object>> targetItems;
    private ObservableList<Map<String, Object>> restrictItems;
    private final List<Observer> observerList = new ArrayList<>();

    @FXML
    private void initialize() {
        inputOnlyInteger(varCount, MAX_FIELD_VALUE);
        inputOnlyInteger(restrictCount, MAX_FIELD_VALUE);
        rebuildConditionTables(INITIAL_FIELD_VALUE, INITIAL_FIELD_VALUE);


        varCount.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue < restrictCount.getValue()) {
                restrictCount.getValueFactory().setValue(newValue);


            } else {
                if (newValue <= MAX_FIELD_VALUE && newValue >= MIN_FIELD_VALUE) {
                    rebuildConditionTables(newValue, restrictCount.getValue());
                }
            }
        });


        restrictCount.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue > varCount.getValue()) {
                varCount.getValueFactory().setValue(newValue);

            } else {
                if (newValue >= MIN_FIELD_VALUE && newValue <= MAX_FIELD_VALUE) {
                    rebuildConditionTables(varCount.getValue(), newValue);
                }
            }
        });

        readProblem(null);

        targetItems.addListener((ListChangeListener<Map<String, Object>>) c -> {
            System.out.println("Changed on " + c);
            targetTable.setItems(targetItems);
            if (c.next()) {
                System.out.println(c.getFrom());
            }

        });
    }

    // Считывание данных и заполнение условий
    @FXML
    public void readProblem(ActionEvent actionEvent) {
        Fraction[] target = readTarget();
        Problem.setTarget(target);

        Fraction[][] restrict = readRestrict();
        Problem.setRestrict(restrict);

        int vCount = varCount.getValue();
        Problem.setVarCount(vCount);

        int rCount = restrictCount.getValue();
        Problem.setRestrictCount(rCount);

        boolean isRational = fractionCombo.getValue().equals("Обыкновенный");
        Problem.setRational(isRational);

        boolean isMin = targetCombo.getValue().equals("Минимизировать");
        Problem.setMin(isMin);

        notifyObservers("conditionsApply");
        System.out.println(Problem.verbose());
    }

    // Построение спинеров
    private void inputOnlyInteger(Spinner spinner, int max) {
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    return null;
                }
            }

            return c;
        };
        TextFormatter<Integer> priceFormatter = new TextFormatter<>(
                new IntegerStringConverter(), 2, filter);

        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, max, 2));
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(priceFormatter);
    }

    // Перестройка таблиц в зависимости от кол-ва ограничений и переменных
    private void rebuildConditionTables(int vCount, int rCount) {
        targetTable.setEditable(true);
        targetTable.getSelectionModel().setCellSelectionEnabled(true);
        targetTable.getColumns().clear();
        targetTable.getItems().clear();

        // Ввод с помощью выделения и ввода клавиш
        targetTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                return;
            }
            if (targetTable.getEditingCell() == null) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    TablePosition focusedCellPosition = targetTable.getFocusModel().getFocusedCell();
                    targetTable.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());
                }
            }
        });
        targetTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {


                TablePosition pos = targetTable.getFocusModel().getFocusedCell();

                if (pos.getRow() == -1) {
                    targetTable.getSelectionModel().select(0);
                } else if (pos.getRow() < targetTable.getItems().size() - 1) {
                    targetTable.getSelectionModel().clearAndSelect(pos.getRow() + 1, pos.getTableColumn());
                }

            }
        });

        Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactory = p -> new EditingCell();

        // Заголовок
        for (int i = 1; i <= vCount; i++) {
            TableColumn<Map, String> column = new TableColumn<>("X" + i);
            column.setSortable(false);
            column.setCellValueFactory(new MapValueFactory<>("X" + i));
            targetTable.getColumns().add(column);
            column.setCellFactory(cellFactory);
            int finalI = i;
            column.setOnEditCommit(e -> {
                e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + finalI, e.getNewValue());
            });
        }
        TableColumn<Map, String> column = new TableColumn<>("C");
        column.setSortable(false);
        column.setCellValueFactory(new MapValueFactory<>("C0"));
        targetTable.getColumns().add(column);
        column.setCellFactory(cellFactory);
        column.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C0", e.getNewValue());
        });

        // Заполнение
        targetItems = FXCollections.observableArrayList();
        Map<String, Object> item1 = new HashMap<>();
        for (int i = 0; i <= vCount; i++) {
            String colName = i == 0 ? "C" : "X";
            item1.put(colName + i, "0");
        }
        targetItems.add(item1);

        targetTable.getItems().addAll(targetItems);


        // Таблица ограничений
        restrictTable.setEditable(true);
        restrictTable.getColumns().clear();
        restrictTable.getSelectionModel().setCellSelectionEnabled(true);
        restrictTable.getItems().clear();

        restrictTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                return;
            }
            if (restrictTable.getEditingCell() == null) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    TablePosition focusedCellPosition = restrictTable.getFocusModel().getFocusedCell();
                    restrictTable.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());

                }
            }
        });
        restrictTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                TablePosition pos = restrictTable.getFocusModel().getFocusedCell();

                if (pos.getRow() == -1) {
                    restrictTable.getSelectionModel().select(0);
                } else if (pos.getRow() < restrictTable.getItems().size() - 1) {
                    restrictTable.getSelectionModel().clearAndSelect(pos.getRow() + 1, pos.getTableColumn());
                }
            }

        });

        for (int i = 1; i <= vCount; i++) {
            TableColumn<Map, String> restrictColumn = new TableColumn<>("X" + i);
            restrictColumn.setSortable(false);
            restrictColumn.setCellValueFactory(new MapValueFactory<>("X" + i));
            restrictTable.getColumns().add(restrictColumn);
            restrictColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            restrictColumn.setCellFactory(cellFactory);
            int finalI = i;
            restrictColumn.setOnEditCommit(e -> {
                e.getTableView().getItems().get(e.getTablePosition().getRow()).put("X" + finalI, e.getNewValue());
            });
        }
        TableColumn<Map, String> restrictColumn = new TableColumn<>("b");
        restrictColumn.setSortable(false);
        restrictColumn.setCellValueFactory(new MapValueFactory<>("C0"));
        restrictTable.getColumns().add(restrictColumn);
        restrictColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        restrictColumn.setCellFactory(cellFactory);
        restrictColumn.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).put("C0", e.getNewValue());
        });

        restrictItems = FXCollections.<Map<String, Object>>observableArrayList();
        for (int row = 0; row < rCount; row++) {
            Map<String, Object> restrictItem = new HashMap<>();
            for (int i = 0; i <= vCount; i++) {
                String colName = i == 0 ? "C" : "X";
                restrictItem.put(colName + i, "0");
            }
            restrictItems.add(restrictItem);
        }
        restrictTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        restrictTable.getItems().addAll(restrictItems);
    }

    // Из условий записать данные на экран
    public void setConditions() {
        varCount.getValueFactory().setValue(Problem.getVarCount());
        restrictCount.getValueFactory().setValue(Problem.getRestrictCount());

        Fraction[] target = Problem.getSourceTarget();
        ObservableList<Map<String, Object>> targetItems = FXCollections.observableArrayList();
        Map<String, Object> item1 = new HashMap<>();
        for (int i = 0; i <= Problem.getVarCount(); i++) {
            String colName = i == 0 ? "C" : "X";
            item1.put(colName + i, target[i].toString());
        }
        targetItems.add(item1);
        this.targetItems.setAll(targetItems);
        targetTable.setItems(targetItems);

        targetCombo.setValue(Problem.isMin() ? "Минимизировать" : "Максимизировать");
        fractionCombo.setValue(Problem.isRational() ? "Обыкновенный" : "Десятичный");

        Fraction[][] restrict = Problem.getRestrict();
        restrictItems.clear();
        for (int row = 0; row < Problem.getRestrictCount(); row++) {
            Map<String, Object> restrictItem = new HashMap<>();
            for (int i = 0; i <= Problem.getVarCount(); i++) {
                String colName = i == 0 ? "C" : "X";
                restrictItem.put(colName + i, restrict[row][i].toString());
            }
            restrictItems.add(restrictItem);
        }
        restrictTable.setItems(restrictItems);
    }

    // Считать данные из целевой функции таблицы
    private Fraction[] readTarget() {
        return itemsToFraction(targetItems)[0];
    }

    // Считать данные из таблицы ограничений
    private Fraction[][] readRestrict() {
        return itemsToFraction(restrictItems);
    }

    // Считать табличные данные в массив
    private Fraction[][] itemsToFraction(ObservableList<Map<String, Object>> items) {
        Fraction[][] fractions = new Fraction[items.size()][];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = new Fraction[items.get(i).size()];
        }

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            for (int j = 0; j < item.size(); j++) {
                String nameCol = j == 0 ? "C" : "X";
                String value = (String) items.get(i).get(nameCol + j);
                Fraction fraction = Fraction.ZERO;
                try {
                    fraction = stringToFraction(value);
                } catch (IllegalArgumentException e) {
                    Operations.message(Alert.AlertType.ERROR,
                            "Несоответствие формата ввода",
                            "",
                            "Могут быть лишь введены числа, десятичные и обыкновенные дроби"
                    );
                    Map<String, Object> remove = items.remove(i);
                    remove.put(nameCol + j, "0");
                    items.add(remove);
                }
                fractions[i][j] = fraction;
            }
        }

        return fractions;
    }

    private void notifyObservers(String type) {
        for (Observer observer : observerList) {
            observer.onConditionChanged(type);
        }
    }

    public void addObserver(MainController observer) {
        observerList.add(observer);
    }

    public void cancelProblem(ActionEvent actionEvent) {
        reset();
    }

    public void reset() {
        varCount.getValueFactory().setValue(2);
        restrictCount.getValueFactory().setValue(2);
        targetCombo.setValue("Минимизировать");
        fractionCombo.setValue("Обыкновенный");
        rebuildConditionTables(INITIAL_FIELD_VALUE, INITIAL_FIELD_VALUE);
    }
}
