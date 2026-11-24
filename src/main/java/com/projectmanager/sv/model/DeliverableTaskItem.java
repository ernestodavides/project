package com.projectmanager.sv.model;

import java.time.LocalDate;

public record DeliverableTaskItem(
        long id,
        long deliverableId,
        String deliverableName,
        long projectId,
        String projectTitle,
        long sprintId,
        int sprintNumber,
        LocalDate sprintEndDate,
        String title,
        double progress
) {
}
