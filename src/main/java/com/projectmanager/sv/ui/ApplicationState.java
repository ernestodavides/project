package com.projectmanager.sv.ui;

import com.projectmanager.sv.model.DeliverableSummary;
import com.projectmanager.sv.model.ProjectSummary;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class ApplicationState {
    private final ObjectProperty<ProjectSummary> selectedProject = new SimpleObjectProperty<>();
    private final ObjectProperty<DeliverableSummary> selectedDeliverable = new SimpleObjectProperty<>();
    private final SimpleLongProperty dataRevision = new SimpleLongProperty();

    public ObjectProperty<ProjectSummary> selectedProjectProperty() {
        return selectedProject;
    }

    public ObjectProperty<DeliverableSummary> selectedDeliverableProperty() {
        return selectedDeliverable;
    }

    public ReadOnlyLongProperty dataRevisionProperty() {
        return dataRevision;
    }

    public void notifyDataChanged() {
        dataRevision.set(dataRevision.get() + 1);
    }
}
