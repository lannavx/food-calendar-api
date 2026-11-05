package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.enums.MealType;
import br.com.lan.food_calendar.meal.Meal;
import br.com.lan.food_calendar.meal.MealRepository;
import br.com.lan.food_calendar.user.User;
import br.com.lan.food_calendar.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class MealPlanRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired
  private MealPlanRepository mealPlanRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MealRepository mealRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("Deve verificar se existe um plano alimentar para o usuário no período informado")
  void shouldReturnTrueWhenMealPlanExistsForUserAndPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mealPlan = buildMealPlan(user, meal, startDate, endDate, DayOfWeek.MONDAY);

    mealPlanRepository.saveAndFlush(mealPlan);

    boolean exists = mealPlanRepository.existsByUserIdAndStartDateAndEndDate(user.getId(), startDate, endDate);

    assertTrue(exists);
  }

  @Test
  @DisplayName("Deve retornar false quando não existir plano no período informado")
  void shouldReturnFalseWhenMealPlanDoesNotExistForPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchStartDate = LocalDate.of(2026, 12, 1);
    LocalDate searchEndDate = LocalDate.of(2026, 12, 10);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal =  mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mealPlan = buildMealPlan(user, meal, startDate, endDate, DayOfWeek.MONDAY);

    mealPlanRepository.saveAndFlush(mealPlan);

    boolean exists =  mealPlanRepository.existsByUserIdAndStartDateAndEndDate(user.getId(), searchStartDate, searchEndDate);

    assertFalse(exists);
  }

  @Test
  @DisplayName("Deve encontrar planos alimentares ativos no período pesquisado")
  void shouldFindActiveMealPlansInPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchStartDate = LocalDate.of(2026, 11, 25);
    LocalDate searchEndDate = LocalDate.of(2026, 12, 5);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal =  mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan savedMealPlan = mealPlanRepository.saveAndFlush(buildMealPlan(user, meal, startDate, endDate, DayOfWeek.MONDAY));

    List<MealPlan> result = mealPlanRepository.findActivePlansInPeriod(searchStartDate, searchEndDate);

    assertEquals(1, result.size());
    assertEquals(savedMealPlan.getId(), result.get(0).getId());

  }

  @Test
  @DisplayName("Não deve retornar planos fora do período pesquisado")
  void shouldNotFindMealPlansOutsidePeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchStartDate = LocalDate.of(2026, 12, 5);
    LocalDate searchEndDate = LocalDate.of(2026, 12, 15);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal =  mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mealPlan = buildMealPlan(user, meal, startDate, endDate, DayOfWeek.MONDAY);

    mealPlanRepository.saveAndFlush(mealPlan);

    List<MealPlan> result = mealPlanRepository.findActivePlansInPeriod(searchStartDate, searchEndDate);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve incluir planos que terminam no início ou começam no fim do período pesquisado")
  void shouldIncludeMealPlansAtPeriodBoundaries() {
    LocalDate searchStartDate = LocalDate.of(2026, 11, 23);
    LocalDate searchEndDate = LocalDate.of(2026, 11, 30);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan endsAtStart = buildMealPlan(user, meal, searchStartDate.minusWeeks(1), searchStartDate, DayOfWeek.MONDAY);
    MealPlan startsAtEnd = buildMealPlan(user, meal, searchEndDate, searchEndDate.plusWeeks(1), DayOfWeek.MONDAY);
    MealPlan startsAfterEnd = buildMealPlan(user, meal, searchEndDate.plusDays(1), searchEndDate.plusWeeks(1), DayOfWeek.MONDAY);

    mealPlanRepository.saveAllAndFlush(List.of(endsAtStart, startsAtEnd, startsAfterEnd));

    List<MealPlan> result = mealPlanRepository.findActivePlansInPeriod(searchStartDate, searchEndDate);

    assertThat(result).extracting(MealPlan::getId).containsExactlyInAnyOrder(endsAtStart.getId(), startsAtEnd.getId());
  }

  @Test
  @DisplayName("Deve retornar apenas os planos do usuário no período pesquisado")
  void shouldFindActiveMealPlansByUserInPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchStartDate = LocalDate.of(2026, 11, 25);
    LocalDate searchEndDate = LocalDate.of(2026, 11, 27);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));

    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan anaPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);

    MealPlan carlosPlan = buildMealPlan(carlos, meal, startDate, endDate, DayOfWeek.MONDAY);

    List<MealPlan> savedMealPlans = mealPlanRepository.saveAllAndFlush(List.of(anaPlan, carlosPlan));

    List<MealPlan> result = mealPlanRepository.findActivePlansByUserInPeriod(ana.getId(),
            searchStartDate, searchEndDate);

    assertEquals(1, result.size());
    assertEquals(savedMealPlans.get(0).getId(), result.get(0).getId());
  }

  @Test
  @DisplayName("Não deve retornar planos de outro usuário no período pesquisado")
  void shouldNotFindPlansFromAnotherUserInPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchStartDate = LocalDate.of(2026, 12, 5);
    LocalDate searchEndDate = LocalDate.of(2026, 12, 10);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));

    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan anaOutOfPeriodPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);

    MealPlan carlosInPeriodPlan = buildMealPlan(carlos, meal, LocalDate.of(2026, 12, 5),
            LocalDate.of(2026, 12, 15), DayOfWeek.MONDAY);

    mealPlanRepository.saveAllAndFlush(List.of(anaOutOfPeriodPlan, carlosInPeriodPlan));

    List<MealPlan> result = mealPlanRepository.findActivePlansByUserInPeriod(ana.getId(),
            searchStartDate, searchEndDate);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar apenas planos programados para o dia da semana pesquisado")
  void shouldFindMealPlansByDayOfWeek() {
    LocalDate searchDate = LocalDate.of(2026, 11, 23);
    String dayOfWeekName = searchDate.getDayOfWeek().name();

    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mondayPlan = buildMealPlan(user, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan tuesdayPlan = buildMealPlan(user, meal, startDate, endDate, DayOfWeek.TUESDAY);

    List<MealPlan> savedMealPlans = mealPlanRepository.saveAllAndFlush(List.of(mondayPlan, tuesdayPlan));

    entityManager.clear();

    List<MealPlan> result = mealPlanRepository.findPlansByDay(searchDate, dayOfWeekName);

    assertEquals(1, result.size());
    assertEquals(savedMealPlans.getFirst().getId(), result.getFirst().getId());
    assertEquals(List.of(DayOfWeek.MONDAY), result.get(0).getTargetDays());
  }

  @Test
  @DisplayName("Não deve retornar planos programados para o dia da semana fora do período de validade")
  void shouldNotFindMealPlansOutsideDateRange() {
    LocalDate searchDate = LocalDate.of(2026, 11, 23);
    String dayOfWeekName = searchDate.getDayOfWeek().name();

    User user = userRepository.saveAndFlush(buildUser("Ana"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mealPlan = buildMealPlan(user, meal, LocalDate.of(2026, 12, 1),
            LocalDate.of(2026, 12, 10), DayOfWeek.MONDAY);

    mealPlanRepository.saveAndFlush(mealPlan);

    List<MealPlan> result = mealPlanRepository.findPlansByDay(searchDate, dayOfWeekName);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar apenas os planos do usuário programados para o dia pesquisado")
  void shouldFindMealPlansByDayAndUser() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    LocalDate searchDate = LocalDate.of(2026, 11, 23);
    String dayOfWeekName = searchDate.getDayOfWeek().name();

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));

    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan mealPlan1 = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan mealPlan2 = buildMealPlan(carlos, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan mealPlan3 = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.TUESDAY);
    MealPlan expiredPlan = buildMealPlan(ana, meal, startDate, searchDate.minusDays(1), DayOfWeek.MONDAY);
    MealPlan futurePlan = buildMealPlan(ana, meal, searchDate.plusDays(1), endDate, DayOfWeek.MONDAY);

    List<MealPlan> savedMealPlans = mealPlanRepository.saveAllAndFlush(
            List.of(mealPlan1, mealPlan2, mealPlan3, expiredPlan, futurePlan));

    List<MealPlan> result = mealPlanRepository.findPlansByDayAndUser(ana.getId(), searchDate, dayOfWeekName);

    assertEquals(1, result.size());
    assertEquals(savedMealPlans.getFirst().getId(), result.getFirst().getId());
  }

  @Test
  @DisplayName("Deve retornar apenas os planos do usuário quando a busca não informar datas")
  void shouldFindMealPlansByUserId() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan firstPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan secondPlan = buildMealPlan(ana, meal, startDate.plusWeeks(2), endDate.plusWeeks(2), DayOfWeek.MONDAY);
    MealPlan anotherUsersPlan = buildMealPlan(carlos, meal, startDate, endDate, DayOfWeek.MONDAY);
    mealPlanRepository.saveAllAndFlush(List.of(firstPlan, secondPlan, anotherUsersPlan));

    List<MealPlan> result = mealPlanRepository.findByUserId(ana.getId());

    assertThat(result).extracting(MealPlan::getId)
            .containsExactlyInAnyOrder(firstPlan.getId(), secondPlan.getId());
  }

  @Test
  @DisplayName("Deve retornar apenas os planos do usuário com as duas datas iguais ao período informado")
  void shouldFindMealPlansByUserAndExactPeriod() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan exactPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan differentStart = buildMealPlan(ana, meal, startDate.minusDays(1), endDate, DayOfWeek.MONDAY);
    MealPlan differentEnd = buildMealPlan(ana, meal, startDate, endDate.plusDays(1), DayOfWeek.MONDAY);
    MealPlan anotherUsersPlan = buildMealPlan(carlos, meal, startDate, endDate, DayOfWeek.MONDAY);

    mealPlanRepository.saveAllAndFlush(List.of(exactPlan, differentStart, differentEnd, anotherUsersPlan));

    List<MealPlan> result = mealPlanRepository.findByUserIdAndStartDateAndEndDate(ana.getId(), startDate, endDate);

    assertThat(result).extracting(MealPlan::getId).containsExactly(exactPlan.getId());
  }

  @Test
  @DisplayName("Deve excluir todos os planos do usuário e preservar os planos dos demais")
  void shouldDeleteOnlyMealPlansOfGivenUser() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));
    Meal meal = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));

    MealPlan firstPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan secondPlan = buildMealPlan(ana, meal, startDate, endDate, DayOfWeek.TUESDAY);
    MealPlan remainingPlan = buildMealPlan(carlos, meal, startDate, endDate, DayOfWeek.MONDAY);
    mealPlanRepository.saveAllAndFlush(List.of(firstPlan, secondPlan, remainingPlan));

    mealPlanRepository.deleteAllByUserId(ana.getId());
    mealPlanRepository.flush();
    entityManager.clear();

    assertThat(mealPlanRepository.findAll()).extracting(MealPlan::getId).containsExactly(remainingPlan.getId());
  }

  @Test
  @DisplayName("Deve excluir todos os planos da refeição e preservar os planos das demais")
  void shouldDeleteOnlyMealPlansOfGivenMeal() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);

    User ana = userRepository.saveAndFlush(buildUser("Ana"));
    User carlos = userRepository.saveAndFlush(buildUser("Carlos"));
    Meal chicken = mealRepository.saveAndFlush(buildMeal("Frango grelhado", "Frango preparado na frigideira"));
    Meal salad = mealRepository.saveAndFlush(buildMeal("Salada", "Alface e tomate"));

    MealPlan firstPlan = buildMealPlan(ana, chicken, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan secondPlan = buildMealPlan(carlos, chicken, startDate, endDate, DayOfWeek.MONDAY);
    MealPlan remainingPlan = buildMealPlan(ana, salad, startDate, endDate, DayOfWeek.MONDAY);
    mealPlanRepository.saveAllAndFlush(List.of(firstPlan, secondPlan, remainingPlan));

    mealPlanRepository.deleteAllByMealId(chicken.getId());
    mealPlanRepository.flush();
    entityManager.clear();

    assertThat(mealPlanRepository.findAll()).extracting(MealPlan::getId).containsExactly(remainingPlan.getId());
  }

  private User buildUser(String name) {
    return User.builder().name(name).build();
  }

  private Meal buildMeal(String name, String description) {
    return Meal.builder().name(name).description(description).build();
  }

  private MealPlan buildMealPlan(User user, Meal meal, LocalDate startDate, LocalDate endDate, DayOfWeek... days) {
    return MealPlan.builder().user(user).meal(meal).mealType(MealType.DINNER).startDate(startDate).endDate(endDate)
            .targetDays(List.of(days)).build();
  }

}
