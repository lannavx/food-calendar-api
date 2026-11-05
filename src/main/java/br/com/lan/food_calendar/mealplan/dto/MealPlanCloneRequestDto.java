package br.com.lan.food_calendar.mealplan.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MealPlanCloneRequestDto(@NotNull Long userId, @NotNull LocalDate originalStartDate,
                                      @NotNull LocalDate originalEndDate, @NotNull LocalDate newStartDate,
                                      @NotNull LocalDate newEndDate) {
}
