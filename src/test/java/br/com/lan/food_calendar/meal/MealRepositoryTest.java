package br.com.lan.food_calendar.meal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class MealRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired
  private MealRepository mealRepository;

  @Test
  @DisplayName("Deve buscar refeições pelo nome ignorando maiúsculas e minúsculas")
  void shouldFindMealByNameIgnoringCase() {
    Meal meal = Meal.builder().name("Frango grelhado").description("Frango preparado na frigideira").build();

    mealRepository.saveAndFlush(meal);

    List<Meal> result = mealRepository.findByNameContainingIgnoreCase("FRANGO");

    assertEquals(1, result.size());
    assertEquals("Frango grelhado", result.get(0).getName());
  }

  @Test
  @DisplayName("Deve retornar todas as refeições que contêm o nome pesquisado")
  void shouldFindMultipleMealsByName() {
    Meal chicken = Meal.builder().name("Frango grelhado").description("Frango preparado na frigideira").build();
    Meal chickenRice = Meal.builder().name("Arroz com frango").description("Arroz e frango grelhado").build();
    Meal salad = Meal.builder().name("Salada").description("Alface, tomate, cenoura").build();
    List<Meal> meals = List.of(chicken, chickenRice, salad);

    mealRepository.saveAllAndFlush(meals);


    List<Meal> result = mealRepository.findByNameContainingIgnoreCase("FRANGO");

    assertEquals(2, result.size());
    assertThat(result).extracting(Meal::getName).containsExactlyInAnyOrder("Frango grelhado", "Arroz com frango");
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando nenhuma refeição corresponder à busca")
  void shouldReturnEmptyListWhenNoMealMatches() {
    Meal meal = Meal.builder().name("Frango grelhado").description("Frango preparado na frigideira").build();

    mealRepository.saveAndFlush(meal);

    List<Meal> result = mealRepository.findByNameContainingIgnoreCase("BANANA");

    assertThat(result).isEmpty();
  }
}
