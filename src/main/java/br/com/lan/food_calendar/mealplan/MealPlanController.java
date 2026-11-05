package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.mealplan.dto.MealPlanCloneRequestDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanCreateRequestDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanResponseDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("v1/meal-plans")
@Validated
public class MealPlanController {

  private final MealPlanService mealPlanService;

  public MealPlanController(MealPlanService mealPlanService) {
    this.mealPlanService = mealPlanService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<MealPlanResponseDto> search(@RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    return mealPlanService.search(userId, startDate, endDate, date);
  }

  @GetMapping("/{mealPlanId}")
  @ResponseStatus(HttpStatus.OK)
  public MealPlanResponseDto findById(@PathVariable Long mealPlanId) {
    return mealPlanService.findById(mealPlanId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MealPlanResponseDto create(@Valid @RequestBody MealPlanCreateRequestDto mealPlanCreateRequestDto) {
    return mealPlanService.create(mealPlanCreateRequestDto);
  }

  @PatchMapping("/{mealPlanId}")
  @ResponseStatus(HttpStatus.OK)
  public MealPlanResponseDto update(@PathVariable Long mealPlanId,
                                    @Valid @RequestBody MealPlanUpdateRequestDto mealPlanUpdateRequestDto) {
    return mealPlanService.update(mealPlanId, mealPlanUpdateRequestDto);
  }

  @PostMapping("/clone")
  @ResponseStatus(HttpStatus.CREATED)
  public void cloneWeek(@Valid @RequestBody MealPlanCloneRequestDto mealPlanCloneRequestDto) {
    mealPlanService.clonePeriod(mealPlanCloneRequestDto);
  }

  @DeleteMapping("/{mealPlanId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long mealPlanId) {
    mealPlanService.delete(mealPlanId);
  }
}
