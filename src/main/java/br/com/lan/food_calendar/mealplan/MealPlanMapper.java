package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.meal.dto.MealResponseDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanResponseDto;
import br.com.lan.food_calendar.user.dto.UserResponseDto;

public class MealPlanMapper {
  public static MealPlanResponseDto toResponseDto(MealPlan plan) {
    return new MealPlanResponseDto(plan.getId(),
            new UserResponseDto(plan.getUser().getId(), plan.getUser().getName()),
            new MealResponseDto(plan.getMeal().getId(), plan.getMeal().getName(), plan.getMeal().getDescription()),
            plan.getMealType(), plan.getStartDate(), plan.getEndDate(), plan.getTargetDays());
  }
}
