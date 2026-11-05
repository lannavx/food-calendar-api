package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.exception.BusinessRuleException;
import br.com.lan.food_calendar.exception.ConflictException;
import br.com.lan.food_calendar.exception.NotFoundException;
import br.com.lan.food_calendar.meal.Meal;
import br.com.lan.food_calendar.meal.MealRepository;
import br.com.lan.food_calendar.mealplan.dto.MealPlanCloneRequestDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanCreateRequestDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanResponseDto;
import br.com.lan.food_calendar.mealplan.dto.MealPlanUpdateRequestDto;
import br.com.lan.food_calendar.user.User;
import br.com.lan.food_calendar.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MealPlanService {
  private final MealPlanRepository mealPlanRepository;
  private final UserRepository userRepository;
  private final MealRepository mealRepository;

  public MealPlanService(MealPlanRepository mealPlanRepository, UserRepository userRepository, MealRepository mealRepository) {
    this.mealPlanRepository = mealPlanRepository;
    this.userRepository = userRepository;
    this.mealRepository = mealRepository;
  }

  public MealPlanResponseDto findById(Long mealPlanId) {
    MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
            .orElseThrow(() -> new NotFoundException("Plano alimentar não encontrado"));

    return MealPlanMapper.toResponseDto(mealPlan);
  }

  public MealPlanResponseDto create(MealPlanCreateRequestDto mealPlanCreateRequestDto) {

    if(mealPlanCreateRequestDto.startDate().isAfter(mealPlanCreateRequestDto.endDate())) {
      throw new BusinessRuleException("A data inicial não pode ser posterior a data final");
    }

    User user = userRepository.findById(mealPlanCreateRequestDto.userId())
            .orElseThrow(() ->
                    new NotFoundException("Usuário " + mealPlanCreateRequestDto.userId() + " não existe"));

    Meal meal = mealRepository.findById(mealPlanCreateRequestDto.mealId())
            .orElseThrow(() -> new NotFoundException("Refeição não existe"));

    MealPlan newMealPlan = MealPlan.builder().user(user).meal(meal)
            .mealType(mealPlanCreateRequestDto.mealType())
            .startDate(mealPlanCreateRequestDto.startDate())
            .endDate(mealPlanCreateRequestDto.endDate())
            .targetDays(mealPlanCreateRequestDto.targetDays()).build();

    MealPlan savedMealPlan = mealPlanRepository.save(newMealPlan);

    return MealPlanMapper.toResponseDto(savedMealPlan);
  }

  @Transactional
  public MealPlanResponseDto update(Long mealPlanId, MealPlanUpdateRequestDto mealPlanUpdateRequestDto) {
    MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
            .orElseThrow(() -> new NotFoundException("Plano alimentar não encontrado"));

    if (mealPlanUpdateRequestDto.mealId() != null) {
      Meal updateMeal = mealRepository.findById(mealPlanUpdateRequestDto.mealId())
              .orElseThrow(() -> new NotFoundException("Nova refeição informada não existe."));
      mealPlan.setMeal(updateMeal);
    }

    if (mealPlanUpdateRequestDto.mealType() != null) {
      mealPlan.setMealType(mealPlanUpdateRequestDto.mealType());
    }

    if (mealPlanUpdateRequestDto.startDate() != null) {
      mealPlan.setStartDate(mealPlanUpdateRequestDto.startDate());
    }

    if(mealPlanUpdateRequestDto.endDate() != null) {
      mealPlan.setEndDate(mealPlanUpdateRequestDto.endDate());
    }

    if(mealPlan.getStartDate().isAfter(mealPlan.getEndDate())) {
      throw new BusinessRuleException("A data inicial não pode ser posterior a data final");
    }

    if (mealPlanUpdateRequestDto.targetDays() != null) {
      mealPlan.setTargetDays(mealPlanUpdateRequestDto.targetDays());
    }

    MealPlan updatedPlanMeal = mealPlanRepository.save(mealPlan);

    return MealPlanMapper.toResponseDto(updatedPlanMeal);
  }

  public List<MealPlanResponseDto> search(Long userId, LocalDate startDate, LocalDate endDate, LocalDate date) {
    List<MealPlan> plans;

    if(date != null) {
      String dayOfWeekName = date.getDayOfWeek().name();
      if(userId != null) {
        plans = mealPlanRepository.findPlansByDayAndUser(userId, date, dayOfWeekName);
      } else {
        plans = mealPlanRepository.findPlansByDay(date, dayOfWeekName);
      }
    } else if(startDate != null && endDate != null) {
      if(userId != null) {
        plans = mealPlanRepository.findActivePlansByUserInPeriod(userId, startDate, endDate);
      } else {
       plans = mealPlanRepository.findActivePlansInPeriod(startDate, endDate);
      }
    } else if (userId != null) {
      plans = mealPlanRepository.findByUserId(userId);
    } else {
       plans = mealPlanRepository.findAll();
    }

    return plans.stream().map(MealPlanMapper::toResponseDto).toList();
  }

  @Transactional
  public void clonePeriod(MealPlanCloneRequestDto mealPlanCloneRequestDto) {
    if(mealPlanCloneRequestDto.newStartDate().isAfter(mealPlanCloneRequestDto.newEndDate())) {
      throw new BusinessRuleException("A nova data inicial não pode ser posterior a nova data final");
    }

    if(mealPlanCloneRequestDto.originalStartDate().isAfter(mealPlanCloneRequestDto.originalEndDate())) {
      throw new BusinessRuleException("A data inicial de origem não pode ser posterior a data final");
    }

    if (mealPlanRepository.existsByUserIdAndStartDateAndEndDate(mealPlanCloneRequestDto.userId(),
            mealPlanCloneRequestDto.newStartDate(), mealPlanCloneRequestDto.newEndDate())) {
      throw new ConflictException("Já existem planos alimentares cadastrados nesse período");
    }

    List<MealPlan> originalPlans =
            mealPlanRepository.findByUserIdAndStartDateAndEndDate(mealPlanCloneRequestDto.userId(),
                    mealPlanCloneRequestDto.originalStartDate(), mealPlanCloneRequestDto.originalEndDate());

    if (originalPlans.isEmpty()) {
      throw new NotFoundException("Nenhum plano alimentar encontrado para esse período");
    }

    List<MealPlan> newPlans = originalPlans.stream()
            .map(oldPlan -> MealPlan.builder()
                    .user(oldPlan.getUser()).meal(oldPlan.getMeal()).mealType(oldPlan.getMealType())
                    .targetDays(oldPlan.getTargetDays()).startDate(mealPlanCloneRequestDto.newStartDate())
                    .endDate(mealPlanCloneRequestDto.newEndDate()).build()).toList();

    mealPlanRepository.saveAll(newPlans);
  }

  public void delete(Long mealPlanId) {

    if (!mealPlanRepository.existsById(mealPlanId)) {
      throw new NotFoundException("Plano alimentar não encontrado");
    }
    mealPlanRepository.deleteById(mealPlanId);
  }
}
