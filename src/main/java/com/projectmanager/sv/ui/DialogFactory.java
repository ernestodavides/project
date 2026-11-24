package com.projectmanager.sv.ui;

import com.projectmanager.sv.model.ProjectSummary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Optional;

public final class DialogFactory {
    private DialogFactory() {
    }

    public static <T> void configureProgressColumn(TableColumn<T, Number> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
            private final Label label = new Label("0.00%");
            private final HBox box = new HBox(8, bar, label);

            {
                bar.setPrefWidth(110);
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    double progress = value.doubleValue();
                    bar.setProgress(progress / 100.0);
                    label.setText(String.format("%.2f%%", progress));
                    setGraphic(box);
                }
            }
        });
    }

    public static HBox createProgressEditor(Slider slider, double initialProgress) {
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(initialProgress);
        slider.setMinWidth(220);
        slider.setPrefWidth(240);
        slider.setMajorTickUnit(25);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setPadding(new Insets(0, 18, 0, 18));

        Label percent = new Label(String.format("%.0f%%", initialProgress));
        percent.setMinWidth(48);
        percent.setAlignment(Pos.CENTER_RIGHT);
        percent.setStyle("-fx-font-weight: bold;");
        slider.valueProperty().addListener((obs, oldValue, value) ->
                percent.setText(String.format("%.0f%%", value.doubleValue())));

        HBox content = new HBox(12, slider, percent);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(16));
        HBox.setHgrow(slider, Priority.ALWAYS);
        return content;
    }

    public static Optional<String[]> showProjectDialog(ProjectSummary existing) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo proyecto" : "Editar proyecto");
        dialog.setHeaderText(existing == null ? "Registrar un nuevo proyecto" : "Actualizar el proyecto #" + existing.id());
        ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField titleField = new TextField(existing == null ? "" : existing.title());
        TextField missionField = new TextField(existing == null ? "" : existing.mission());
        missionField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().length() <= 15 ? change : null));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Misión (máx. 15):"), 0, 1);
        grid.add(missionField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == saveType
                ? new String[]{titleField.getText(), missionField.getText()} : null);

        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] values = result.get();
            if (values[0].isBlank() || values[1].isBlank()) {
                showError("El título y la misión son obligatorios.");
                return Optional.empty();
            }
        }
        return result;
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error en la operación");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
