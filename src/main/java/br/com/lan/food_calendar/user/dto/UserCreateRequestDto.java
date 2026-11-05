package br.com.lan.food_calendar.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(@Size(max = 255, message = "O nome deve ter no máximo 255 caracteres") @NotBlank String name) {
}
