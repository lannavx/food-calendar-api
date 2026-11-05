package br.com.lan.food_calendar.meal;

import br.com.lan.food_calendar.exception.NotFoundException;
import br.com.lan.food_calendar.meal.dto.MealCreateRequestDto;
import br.com.lan.food_calendar.meal.dto.MealResponseDto;
import br.com.lan.food_calendar.meal.dto.MealUpdateRequestDto;
import br.com.lan.food_calendar.mealplan.MealPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MealService {

  private final MealRepository mealRepository;
  private final MealPlanRepository mealPlanRepository;

  public MealService(MealRepository mealRepository, MealPlanRepository mealPlanRepository) {
    this.mealRepository = mealRepository;
    this.mealPlanRepository = mealPlanRepository;
  }

  public List<MealResponseDto> findAll() {
    List<Meal> meals = mealRepository.findAll();

    return meals.stream().map(meal -> new MealResponseDto(meal.getId(), meal.getName(), meal.getDescription())).toList();
  }

  public List<MealResponseDto> findByName(String name) {
    List<Meal> meals = mealRepository.findByNameContainingIgnoreCase(name);

    return meals.stream().map(meal -> new MealResponseDto(meal.getId(), meal.getName(), meal.getDescription())).toList();
  }

  public MealResponseDto findById(Long mealId) {
    Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new NotFoundException("Refeição não encontrada"));

    return new MealResponseDto(meal.getId(), meal.getName(), meal.getDescription());
  }

  public MealResponseDto create(MealCreateRequestDto mealCreateRequestDto) {
    Meal newMeal = Meal.builder().name(mealCreateRequestDto.name())
            .description(mealCreateRequestDto.description()).build();

    Meal savedMeal = mealRepository.save(newMeal);

    return new MealResponseDto(savedMeal.getId(), savedMeal.getName(), savedMeal.getDescription());
  }

  public MealResponseDto update(Long mealId, MealUpdateRequestDto mealUpdateRequestDto) {
    Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new NotFoundException("Refeição não encontrada"));

    if (mealUpdateRequestDto.name() != null) {
      meal.setName(mealUpdateRequestDto.name());
    }

    if (mealUpdateRequestDto.description() != null) {
      meal.setDescription(mealUpdateRequestDto.description());
    }

    Meal updateMeal = mealRepository.save(meal);

    return new MealResponseDto(updateMeal.getId(), updateMeal.getName(), updateMeal.getDescription());
  }

  @Transactional
  public void delete(Long mealId) {
    Meal meal = mealRepository.findById(mealId)
            .orElseThrow(() -> new NotFoundException("Refeição não encontrada"));

    mealPlanRepository.deleteAllByMealId(mealId);

    mealRepository.delete(meal);
  }

}
