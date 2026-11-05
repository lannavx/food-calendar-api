package br.com.lan.food_calendar.mealplan.dto;

import br.com.lan.food_calendar.enums.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record MealPlanUpdateRequestDto(Long mealId,
                                       MealType mealType,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       @Size(min = 1, message = "A lista de dias não pode estar vazia")
                                       List<@NotNull(message = "O dia informado não pode ser nulo") DayOfWeek> targetDays) {
}
