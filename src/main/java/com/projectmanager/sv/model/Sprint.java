package com.projectmanager.sv.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Sprint(
        long id,
        int sprintNumber,
        LocalDate startDate,
        LocalDate endDate
) {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String toString() {
        return "Sprint #" + sprintNumber + " (" + startDate.format(DATE_FORMAT) + " → " + endDate.format(DATE_FORMAT) + ")";
    }
}
