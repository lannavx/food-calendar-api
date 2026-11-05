package br.com.lan.food_calendar.meal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MealCreateRequestDto(@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres") @NotBlank String name, @NotBlank String description) {
}
