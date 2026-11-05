package br.com.lan.food_calendar.mealplan.dto;

import br.com.lan.food_calendar.enums.MealType;
import br.com.lan.food_calendar.meal.dto.MealResponseDto;
import br.com.lan.food_calendar.user.dto.UserResponseDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record MealPlanResponseDto(Long id, UserResponseDto user, MealResponseDto meal, MealType mealType,
                                  LocalDate startDate, LocalDate endDate, List<DayOfWeek> targetDays) {
}
