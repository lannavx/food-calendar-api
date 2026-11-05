package br.com.lan.food_calendar.mealplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

  void deleteAllByUserId(Long userId);

  void deleteAllByMealId(Long mealId);

  boolean existsByUserIdAndStartDateAndEndDate(Long userId, LocalDate startDate, LocalDate endDate);

  List<MealPlan> findByUserIdAndStartDateAndEndDate(Long userId, LocalDate startDate, LocalDate endDate);

  List<MealPlan> findByUserId(Long userId);

  @Query("SELECT mp FROM MealPlan mp WHERE mp.startDate <= :periodEnd AND mp.endDate >= :periodStart")
  List<MealPlan> findActivePlansInPeriod(@Param("periodStart") LocalDate periodStart,
                                         @Param("periodEnd") LocalDate periodEnd);

  @Query("SELECT mp FROM MealPlan mp WHERE mp.user.id = :userId AND mp.startDate <= :periodEnd AND mp.endDate >= :periodStart")
  List<MealPlan> findActivePlansByUserInPeriod(@Param("userId") Long userId,
                                               @Param("periodStart") LocalDate periodStart,
                                               @Param("periodEnd") LocalDate periodEnd);

  @Query(value = "SELECT * FROM meal_plans WHERE start_date <= :date AND end_date >= :date AND cast(:dayOfWeek as text) = ANY(target_days)", nativeQuery = true)
  List<MealPlan> findPlansByDay(@Param("date") LocalDate date, @Param("dayOfWeek") String dayOfWeek);

  @Query(value = "SELECT * FROM meal_plans WHERE user_id = :userId AND start_date <= :date AND end_date >= :date AND cast(:dayOfWeek as text) = ANY(target_days)", nativeQuery = true)
  List<MealPlan> findPlansByDayAndUser(@Param("userId") Long userId,
                                       @Param("date") LocalDate date,
                                       @Param("dayOfWeek") String dayOfWeek);
}
