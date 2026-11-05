package br.com.lan.food_calendar.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
  List<Meal> findByNameContainingIgnoreCase(String name);
}
