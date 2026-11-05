package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.enums.MealType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

  @Mock
  private MealPlanRepository mealPlanRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MealRepository mealRepository;

  @InjectMocks
  private MealPlanService mealPlanService;

  @Test
  @DisplayName("Deve buscar plano alimentar pelo ID")
  void shouldReturnMealPlanById() {
    Long id = 3L;
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(2L).name("Crepioca").description("Ovo e tapioca").build();
    LocalDate startDate = LocalDate.of(2026, 11, 9);
    LocalDate endDate = startDate.plusDays(6);
    MealPlan mealPlan = MealPlan.builder().id(id).user(user).meal(meal).mealType(MealType.BREAKFAST)
            .startDate(startDate).endDate(endDate).targetDays(List.of(DayOfWeek.MONDAY)).build();

    when(mealPlanRepository.findById(id)).thenReturn(Optional.of(mealPlan));

    MealPlanResponseDto result = mealPlanService.findById(id);

    assertThat(result.id()).isEqualTo(id);
    assertThat(result.user().id()).isEqualTo(1L);
    assertThat(result.meal().id()).isEqualTo(2L);
    assertThat(result.mealType()).isEqualTo(MealType.BREAKFAST);
    assertThat(result.startDate()).isEqualTo(startDate);
    assertThat(result.endDate()).isEqualTo(endDate);
    assertThat(result.targetDays()).containsExactly(DayOfWeek.MONDAY);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar plano alimentar com ID inexistente")
  void shouldThrowNotFoundExceptionWhenMealPlanIdDoesNotExist() {
    Long id = 99L;

    when(mealPlanRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealPlanService.findById(id));
  }

  @Test
  @DisplayName("Deve rejeitar criação do plano com data inicial posterior à final")
  void shouldRejectMealPlanWhenStartDateIsAfterEndDate() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 10);
    MealPlanCreateRequestDto requestDto = new MealPlanCreateRequestDto(1L, 2L,
            MealType.DINNER, startDate, endDate, List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

    assertThrows(BusinessRuleException.class, () -> mealPlanService.create(requestDto));

    verifyNoInteractions(userRepository, mealRepository, mealPlanRepository);
  }

  @Test
  @DisplayName("Deve rejeitar criação do plano com usuário inexistente")
  void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
    Long invalidId = 99L;
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    MealPlanCreateRequestDto requestDto = new MealPlanCreateRequestDto(invalidId, 2L,
            MealType.DINNER, startDate, endDate, List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

    when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealPlanService.create(requestDto));

    verify(userRepository).findById(invalidId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(mealRepository, mealPlanRepository);
  }

  @Test
  @DisplayName("Deve rejeitar criação do plano com refeição inexistente")
  void shouldThrowNotFoundExceptionWhenMealDoesNotExist() {
    Long invalidId = 99L;
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    User user = User.builder().id(1L).name("Ana").build();
    MealPlanCreateRequestDto requestDto = new MealPlanCreateRequestDto(1L, invalidId,
            MealType.DINNER, startDate, endDate, List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(mealRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealPlanService.create(requestDto));

    verify(userRepository).findById(1L);
    verifyNoMoreInteractions(userRepository);

    verify(mealRepository).findById(invalidId);
    verifyNoMoreInteractions(mealRepository);

    verifyNoInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve salvar e retornar o plano criado")
  void shouldCreateMealPlanWhenDataIsValid() {
    LocalDate startDate = LocalDate.of(2026, 11, 20);
    LocalDate endDate = LocalDate.of(2026, 11, 30);
    List<DayOfWeek> targetDays = List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(2L).name("Crepioca").description("1 ovo, tapioca, queijo").build();
    MealPlanCreateRequestDto requestDto = new MealPlanCreateRequestDto(1L, 2L, MealType.DINNER, startDate, endDate, targetDays);
    MealPlan savedMealPlan = createDefaultMealPlan(user, meal);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(mealRepository.findById(2L)).thenReturn(Optional.of(meal));

    when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(savedMealPlan);

    MealPlanResponseDto result = mealPlanService.create(requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.startDate()).isEqualTo(startDate);
    assertThat(result.endDate()).isEqualTo(endDate);
    assertThat(result.user().id()).isEqualTo(1L);
    assertThat(result.user().name()).isEqualTo("Ana");
    assertThat(result.meal().id()).isEqualTo(2L);
    assertThat(result.meal().name()).isEqualTo("Crepioca");
    assertThat(result.meal().description()).isEqualTo("1 ovo, tapioca, queijo");

    assertThat(result.mealType()).isEqualTo(MealType.DINNER);
    assertThat(result.targetDays()).isEqualTo(targetDays);

    ArgumentCaptor<MealPlan> planArgumentCaptor = ArgumentCaptor.forClass(MealPlan.class);
    verify(mealPlanRepository).save(planArgumentCaptor.capture());
    MealPlan captureMealPlan = planArgumentCaptor.getValue();

    assertThat(captureMealPlan.getId()).isNull();
    assertThat(captureMealPlan.getUser()).isSameAs(user);
    assertThat(captureMealPlan.getMeal()).isSameAs(meal);
    assertThat(captureMealPlan.getMealType()).isEqualTo(MealType.DINNER);
    assertThat(captureMealPlan.getStartDate()).isEqualTo(startDate);
    assertThat(captureMealPlan.getEndDate()).isEqualTo(endDate);
    assertThat(captureMealPlan.getTargetDays()).isEqualTo(targetDays);

    verify(userRepository).findById(1L);
    verify(mealRepository).findById(2L);
  }

  @Test
  @DisplayName("Deve atualizar todos os campos do plano alimentar")
  void shouldUpdateAllMealPlanFields() {
    LocalDate newStartDate = LocalDate.of(2026, 12, 20);
    LocalDate newEndDate = LocalDate.of(2026, 12, 30);
    List<DayOfWeek> newTargetDays = List.of(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);
    User user = User.builder().id(1L).name("Ana").build();
    Meal oldMeal = Meal.builder().id(1L).name("Crepioca").build();
    Meal newMeal = Meal.builder().id(2L).name("PF tradicional").build();
    MealPlan currentMealPlan = createDefaultMealPlan(user, oldMeal);
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(2L, MealType.LUNCH, newStartDate, newEndDate,
            newTargetDays);

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(currentMealPlan));
    when(mealRepository.findById(2L)).thenReturn(Optional.of(newMeal));

    when(mealPlanRepository.save(same(currentMealPlan))).thenReturn(currentMealPlan);

    MealPlanResponseDto result = mealPlanService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.meal().id()).isEqualTo(2L);
    assertThat(result.meal().name()).isEqualTo("PF tradicional");
    assertThat(result.mealType()).isEqualTo(MealType.LUNCH);
    assertThat(result.startDate()).isEqualTo(newStartDate);
    assertThat(result.endDate()).isEqualTo(newEndDate);
    assertThat(result.targetDays()).isEqualTo(newTargetDays);

    ArgumentCaptor<MealPlan> planArgumentCaptor = ArgumentCaptor.forClass(MealPlan.class);
    verify(mealPlanRepository).save(planArgumentCaptor.capture());
    MealPlan captureMealPlan = planArgumentCaptor.getValue();

    assertThat(captureMealPlan.getId()).isEqualTo(1L);
    assertThat(captureMealPlan.getUser()).isSameAs(user);
    assertThat(captureMealPlan.getMeal()).isSameAs(newMeal);
    assertThat(captureMealPlan.getMealType()).isEqualTo(MealType.LUNCH);
    assertThat(captureMealPlan.getStartDate()).isEqualTo(newStartDate);
    assertThat(captureMealPlan.getEndDate()).isEqualTo(newEndDate);
    assertThat(captureMealPlan.getTargetDays()).isEqualTo(newTargetDays);

    verify(mealPlanRepository).findById(1L);
    verify(mealRepository).findById(2L);

  }

  @Test
  @DisplayName("Deve atualizar somente a refeição vinculada ao plano alimentar")
  void shouldUpdateOnlyMeal() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal oldMeal = Meal.builder().id(1L).name("Crepioca").build();
    Meal newMeal = Meal.builder().id(2L).name("PF tradicional").build();
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(2L, null, null, null, null);
    MealPlan mealPlan = createDefaultMealPlan(user, oldMeal);

    MealType originalMealType = mealPlan.getMealType();
    LocalDate originalStartDate = mealPlan.getStartDate();
    LocalDate originalEndDate = mealPlan.getEndDate();
    List<DayOfWeek> originalTargetDays =
            List.copyOf(mealPlan.getTargetDays());

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));
    when(mealRepository.findById(2L)).thenReturn(Optional.of(newMeal));
    when(mealPlanRepository.save(same(mealPlan))).thenReturn(mealPlan);

    MealPlanResponseDto result = mealPlanService.update(1L, requestDto);

    assertThat(result.meal().id()).isEqualTo(2L);
    assertThat(mealPlan.getMeal()).isSameAs(newMeal);
    assertThat(result.id()).isEqualTo(1L);

    assertThat(result.mealType()).isEqualTo(originalMealType);
    assertThat(result.startDate()).isEqualTo(originalStartDate);
    assertThat(result.endDate()).isEqualTo(originalEndDate);
    assertThat(result.targetDays()).isEqualTo(originalTargetDays);

    assertThat(mealPlan.getId()).isEqualTo(1L);
    assertThat(mealPlan.getUser()).isSameAs(user);

    verify(mealPlanRepository).findById(1L);
    verify(mealRepository).findById(2L);
    verify(mealPlanRepository).save(same(mealPlan));
  }

  @Test
  @DisplayName("Deve atualizar somente o tipo da refeição e preservar os demais campos")
  void shouldUpdateOnlyMealType() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(null, MealType.BREAKFAST, null, null, null);
    MealPlan mealPlan = createDefaultMealPlan(user, meal);

    LocalDate originalStartDate = mealPlan.getStartDate();
    LocalDate originalEndDate = mealPlan.getEndDate();
    List<DayOfWeek> originalTargetDays =
            List.copyOf(mealPlan.getTargetDays());

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));
    when(mealPlanRepository.save(same(mealPlan))).thenReturn(mealPlan);

    MealPlanResponseDto result = mealPlanService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.meal().id()).isEqualTo(meal.getId());
    assertThat(result.startDate()).isEqualTo(originalStartDate);
    assertThat(result.endDate()).isEqualTo(originalEndDate);
    assertThat(result.targetDays()).isEqualTo(originalTargetDays);
    assertThat(result.mealType()).isEqualTo(MealType.BREAKFAST);

    assertThat(mealPlan.getUser()).isSameAs(user);
    assertThat(mealPlan.getMeal()).isSameAs(meal);
    assertThat(mealPlan.getMealType()).isEqualTo(MealType.BREAKFAST);


    verify(mealPlanRepository).findById(1L);
    verify(mealPlanRepository).save(same(mealPlan));
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve atualizar somente os dias da semana do plano alimentar")
  void shouldUpdateOnlyTargetDays() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    List<DayOfWeek> newTargetDays = List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY );
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(null, null, null, null, newTargetDays);
    MealPlan mealPlan = createDefaultMealPlan(user, meal);

    MealType originalMealType = mealPlan.getMealType();
    LocalDate originalStartDate = mealPlan.getStartDate();
    LocalDate originalEndDate = mealPlan.getEndDate();

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));
    when(mealPlanRepository.save(same(mealPlan))).thenReturn(mealPlan);

    MealPlanResponseDto result = mealPlanService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.meal().id()).isEqualTo(meal.getId());
    assertThat(result.mealType()).isEqualTo(originalMealType);
    assertThat(result.startDate()).isEqualTo(originalStartDate);
    assertThat(result.endDate()).isEqualTo(originalEndDate);
    assertThat(result.targetDays()).isEqualTo(newTargetDays);

    assertThat(mealPlan.getUser()).isSameAs(user);
    assertThat(mealPlan.getMeal()).isSameAs(meal);
    assertThat(mealPlan.getTargetDays()).isEqualTo(newTargetDays);

    verify(mealPlanRepository).findById(1L);
    verify(mealPlanRepository).save(same(mealPlan));
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve lançar NotFoundException quando o plano alimentar não existir")
  void shouldThrowNotFoundExceptionWhenMealPlanDoesNotExist() {
    Long invalidId = 99L;
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(2L, null, null, null, null);

    when(mealPlanRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealPlanService.update(invalidId, requestDto));

    verify(mealPlanRepository).findById(invalidId);
    verifyNoMoreInteractions(mealPlanRepository);
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve lançar NotFoundException quando a nova refeição não existir")
  void shouldThrowNotFoundExceptionWhenNewMealDoesNotExist() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    Long invalidMealId = 99L;
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(invalidMealId, null, null, null, null);

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));
    when(mealRepository.findById(invalidMealId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealPlanService.update(1L, requestDto));

    verify(mealPlanRepository).findById(1L);
    verifyNoMoreInteractions(mealPlanRepository);

    verify(mealRepository).findById(invalidMealId);
    verifyNoMoreInteractions(mealRepository);

    assertThat(mealPlan.getMeal()).isSameAs(meal);
  }

  @Test
  @DisplayName("Deve rejeitar atualização quando ambas as datas informadas formarem um período inválido")
  void shouldRejectUpdateWhenDateRangeIsInvalid() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    LocalDate startDate = LocalDate.of(2026, 12, 30);
    LocalDate endDate = LocalDate.of(2026, 12, 20);
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(null, null, startDate, endDate, null);

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));

    assertThrows(BusinessRuleException.class, () -> mealPlanService.update(1L, requestDto));

    verify(mealPlanRepository).findById(1L);
    verifyNoMoreInteractions(mealPlanRepository);
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve rejeitar nova data inicial posterior à data final existente")
  void shouldRejectStartDateAfterExistingEndDate() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    LocalDate startDate = LocalDate.of(2026, 12, 1);
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(null, null, startDate, null, null);

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));

    assertThrows(BusinessRuleException.class, () -> mealPlanService.update(1L, requestDto));

    verify(mealPlanRepository).findById(1L);
    verifyNoMoreInteractions(mealPlanRepository);
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve rejeitar nova data final anterior à data inicial existente")
  void shouldRejectEndDateBeforeExistingStartDate() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    LocalDate endDate = LocalDate.of(2026, 11, 10);
    MealPlanUpdateRequestDto requestDto = new MealPlanUpdateRequestDto(null, null, null, endDate, null);

    when(mealPlanRepository.findById(1L)).thenReturn(Optional.of(mealPlan));

    assertThrows(BusinessRuleException.class, () -> mealPlanService.update(1L, requestDto));

    verify(mealPlanRepository).findById(1L);
    verifyNoMoreInteractions(mealPlanRepository);
    verifyNoInteractions(mealRepository);
  }

  @Test
  @DisplayName("Deve buscar planos alimentares por data e usuário")
  void shouldSearchMealPlansByDateAndUser() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    Long userId = user.getId();
    LocalDate date = LocalDate.of(2026, 11, 23);
    String dayOfWeekName = date.getDayOfWeek().name();

    when(mealPlanRepository.findPlansByDayAndUser(userId, date, dayOfWeekName)).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(userId, null,null, date);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());
    assertThat(result.get(0).user().id()).isEqualTo(userId);
    assertThat(result.get(0).meal().name()).isEqualTo("Crepioca");

    verify(mealPlanRepository).findPlansByDayAndUser(userId, date, dayOfWeekName);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve buscar planos alimentares por data para todos os usuários")
  void shouldSearchMealPlansByDateForAllUsers() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    LocalDate date = LocalDate.of(2026, 11, 23);
    String dayOfWeekName = date.getDayOfWeek().name();

    when(mealPlanRepository.findPlansByDay(date, dayOfWeekName)).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(null, null,null, date);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());
    assertThat(result.get(0).meal().name()).isEqualTo("Crepioca");

    verify(mealPlanRepository).findPlansByDay(date, dayOfWeekName);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve buscar planos alimentares por período e usuário")
  void shouldSearchMealPlansByPeriodAndUser() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    Long userId = user.getId();
    LocalDate periodStart = LocalDate.of(2026, 11, 23);
    LocalDate periodEnd = LocalDate.of(2026, 11, 27);

    when(mealPlanRepository.findActivePlansByUserInPeriod(userId, periodStart, periodEnd)).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(userId, periodStart, periodEnd, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());
    assertThat(result.get(0).user().id()).isEqualTo(userId);
    assertThat(result.get(0).meal().name()).isEqualTo("Crepioca");

    verify(mealPlanRepository).findActivePlansByUserInPeriod(userId, periodStart, periodEnd);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve buscar planos alimentares por período para todos os usuários")
  void shouldSearchMealPlansByPeriodForAllUsers() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    LocalDate periodStart = LocalDate.of(2026, 11, 23);
    LocalDate periodEnd = LocalDate.of(2026, 11, 27);

    when(mealPlanRepository.findActivePlansInPeriod(periodStart, periodEnd)).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(null, periodStart, periodEnd, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());
    assertThat(result.get(0).meal().name()).isEqualTo("Crepioca");

    verify(mealPlanRepository).findActivePlansInPeriod(periodStart, periodEnd);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve buscar somente os planos do usuário quando nenhuma data for informada")
  void shouldSearchMealPlansByUserWithoutDates() {
    User user = User.builder().id(2L).name("Ana").build();
    Meal meal = Meal.builder().id(3L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);

    when(mealPlanRepository.findByUserId(2L)).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(2L, null, null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());
    assertThat(result.get(0).user().id()).isEqualTo(2L);
    assertThat(result.get(0).meal().id()).isEqualTo(3L);

    verify(mealPlanRepository).findByUserId(2L);
    verify(mealPlanRepository, never()).findAll();
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando o usuário não possuir planos")
  void shouldReturnEmptyListWhenUserHasNoMealPlans() {
    when(mealPlanRepository.findByUserId(2L)).thenReturn(List.of());

    List<MealPlanResponseDto> result = mealPlanService.search(2L, null, null, null);

    assertThat(result).isEmpty();
    verify(mealPlanRepository).findByUserId(2L);
    verify(mealPlanRepository, never()).findAll();
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve retornar todos os planos alimentares quando nenhum filtro for informado")
  void shouldReturnAllMealPlansWhenNoFiltersProvided() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);

    when(mealPlanRepository.findAll()).thenReturn(List.of(mealPlan));

    List<MealPlanResponseDto> result = mealPlanService.search(null, null,null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(mealPlan.getId());

    verify(mealPlanRepository).findAll();
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não houver planos no período pesquisado")
  void shouldReturnEmptySearchResults() {
    Long userId = 1L;
    LocalDate start = LocalDate.of(2026, 11, 20);
    LocalDate end = LocalDate.of(2026, 11, 30);

    when(mealPlanRepository.findActivePlansByUserInPeriod(userId, start, end)).thenReturn(List.of());

    assertThat(mealPlanService.search(userId, start, end, null)).isEmpty();

    verify(mealPlanRepository).findActivePlansByUserInPeriod(userId, start, end);
  }

  @Test
  @DisplayName("Deve clonar os planos alimentares para um novo período")
  void shouldCloneMealPlansToNewPeriod() {
    User user = User.builder().id(1L).name("Ana").build();
    Meal meal = Meal.builder().id(1L).name("Crepioca").build();
    MealPlan mealPlan = createDefaultMealPlan(user, meal);
    Long userId = user.getId();
    LocalDate newStartDate = LocalDate.of(2026, 12, 20);
    LocalDate newEndDate = LocalDate.of(2026, 12, 30);
    MealPlanCloneRequestDto requestDto = new MealPlanCloneRequestDto(mealPlan.getUser().getId(),
            mealPlan.getStartDate(), mealPlan.getEndDate(), newStartDate, newEndDate);

    when(mealPlanRepository.existsByUserIdAndStartDateAndEndDate(userId, newStartDate, newEndDate)).thenReturn(false);
    when(mealPlanRepository.findByUserIdAndStartDateAndEndDate(userId, mealPlan.getStartDate(), mealPlan.getEndDate())).thenReturn(List.of(mealPlan));

    mealPlanService.clonePeriod(requestDto);

    ArgumentCaptor<List<MealPlan>> plansArgumentCaptor = ArgumentCaptor.captor();
    verify(mealPlanRepository).saveAll(plansArgumentCaptor.capture());
    List<MealPlan> capturedPlans = plansArgumentCaptor.getValue();

    assertThat(capturedPlans).hasSize(1);

    MealPlan clonedPlan = capturedPlans.get(0);

    assertThat(clonedPlan).isNotSameAs(mealPlan);
    assertThat(clonedPlan.getId()).isNull();

    assertThat(clonedPlan.getUser()).isSameAs(user);
    assertThat(clonedPlan.getMeal()).isSameAs(meal);
    assertThat(clonedPlan.getMealType()).isEqualTo(mealPlan.getMealType());
    assertThat(clonedPlan.getTargetDays()).isEqualTo(mealPlan.getTargetDays());

    assertThat(clonedPlan.getStartDate()).isEqualTo(newStartDate);
    assertThat(clonedPlan.getEndDate()).isEqualTo(newEndDate);
  }

  @Test
  @DisplayName("Deve rejeitar clonagem quando o novo período possuir datas inválidas")
  void shouldRejectCloneWhenNewPeriodIsInvalid() {
    Long userId = 1L;
    LocalDate originalStartDate = LocalDate.of(2026, 11, 20);
    LocalDate originalEndDate = LocalDate.of(2026, 11, 30);
    LocalDate newStartDate = LocalDate.of(2026, 12, 30);
    LocalDate newEndDate = LocalDate.of(2026, 12, 20);

    MealPlanCloneRequestDto requestDto = new MealPlanCloneRequestDto(userId, originalStartDate, originalEndDate, newStartDate, newEndDate);

    assertThrows(BusinessRuleException.class, () -> mealPlanService.clonePeriod(requestDto));

    verifyNoInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve rejeitar clonagem quando o período de origem possuir datas inválidas")
  void shouldRejectCloneWhenOriginalPeriodIsInvalid() {
    Long userId = 1L;
    LocalDate originalStartDate = LocalDate.of(2026, 11, 30);
    LocalDate originalEndDate = LocalDate.of(2026, 11, 20);
    LocalDate newStartDate = LocalDate.of(2026, 12, 20);
    LocalDate newEndDate = LocalDate.of(2026, 12, 30);

    MealPlanCloneRequestDto requestDto = new MealPlanCloneRequestDto(userId, originalStartDate, originalEndDate, newStartDate, newEndDate);

    assertThrows(BusinessRuleException.class, () -> mealPlanService.clonePeriod(requestDto));

    verifyNoInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve lançar ConflictException quando já existirem planos no período de destino")
  void shouldThrowConflictExceptionWhenTargetPeriodAlreadyExists() {
    Long userId = 1L;
    LocalDate originalStartDate = LocalDate.of(2026, 11, 20);
    LocalDate originalEndDate = LocalDate.of(2026, 11, 30);
    LocalDate newStartDate = LocalDate.of(2026, 12, 20);
    LocalDate newEndDate = LocalDate.of(2026, 12, 30);

    MealPlanCloneRequestDto requestDto = new MealPlanCloneRequestDto(userId, originalStartDate, originalEndDate, newStartDate, newEndDate);

    when(mealPlanRepository.existsByUserIdAndStartDateAndEndDate(userId, newStartDate, newEndDate)).thenReturn(true);

    assertThrows(ConflictException.class, () -> mealPlanService.clonePeriod(requestDto));

    verify(mealPlanRepository).existsByUserIdAndStartDateAndEndDate(userId, newStartDate, newEndDate);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve lançar NotFoundException quando não existirem planos no período de origem")
  void shouldThrowNotFoundExceptionWhenOriginalPeriodIsEmpty() {
    Long userId = 1L;
    LocalDate originalStartDate = LocalDate.of(2026, 11, 20);
    LocalDate originalEndDate = LocalDate.of(2026, 11, 30);
    LocalDate newStartDate = LocalDate.of(2026, 12, 20);
    LocalDate newEndDate = LocalDate.of(2026, 12, 30);

    MealPlanCloneRequestDto requestDto = new MealPlanCloneRequestDto(userId, originalStartDate, originalEndDate, newStartDate, newEndDate);

    when(mealPlanRepository.existsByUserIdAndStartDateAndEndDate(userId, newStartDate, newEndDate)).thenReturn(false);
    when(mealPlanRepository.findByUserIdAndStartDateAndEndDate(userId, originalStartDate, originalEndDate)).thenReturn(List.of());

    assertThrows(NotFoundException.class, () -> mealPlanService.clonePeriod(requestDto));

    verify(mealPlanRepository).existsByUserIdAndStartDateAndEndDate(userId, newStartDate, newEndDate);
    verify(mealPlanRepository).findByUserIdAndStartDateAndEndDate(userId, originalStartDate, originalEndDate);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  @Test
  @DisplayName("Deve excluir o plano alimentar quando o ID existir")
  void shouldDeleteMealPlanWhenIdExists() {
    Long mealPlanId = 1L;

    when(mealPlanRepository.existsById(mealPlanId)).thenReturn(true);

    mealPlanService.delete(mealPlanId);

    verify(mealPlanRepository).existsById(mealPlanId);
    verify(mealPlanRepository).deleteById(mealPlanId);
  }

  @Test
  @DisplayName("Deve lançar NotFoundException ao excluir plano alimentar inexistente")
  void shouldThrowNotFoundExceptionWhenDeletingUnknownMealPlan() {
    Long invalidId = 99L;

    when(mealPlanRepository.existsById(invalidId)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> mealPlanService.delete(invalidId));

    verify(mealPlanRepository).existsById(invalidId);
    verifyNoMoreInteractions(mealPlanRepository);
  }

  private MealPlan createDefaultMealPlan(User user, Meal meal) {
    return MealPlan.builder().id(1L).user(user).meal(meal)
            .mealType(MealType.DINNER)
            .startDate(LocalDate.of(2026, 11, 20))
            .endDate(LocalDate.of(2026, 11, 30))
            .targetDays(List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY))
            .build();
  }
}
