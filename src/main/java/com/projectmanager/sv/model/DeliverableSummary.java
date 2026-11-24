package com.projectmanager.sv.model;

import java.time.LocalDate;

public record DeliverableSummary(
        long id,
        long projectId,
        String name,
        String goal,
        double progress,
        LocalDate endDate
) {
    @Override
    public String toString() {
        return name;
    }
}
