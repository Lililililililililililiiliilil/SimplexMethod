package com.example.simplex.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.simplex.tools.Fraction;
import com.example.simplex.tools.Operations;
import com.example.simplex.tools.Observer;
import com.example.simplex.logic.Problem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainController implements Observer {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab conditionsTab;
    @FXML
    private ProblemController problemTabPageController;
    @FXML
    private Tab simplexTab;
    @FXML
    private SimplexController simplexTabPageController;
    @FXML
    private Tab extraSimplexTab;
    @FXML
    private ArtificialSimplexController artificialSimplexTabPageController;


    @FXML
    private void initialize() {
        problemTabPageController.addObserver(this);
    }

    @FXML
    private void clickAboutEvent(MouseEvent event) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/simplex/Help.fxml")));
        parent.autosize();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Помощь");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(parent);
        Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        alert.showAndWait();
    }

    // Открыть условия задачи
    @FXML
    public void importTask(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открытие файла с условиями задачи");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TASK Файлы", "*.task")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                setConditionsFromLines(lines);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Невозможно открыть файл");
        }
    }

    // Сохранить условия задачи
    @FXML
    public void exportTask(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Task(*.task)", "*.task"));
        fileChooser.setTitle("Сохранение условий задачи в файл");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".task");
            }
            try {
                PrintWriter writer = new PrintWriter(file);
                writeConditions(writer);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    // Считать условия из строк
    private void setConditionsFromLines(List<String> lines) {
        boolean isMin = lines.get(0).equals("1");
        int varCount = Integer.parseInt(lines.get(1));
        int restrictCount = Integer.parseInt(lines.get(2));
        Fraction[] target = Arrays.stream(lines.get(3).split(" ")).map(Operations::stringToFraction).toArray(Fraction[]::new);

        Fraction[][] restrict = new Fraction[restrictCount][];
        int rowIdx = 0;
        for (int i = 4; i < lines.size(); i++) {
            Fraction[] restrictRow = Arrays.stream(lines.get(i).split(" ")).map(Operations::stringToFraction).toArray(Fraction[]::new);
            restrict[rowIdx++] = restrictRow;
        }

        Problem.setMin(isMin);
        Problem.setVarCount(varCount);
        Problem.setTarget(target);
        Problem.setRestrict(restrict);
        Problem.setRestrictCount(restrictCount);

        onConditionChanged("conditionsApply");
        problemTabPageController.setConditions();
    }

    // Запись условий в строки
    private void writeConditions(PrintWriter writer) {
        String rowMin = Problem.isMin() ? "1" : "0";
        String rowVarCount = Integer.toString(Problem.getVarCount());
        String rowRestrictCount = Integer.toString(Problem.getRestrictCount());
        writer.println(rowMin);
        writer.println(rowVarCount);
        writer.println(rowRestrictCount);

        Fraction[] target = Problem.getSourceTarget();
        for (int i = 0; i < target.length - 1; i++) {
            writer.print(target[i].toString() + " ");
        }
        writer.println(target[target.length - 1]);

        Fraction[][] restrict = Problem.getRestrict();
        for (Fraction[] fractions : restrict) {
            for (int i = 0; i < fractions.length - 1; i++) {
                writer.print(fractions[i].toString() + " ");
            }
            writer.println(fractions[fractions.length - 1].toString());
        }
        writer.flush();
        writer.close();
    }

    @Override
    public void onConditionChanged(String type) {
        if (type.equals("conditionsApply")) {
            simplexTabPageController.reset();
            artificialSimplexTabPageController.reset();
        }
    }
}
