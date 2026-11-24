package com.projectmanager.sv.model;

import java.time.LocalDate;

public record ProjectSummary(
        long id,
        String title,
        String mission,
        double progress,
        LocalDate startDate,
        LocalDate endDate
) {
    @Override
    public String toString() {
        return title + " (" + mission + ")";
    }
}
