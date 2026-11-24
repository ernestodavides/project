package com.projectmanager.sv.controller;

import com.projectmanager.sv.db.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane projectsView;
    @FXML private StackPane deliverablesView;
    @FXML private StackPane sprintsView;
    @FXML private Label dbStatusLabel;
    @FXML private Button projectsMenuButton;
    @FXML private Button deliverablesMenuButton;
    @FXML private Button sprintsMenuButton;

    private static final String ACTIVE_MENU_STYLE =
            "-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT;";
    private static final String INACTIVE_MENU_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #cfd8dc; -fx-alignment: CENTER_LEFT;";

    @FXML
    public void initialize() {
        dbStatusLabel.setText("H2 conectado: " + DatabaseManager.JDBC_URL + " | Usuario: " + DatabaseManager.DB_USER);
        showView(projectsView, projectsMenuButton);
    }

    @FXML private void handleShowProjects() {
        showView(projectsView, projectsMenuButton);
    }

    @FXML private void handleShowDeliverables() {
        showView(deliverablesView, deliverablesMenuButton);
    }

    @FXML private void handleShowSprints() {
        showView(sprintsView, sprintsMenuButton);
    }

    private void showView(StackPane activeView, Button activeButton) {
        projectsView.setVisible(activeView == projectsView);
        projectsView.setManaged(activeView == projectsView);
        deliverablesView.setVisible(activeView == deliverablesView);
        deliverablesView.setManaged(activeView == deliverablesView);
        sprintsView.setVisible(activeView == sprintsView);
        sprintsView.setManaged(activeView == sprintsView);
        updateActiveMenuButton(activeButton);
    }

    private void updateActiveMenuButton(Button activeButton) {
        projectsMenuButton.setStyle(activeButton == projectsMenuButton ? ACTIVE_MENU_STYLE : INACTIVE_MENU_STYLE);
        deliverablesMenuButton.setStyle(activeButton == deliverablesMenuButton ? ACTIVE_MENU_STYLE : INACTIVE_MENU_STYLE);
        sprintsMenuButton.setStyle(activeButton == sprintsMenuButton ? ACTIVE_MENU_STYLE : INACTIVE_MENU_STYLE);
    }
}
