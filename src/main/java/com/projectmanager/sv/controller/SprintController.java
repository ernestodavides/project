package com.projectmanager.sv.controller;

import com.projectmanager.sv.model.DeliverableTaskItem;
import com.projectmanager.sv.model.Sprint;
import com.projectmanager.sv.repository.ProjectRepository;
import com.projectmanager.sv.ui.ApplicationState;
import com.projectmanager.sv.ui.ApplicationStateHolder;
import com.projectmanager.sv.ui.DialogFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SprintController {
    private final ProjectRepository repository = new ProjectRepository();
    private final ApplicationState state = ApplicationStateHolder.STATE;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Button addSprintButton;
    @FXML private Button editSprintButton;
    @FXML private Button deleteSprintButton;
    @FXML private Button updateSprintTaskProgressButton;
    @FXML private TableView<Sprint> sprintsTable;
    @FXML private TableColumn<Sprint, Number> colSprintNumber;
    @FXML private TableColumn<Sprint, LocalDate> colSprintStartDate;
    @FXML private TableColumn<Sprint, LocalDate> colSprintEndDate;
    @FXML private Label selectedSprintTasksLabel;
    @FXML private TableView<DeliverableTaskItem> sprintTasksTable;
    @FXML private TableColumn<DeliverableTaskItem, String> colSprintTaskProject;
    @FXML private TableColumn<DeliverableTaskItem, String> colSprintTaskDeliverable;
    @FXML private TableColumn<DeliverableTaskItem, String> colSprintTaskTitle;
    @FXML private TableColumn<DeliverableTaskItem, Number> colSprintTaskProgress;
    @FXML private TableColumn<DeliverableTaskItem, LocalDate> colSprintTaskEndDate;

    @FXML
    public void initialize() {
        colSprintNumber.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().sprintNumber()));
        colSprintStartDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().startDate()));
        colSprintEndDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().endDate()));
        configureDateColumn(colSprintStartDate);
        configureDateColumn(colSprintEndDate);
        colSprintTaskProject.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().projectTitle()));
        colSprintTaskDeliverable.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().deliverableName()));
        colSprintTaskTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().title()));
        colSprintTaskProgress.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().progress()));
        DialogFactory.configureProgressColumn(colSprintTaskProgress);
        colSprintTaskEndDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().sprintEndDate()));
        configureTaskDateColumn(colSprintTaskEndDate);

        editSprintButton.setDisable(true);
        deleteSprintButton.setDisable(true);
        sprintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, sprint) -> {
            editSprintButton.setDisable(sprint == null);
            deleteSprintButton.setDisable(sprint == null);
            reloadTasks(sprint);
        });
        sprintTasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, task) ->
                updateSprintTaskProgressButton.setDisable(task == null));
        state.dataRevisionProperty().addListener((obs, oldValue, revision) -> reload());
        reload();
    }

    public void reload() {
        Sprint selectedSprint = sprintsTable.getSelectionModel().getSelectedItem();
        Long selectedSprintId = selectedSprint == null ? null : selectedSprint.id();
        var sprints = repository.findAllSprints();
        sprintsTable.setItems(FXCollections.observableArrayList(sprints));
        if (!sprints.isEmpty()) {
            Sprint sprintToSelect = sprints.stream()
                    .filter(sprint -> selectedSprintId != null && sprint.id() == selectedSprintId)
                    .findFirst()
                    .orElse(sprints.get(0));
            sprintsTable.getSelectionModel().select(sprintToSelect);
        } else {
            reloadTasks(null);
        }
    }

    private void configureDateColumn(TableColumn<Sprint, LocalDate> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "-" : date.format(DATE_FORMAT));
            }
        });
    }

    private void configureTaskDateColumn(TableColumn<DeliverableTaskItem, LocalDate> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "-" : date.format(DATE_FORMAT));
            }
        });
    }

    private void reloadTasks(Sprint sprint) {
        updateSprintTaskProgressButton.setDisable(true);
        if (sprint == null) {
            selectedSprintTasksLabel.setText("Tareas del sprint");
            sprintTasksTable.getItems().clear();
            return;
        }
        selectedSprintTasksLabel.setText("Tareas de todos los proyectos en el sprint #" + sprint.sprintNumber()
                + " (" + sprint.startDate().format(DATE_FORMAT) + " -> " + sprint.endDate().format(DATE_FORMAT) + ")");
        sprintTasksTable.setItems(FXCollections.observableArrayList(repository.findTasksBySprint(sprint.id())));
    }

    @FXML private void handleAddSprint() {
        Dialog<Sprint> dialog = new Dialog<>();
        dialog.setTitle("Nuevo sprint");
        dialog.setHeaderText("Crear un sprint global de trabajo");
        ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        int nextNumber = sprintsTable.getItems().stream().mapToInt(Sprint::sprintNumber).max().orElse(0) + 1;
        Spinner<Integer> number = new Spinner<>(1, 999, nextNumber);
        DatePicker start = new DatePicker(LocalDate.now());
        DatePicker end = new DatePicker(LocalDate.now().plusDays(13));
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        grid.add(new Label("Número de sprint:"), 0, 0); grid.add(number, 1, 0);
        grid.add(new Label("Fecha de inicio:"), 0, 1); grid.add(start, 1, 1);
        grid.add(new Label("Fecha de fin:"), 0, 2); grid.add(end, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == save
                ? new Sprint(0, number.getValue(), start.getValue(), end.getValue()) : null);
        dialog.showAndWait().ifPresent(sprint -> {
            if (sprint.startDate() == null || sprint.endDate() == null || sprint.endDate().isBefore(sprint.startDate())) {
                DialogFactory.showError("La fecha de fin debe ser igual o posterior a la fecha de inicio.");
                return;
            }
            try {
                repository.createSprint(sprint.sprintNumber(), sprint.startDate(), sprint.endDate());
                state.notifyDataChanged();
            } catch (Exception ex) {
                DialogFactory.showError(ex.getMessage());
            }
        });
    }

    @FXML private void handleEditSprint() {
        Sprint selected = sprintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Dialog<Sprint> dialog = new Dialog<>();
        dialog.setTitle("Editar sprint");
        dialog.setHeaderText("Modificar sprint #" + selected.sprintNumber());
        ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        Spinner<Integer> number = new Spinner<>(1, 999, selected.sprintNumber());
        DatePicker start = new DatePicker(selected.startDate());
        DatePicker end = new DatePicker(selected.endDate());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        grid.add(new Label("Número de sprint:"), 0, 0); grid.add(number, 1, 0);
        grid.add(new Label("Fecha de inicio:"), 0, 1); grid.add(start, 1, 1);
        grid.add(new Label("Fecha de fin:"), 0, 2); grid.add(end, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == save
                ? new Sprint(selected.id(), number.getValue(), start.getValue(), end.getValue()) : null);
        dialog.showAndWait().ifPresent(sprint -> {
            if (sprint.startDate() == null || sprint.endDate() == null || sprint.endDate().isBefore(sprint.startDate())) {
                DialogFactory.showError("La fecha de fin debe ser igual o posterior a la fecha de inicio.");
                return;
            }
            try {
                repository.updateSprint(sprint.id(), sprint.sprintNumber(), sprint.startDate(), sprint.endDate());
                state.notifyDataChanged();
            } catch (Exception ex) {
                DialogFactory.showError(ex.getMessage());
            }
        });
    }

    @FXML private void handleDeleteSprint() {
        Sprint selected = sprintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (DialogFactory.confirm("Eliminar sprint",
                "¿Eliminar el sprint #" + selected.sprintNumber() + "?")) {
            try {
                repository.deleteSprint(selected.id());
                state.notifyDataChanged();
            } catch (Exception ex) {
                DialogFactory.showError(ex.getMessage());
            }
        }
    }

    @FXML private void handleUpdateSprintTaskProgress() {
        DeliverableTaskItem task = sprintTasksTable.getSelectionModel().getSelectedItem();
        if (task == null) return;
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Actualizar avance de la tarea");
        dialog.setHeaderText(task.projectTitle() + " -> " + task.deliverableName() + "\nTarea: " + task.title());
        ButtonType save = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        Slider slider = new Slider();
        dialog.getDialogPane().setContent(DialogFactory.createProgressEditor(slider, task.progress()));
        dialog.setResultConverter(button -> button == save ? (double) Math.round(slider.getValue()) : null);
        dialog.showAndWait().ifPresent(progress -> {
            repository.updateTaskProgress(task.id(), progress);
            state.notifyDataChanged();
        });
    }
}
