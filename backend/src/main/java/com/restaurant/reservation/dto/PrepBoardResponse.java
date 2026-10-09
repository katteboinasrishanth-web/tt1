package com.restaurant.reservation.dto;

import java.time.LocalDate;
import java.util.List;

public class PrepBoardResponse {

    private LocalDate date;
    private int diningMinutes;
    private int taskCount;
    private int occasionCount;
    private int comboCount;
    private List<PrepTaskResponse> tasks;

    public PrepBoardResponse(LocalDate date, int diningMinutes, List<PrepTaskResponse> tasks) {
        this.date = date;
        this.diningMinutes = diningMinutes;
        this.tasks = tasks;
        this.taskCount = tasks.size();
        this.occasionCount = (int) tasks.stream()
                .filter(t -> !"NONE".equals(t.getOccasion()))
                .count();
        this.comboCount = (int) tasks.stream()
                .filter(PrepTaskResponse::isCombo)
                .count();
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDiningMinutes() {
        return diningMinutes;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public int getOccasionCount() {
        return occasionCount;
    }

    public int getComboCount() {
        return comboCount;
    }

    public List<PrepTaskResponse> getTasks() {
        return tasks;
    }
}