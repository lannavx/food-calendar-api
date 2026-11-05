package br.com.lan.food_calendar.meal.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MealUpdateRequestDto(@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
                                   @Pattern(regexp = "(?s).*\\S.*", message = "O nome não pode estar em branco") String name,
                                   @Pattern(regexp = "(?s).*\\S.*", message = "A descrição não pode estar em branco")  String description) {
}
