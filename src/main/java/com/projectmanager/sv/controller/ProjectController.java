package com.projectmanager.sv.controller;

import com.projectmanager.sv.model.ProjectSummary;
import com.projectmanager.sv.repository.ProjectRepository;
import com.projectmanager.sv.ui.ApplicationState;
import com.projectmanager.sv.ui.DialogFactory;
import com.projectmanager.sv.ui.ApplicationStateHolder;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProjectController {
    private final ProjectRepository repository = new ProjectRepository();
    private final ApplicationState state = ApplicationStateHolder.STATE;

    @FXML private Button editProjectButton;
    @FXML private Button deleteProjectButton;
    @FXML private TableView<ProjectSummary> projectsTable;
    @FXML private TableColumn<ProjectSummary, String> colProjectTitle;
    @FXML private TableColumn<ProjectSummary, String> colProjectMission;
    @FXML private TableColumn<ProjectSummary, Number> colProjectProgress;
    @FXML private TableColumn<ProjectSummary, LocalDate> colProjectStartDate;
    @FXML private TableColumn<ProjectSummary, LocalDate> colProjectEndDate;

    @FXML
    public void initialize() {
        colProjectTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().title()));
        colProjectMission.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().mission()));
        colProjectProgress.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().progress()));
        DialogFactory.configureProgressColumn(colProjectProgress);
        colProjectStartDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().startDate()));
        colProjectEndDate.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().endDate()));
        configureDateColumn(colProjectStartDate);
        configureDateColumn(colProjectEndDate);

        editProjectButton.setDisable(true);
        deleteProjectButton.setDisable(true);
        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, project) -> {
            editProjectButton.setDisable(project == null);
            deleteProjectButton.setDisable(project == null);
        });
        state.dataRevisionProperty().addListener((obs, oldValue, revision) -> reload());
        reload();
    }

    private void configureDateColumn(TableColumn<ProjectSummary, LocalDate> column) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "-" : formatter.format(date));
            }
        });
    }

    public void reload() {
        ProjectSummary selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        Long selectedProjectId = selectedProject == null ? null : selectedProject.id();
        var projects = repository.findAllProjectsWithProgress();
        projectsTable.setItems(FXCollections.observableArrayList(projects));
        if (!projects.isEmpty()) {
            ProjectSummary projectToSelect = projects.stream()
                    .filter(project -> selectedProjectId != null && project.id() == selectedProjectId)
                    .findFirst()
                    .orElse(projects.get(0));
            projectsTable.getSelectionModel().select(projectToSelect);
        } else {
            state.selectedProjectProperty().set(null);
        }
    }

    @FXML private void handleNewProject() {
        DialogFactory.showProjectDialog(null).ifPresent(values -> {
            repository.createProject(values[0], values[1]);
            state.notifyDataChanged();
        });
    }

    @FXML private void handleEditProject() {
        ProjectSummary selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        DialogFactory.showProjectDialog(selected).ifPresent(values -> {
            repository.updateProject(selected.id(), values[0], values[1]);
            state.notifyDataChanged();
        });
    }

    @FXML private void handleDeleteProject() {
        ProjectSummary selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (DialogFactory.confirm("Eliminar proyecto", "¿Eliminar el proyecto '" + selected.title() + "' y todos sus entregables?")) {
            repository.deleteProject(selected.id());
            state.notifyDataChanged();
        }
    }
}
