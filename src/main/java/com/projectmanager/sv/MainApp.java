package com.projectmanager.sv;

import java.util.Locale;

import com.projectmanager.sv.db.DatabaseManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
@Slf4j 
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            DatabaseManager.initializeDatabase();
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/projectmanager/sv/view/main_view.fxml"));
            Scene scene = new Scene(loader.load(), 1220, 780);
            stage.setTitle("Gestor de avance de proyectos");
            stage.setScene(scene);
            stage.show();
            log.info("Application started");
        } catch (Exception ex) {
            log.error("Application startup failed", ex);
            throw ex;
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("es-SV"));
        launch(args);
    }
}
