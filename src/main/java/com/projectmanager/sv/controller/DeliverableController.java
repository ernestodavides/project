package com.projectmanager.sv.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.projectmanager.sv.model.DeliverableSummary;
import com.projectmanager.sv.model.DeliverableTaskItem;
import com.projectmanager.sv.model.ProjectSummary;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class DeliverableController {
    private final ProjectRepository repository = new ProjectRepository();
    private final ApplicationState state = ApplicationStateHolder.STATE;

    @FXML private ComboBox<ProjectSummary> projectSelector;
    @FXML private Button addDeliverableButton;
    @FXML private Button deleteDeliverableButton;
    @FXML private TableView<DeliverableSummary> deliverablesTable;
    @FXML private TableColumn<DeliverableSummary, String> colDeliverableName;
    @FXML private TableColumn<DeliverableSummary, String> colDeliverableGoal;
    @FXML private TableColumn<DeliverableSummary, Number> colDeliverableProgress;
    @FXML private TableColumn<DeliverableSummary, LocalDate> colDeliverableEndDate;
    @FXML private Button addTaskButton;
    @FXML private Button updateTaskProgressButton;
    @FXML private Button deleteTaskButton;
    @FXML private Label selectedDeliverableLabel;
    @FXML private TableView<DeliverableTaskItem> deliverableTasksTable;
    @FXML private TableColumn<DeliverableTaskItem, String> colTaskTitle;
    @FXML private TableColumn<DeliverableTaskItem, Number> colTaskSprint;
    @FXML private TableColumn<DeliverableTaskItem, Number> colTaskProgress;
    @FXML private TableColumn<DeliverableTaskItem, LocalDate> colTaskEndDate;

    @FXML
    public void initialize() {
        colDeliverableName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name()));
        colDeliverableGoal.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().goal()));
        colDeliverableProgress.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().progress()));
        DialogFactory.configureProgressColumn(colDeliverableProgress);
        colDeliverableEndDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().endDate()));
        configureDeliverableDateColumn(colDeliverableEndDate);
        colTaskTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().title()));
        colTaskSprint.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().sprintNumber()));
        colTaskProgress.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().progress()));
        DialogFactory.configureProgressColumn(colTaskProgress);
        colTaskEndDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().sprintEndDate()));
        configureTaskDateColumn(colTaskEndDate);

        deliverablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, deliverable) ->
        {
            state.selectedDeliverableProperty().set(deliverable);
            deleteDeliverableButton.setDisable(deliverable == null);
            addTaskButton.setDisable(deliverable == null);
            if (deliverable == null) {
                deliverableTasksTable.getItems().clear();
                selectedDeliverableLabel.setText("Tareas (seleccione un entregable)");
            } else {
                reloadTasks(deliverable);
            }
        });
        projectSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, project) -> {
            state.selectedProjectProperty().set(project);
            reload(project);
        });
        state.dataRevisionProperty().addListener((obs, oldValue, revision) -> refreshProjectSelector());
        deliverableTasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, task) -> {
            updateTaskProgressButton.setDisable(task == null);
            deleteTaskButton.setDisable(task == null);
        });
        refreshProjectSelector();
    }

    private void refreshProjectSelector() {
        ProjectSummary selectedProject = projectSelector.getSelectionModel().getSelectedItem();
        Long selectedProjectId = null;
        if (selectedProject != null) {
            selectedProjectId = selectedProject.id();
        } else {
            ProjectSummary sharedSelection = state.selectedProjectProperty().get();
            if (sharedSelection != null) {
                selectedProjectId = sharedSelection.id();
            }
        }
        Long projectIdToKeep = selectedProjectId;
        var projects = repository.findAllProjectsWithProgress();
        projectSelector.setItems(FXCollections.observableArrayList(projects));
        if (projects.isEmpty()) {
            projectSelector.getSelectionModel().clearSelection();
            state.selectedProjectProperty().set(null);
            reload(null);
            return;
        }
        ProjectSummary projectToSelect = projects.stream()
                .filter(project -> projectIdToKeep != null && project.id() == projectIdToKeep)
                .findFirst()
                .orElse(projects.get(0));
        projectSelector.getSelectionModel().select(projectToSelect);
    }

    private void reload(ProjectSummary project) {
        if (project == null) {
            deliverablesTable.getItems().clear();
            deliverableTasksTable.getItems().clear();
            addDeliverableButton.setDisable(true);
            deleteDeliverableButton.setDisable(true);
            addTaskButton.setDisable(true);
            updateTaskProgressButton.setDisable(true);
            deleteTaskButton.setDisable(true);
            state.selectedDeliverableProperty().set(null);
            return;
        }
        DeliverableSummary selectedDeliverable = deliverablesTable.getSelectionModel().getSelectedItem();
        Long selectedDeliverableId = selectedDeliverable == null ? null : selectedDeliverable.id();
        addDeliverableButton.setDisable(false);
        var deliverables = repository.findDeliverablesByProject(project.id());
        deliverablesTable.setItems(FXCollections.observableArrayList(deliverables));
        if (!deliverables.isEmpty()) {
            DeliverableSummary deliverableToSelect = deliverables.stream()
                    .filter(deliverable -> selectedDeliverableId != null && deliverable.id() == selectedDeliverableId)
                    .findFirst()
                    .orElse(deliverables.get(0));
            deliverablesTable.getSelectionModel().select(deliverableToSelect);
        } else {
            state.selectedDeliverableProperty().set(null);
            deliverableTasksTable.getItems().clear();
            deleteDeliverableButton.setDisable(true);
            addTaskButton.setDisable(true);
        }
    }

    @FXML private void handleAddDeliverable() {
        ProjectSummary project = state.selectedProjectProperty().get();
        if (project == null) return;
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nuevo entregable");
        dialog.setHeaderText("Agregar un entregable a " + project.title());
        ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        TextField name = new TextField();
        TextArea goal = new TextArea();
        goal.setPrefRowCount(3);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        grid.add(new Label("Nombre:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Objetivo:"), 0, 1); grid.add(goal, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == save ? new String[]{name.getText(), goal.getText()} : null);
        dialog.showAndWait().ifPresent(values -> {
            if (values[0].isBlank()) {
                DialogFactory.showError("El nombre del entregable es obligatorio.");
                return;
            }
            repository.createDeliverable(project.id(), values[0], values[1]);
            state.notifyDataChanged();
        });
    }

    @FXML private void handleDeleteDeliverable() {
        DeliverableSummary deliverable = deliverablesTable.getSelectionModel().getSelectedItem();
        if (deliverable != null && DialogFactory.confirm("Eliminar entregable",
                "¿Eliminar el entregable '" + deliverable.name() + "' y sus tareas?")) {
            repository.deleteDeliverable(deliverable.id());
            state.notifyDataChanged();
        }
    }

    @FXML private void handleAddTask() {
        ProjectSummary project = state.selectedProjectProperty().get();
        DeliverableSummary deliverable = deliverablesTable.getSelectionModel().getSelectedItem();
        if (project == null || deliverable == null) return;
        var sprints = repository.findAllSprints();
        if (sprints.isEmpty()) {
            DialogFactory.showError("Cree al menos un sprint antes de agregar tareas.");
            return;
        }
        Dialog<DeliverableTaskItem> dialog = new Dialog<>();
        dialog.setTitle("Nueva tarea del entregable");
        ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        TextField title = new TextField();
        ComboBox<Sprint> sprint = new ComboBox<>(FXCollections.observableArrayList(sprints));
        sprint.getSelectionModel().selectFirst();
        Slider progress = new Slider();
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16));
        grid.add(new Label("Título de la tarea:"), 0, 0); grid.add(title, 1, 0);
        grid.add(new Label("Sprint:"), 0, 1); grid.add(sprint, 1, 1);
        grid.add(new Label("Avance inicial:"), 0, 2); grid.add(DialogFactory.createProgressEditor(progress, 0), 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == save
                ? new DeliverableTaskItem(0, deliverable.id(), deliverable.name(), project.id(), project.title(),
                sprint.getValue().id(), sprint.getValue().sprintNumber(), sprint.getValue().endDate(),
                title.getText(), Math.round(progress.getValue()))
                : null);
        dialog.showAndWait().ifPresent(task -> {
            if (task.title().isBlank()) {
                DialogFactory.showError("El título de la tarea es obligatorio.");
                return;
            }
            repository.createTask(deliverable.id(), task.sprintId(), task.title(), task.progress());
            state.notifyDataChanged();
        });
    }

    private void reloadTasks(DeliverableSummary deliverable) {
        if (deliverable == null) {
            deliverableTasksTable.getItems().clear();
            return;
        }
        selectedDeliverableLabel.setText("Tareas de: " + deliverable.name());
        deliverableTasksTable.setItems(FXCollections.observableArrayList(
                repository.findTasksByDeliverable(deliverable.id())));
    }

    private void configureDeliverableDateColumn(TableColumn<DeliverableSummary, LocalDate> column) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "-" : formatter.format(date));
            }
        });
    }

    private void configureTaskDateColumn(TableColumn<DeliverableTaskItem, LocalDate> column) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "-" : formatter.format(date));
            }
        });
    }

    @FXML private void handleUpdateTaskProgress() {
        var task = deliverableTasksTable.getSelectionModel().getSelectedItem();
        if (task != null) promptProgress(task);
    }

    @FXML private void handleDeleteTask() {
        var task = deliverableTasksTable.getSelectionModel().getSelectedItem();
        if (task != null && DialogFactory.confirm("Eliminar tarea", "¿Eliminar la tarea '" + task.title() + "'?")) {
            repository.deleteTask(task.id());
            state.notifyDataChanged();
        }
    }

    private void promptProgress(DeliverableTaskItem task) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Actualizar avance de la tarea");
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
