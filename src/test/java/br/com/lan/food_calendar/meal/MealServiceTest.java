package br.com.lan.food_calendar.meal;

import br.com.lan.food_calendar.exception.NotFoundException;
import br.com.lan.food_calendar.meal.dto.MealCreateRequestDto;
import br.com.lan.food_calendar.meal.dto.MealResponseDto;
import br.com.lan.food_calendar.meal.dto.MealUpdateRequestDto;
import br.com.lan.food_calendar.mealplan.MealPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

  @Mock
  private MealRepository mealRepository;

  @Mock
  private MealPlanRepository mealPlanRepository;

  @InjectMocks
  private MealService mealService;

  @Test
  @DisplayName("Deve buscar refeição pelo ID")
  void shouldReturnMealById() {
    Long id = 3L;
    Meal meal = Meal.builder().id(id).name("Crepioca").description("Ovo e tapioca").build();

    when(mealRepository.findById(id)).thenReturn(Optional.of(meal));

    MealResponseDto result = mealService.findById(id);

    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo("Crepioca");
    assertThat(result.description()).isEqualTo("Ovo e tapioca");
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar refeição com ID inexistente")
  void shouldThrowNotFoundExceptionWhenMealIdDoesNotExist() {
    Long id = 99L;

    when(mealRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealService.findById(id));
  }

  @Test
  @DisplayName("Deve retornar a lista de refeições com sucesso")
  void shouldReturnMealList() {
    Meal chickenPizza = Meal.builder().id(1L).name("Mini pizza de frango").description("Massa, 50g frango, 8g muçarela, 30g molho de tomate").build();
    Meal crepioca = Meal.builder().id(2L).name("Crepioca").description("1 ovo, 1 clara de ovo, 22g de tapioca, 1 fatia " +
            "de queijo mussarela").build();
    List<Meal> meals = List.of(chickenPizza, crepioca);

    when(mealRepository.findAll()).thenReturn(meals);

    List<MealResponseDto> result = mealService.findAll();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);

    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Mini pizza de frango");
    assertThat(result.get(0).description()).isEqualTo("Massa, 50g frango, 8g muçarela, 30g molho de tomate");

    assertThat(result.get(1).id()).isEqualTo(2L);
    assertThat(result.get(1).name()).isEqualTo("Crepioca");
    assertThat(result.get(1).description()).isEqualTo("1 ovo, 1 clara de ovo, 22g de tapioca, 1 fatia de queijo mussarela");

    verify(mealRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Deve retornar uma lista vazia quando não houver refeições")
  void shouldReturnEmptyMealList() {
    when(mealRepository.findAll()).thenReturn(List.of());

    List<MealResponseDto> result = mealService.findAll();

    assertThat(result).isEmpty();

    verify(mealRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Deve retornar a refeição com base na pesquisa do usuário")
  void shouldReturnMealSearched() {
    Meal meal = Meal.builder().id(1L).name("Pizza").description("Pizza de frango").build();
    String searchedName = "PIZ";

    when(mealRepository.findByNameContainingIgnoreCase(searchedName)).thenReturn(List.of(meal));

    List<MealResponseDto> result = mealService.findByName(searchedName);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Pizza");
    assertThat(result.get(0).description()).isEqualTo("Pizza de frango");

    verify(mealRepository).findByNameContainingIgnoreCase(searchedName);
  }

  @Test
  @DisplayName("Deve salvar e retornar a refeição criada")
  void shouldReturnANewMeal() {
    MealCreateRequestDto newMeal = new MealCreateRequestDto("Crepioca", "1 ovo, tapioca, queijo");
    Meal createdMeal = Meal.builder().id(1L).name("Crepioca").description("1 ovo, tapioca, queijo").build();

    when(mealRepository.save(any(Meal.class))).thenReturn(createdMeal);

    MealResponseDto result = mealService.create(newMeal);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Crepioca");
    assertThat(result.description()).isEqualTo("1 ovo, tapioca, queijo");

    ArgumentCaptor<Meal> mealCaptor = ArgumentCaptor.forClass(Meal.class);
    verify(mealRepository).save(mealCaptor.capture());
    Meal capturedMeal = mealCaptor.getValue();

    assertThat(capturedMeal.getId()).isNull();
    assertThat(capturedMeal.getName()).isEqualTo("Crepioca");
    assertThat(capturedMeal.getDescription()).isEqualTo("1 ovo, tapioca, queijo");
  }

  @Test
  @DisplayName("Deve atualizar a refeição quando o ID for encontrado")
  void shouldUpdateMealWhenIdExists() {
    MealUpdateRequestDto requestDto = new MealUpdateRequestDto("Crepioca", "2 ovos, tapioca, requeijão");
    Meal meal = Meal.builder().id(1L).name("Tapioca").description("1 ovos, tapioca, queijo").build();

    when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
    when(mealRepository.save(same(meal))).thenReturn(meal);

    MealResponseDto result = mealService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Crepioca");
    assertThat(result.description()).isEqualTo("2 ovos, tapioca, requeijão");

    verify(mealRepository).findById(1L);
    verify(mealRepository).save(same(meal));
  }

  @Test
  @DisplayName("Deve atualizar somente o nome da refeição")
  void shouldUpdateOnlyMealName() {
    MealUpdateRequestDto requestDto = new MealUpdateRequestDto("Crepioca", null);
    Meal meal = Meal.builder().id(1L).name("Tapioca").description("1 ovo, tapioca, queijo").build();

    when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
    when(mealRepository.save(same(meal))).thenReturn(meal);

    MealResponseDto result = mealService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Crepioca");
    assertThat(result.description()).isEqualTo("1 ovo, tapioca, queijo");

    verify(mealRepository).findById(1L);
    verify(mealRepository).save(same(meal));
  }

  @Test
  @DisplayName("Deve atualizar somente a descrição da refeição")
  void shouldUpdateOnlyMealDescription() {
    MealUpdateRequestDto requestDto = new MealUpdateRequestDto(null, "2 ovos, tapioca, queijo");
    Meal meal = Meal.builder().id(1L).name("Crepioca").description("1 ovo, tapioca, queijo").build();

    when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
    when(mealRepository.save(same(meal))).thenReturn(meal);

    MealResponseDto result = mealService.update(1L, requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Crepioca");
    assertThat(result.description()).isEqualTo("2 ovos, tapioca, queijo");

    verify(mealRepository).findById(1L);
    verify(mealRepository).save(same(meal));
  }

  @Test
  @DisplayName("Deve lançar exceção quando a refeição não for encontrada")
  void shouldThrowExceptionWhenMealNotFound() {
    Long invalidId = 99L;
    MealUpdateRequestDto requestDto = new MealUpdateRequestDto("Crepioca", "2 ovos, tapioca, requeijão");

    when(mealRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealService.update(invalidId, requestDto));

    verify(mealRepository, never()).save(any(Meal.class));
  }

  @Test
  @DisplayName("Deve deletar a refeição e o plano alimentar vinculado a essa refeição")
  void shouldDeleteMealAndMealPlansWhenIdExists() {
    Meal meal = Meal.builder().id(1L).name("Crepioca").description("1 ovo, tapioca, requeijão").build();

    when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));

    mealService.delete(1L);

    verify(mealRepository).findById(1L);
    InOrder deletionOrder = inOrder(mealPlanRepository, mealRepository);
    deletionOrder.verify(mealPlanRepository).deleteAllByMealId(1L);
    deletionOrder.verify(mealRepository).delete(meal);
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando a busca não encontrar refeições")
  void shouldReturnEmptyListWhenNameDoesNotMatch() {
    when(mealRepository.findByNameContainingIgnoreCase("inexistente")).thenReturn(List.of());

    assertThat(mealService.findByName("inexistente")).isEmpty();

    verify(mealRepository).findByNameContainingIgnoreCase("inexistente");
  }

  @Test
  @DisplayName("Deve preservar a refeição quando nenhum campo for informado")
  void shouldPreserveMealWhenUpdateIsEmpty() {
    Meal meal = Meal.builder().id(1L).name("Crepioca").description("1 ovo, tapioca, queijo").build();
    when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
    when(mealRepository.save(same(meal))).thenReturn(meal);

    MealResponseDto result = mealService.update(1L, new MealUpdateRequestDto(null, null));

    assertThat(result).isEqualTo(new MealResponseDto(1L, "Crepioca", "1 ovo, tapioca, queijo"));
    verify(mealRepository).save(same(meal));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar excluir refeição inexistente")
  void shouldThrowExceptionWhenDeletingNonexistentMeal() {
    Long invalidId = 99L;

    when(mealRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> mealService.delete(invalidId));

    verify(mealRepository).findById(invalidId);
    verifyNoMoreInteractions(mealRepository);
    verifyNoInteractions(mealPlanRepository);
  }
}
