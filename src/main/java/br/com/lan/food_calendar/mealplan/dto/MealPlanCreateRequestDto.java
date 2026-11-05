package br.com.lan.food_calendar.mealplan.dto;

import br.com.lan.food_calendar.enums.MealType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record MealPlanCreateRequestDto(@NotNull Long userId, @NotNull Long mealId, @NotNull MealType mealType,
                                       @NotNull LocalDate startDate, @NotNull LocalDate endDate,
                                       @NotEmpty(message = "A lista não pode estar vazia") List<@NotNull(message = "O" +
                                               " dia não pode ser nulo") DayOfWeek> targetDays) {
}
