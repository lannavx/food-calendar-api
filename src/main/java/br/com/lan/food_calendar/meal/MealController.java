package br.com.lan.food_calendar.meal;

import br.com.lan.food_calendar.meal.dto.MealCreateRequestDto;
import br.com.lan.food_calendar.meal.dto.MealResponseDto;
import br.com.lan.food_calendar.meal.dto.MealUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/meals")
@Validated
public class MealController {

  private final MealService mealService;

  public MealController(MealService mealService) {
    this.mealService = mealService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<MealResponseDto> listMeals(@RequestParam(required = false) String name) {
    if(name == null || name.isBlank()) {
      return mealService.findAll();
    }

    return mealService.findByName(name);
  }

  @GetMapping("/{mealId}")
  @ResponseStatus(HttpStatus.OK)
  public MealResponseDto findById(@PathVariable Long mealId) {
    return mealService.findById(mealId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MealResponseDto create(@Valid @RequestBody MealCreateRequestDto mealCreateRequestDto) {
    return mealService.create(mealCreateRequestDto);
  }

  @PatchMapping("/{mealId}")
  @ResponseStatus(HttpStatus.OK)
  public MealResponseDto update(@PathVariable Long mealId,
                                @Valid @RequestBody MealUpdateRequestDto mealUpdateRequestDto) {
    return mealService.update(mealId, mealUpdateRequestDto);
  }

  @DeleteMapping("/{mealId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long mealId) {
    mealService.delete(mealId);
  }
}
