package br.com.lan.food_calendar.mealplan;

import br.com.lan.food_calendar.enums.MealType;
import br.com.lan.food_calendar.meal.Meal;
import br.com.lan.food_calendar.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meal_plans")
public class MealPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "meal_id")
  private Meal meal;

  @Enumerated(EnumType.STRING)
  @Column(name = "meal_type", nullable = false)
  private MealType mealType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "target_days", nullable = false, columnDefinition = "text[]")
  private List<DayOfWeek> targetDays;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public MealPlan() {};

  private MealPlan(MealPlanBuilder builder) {
    this.id = builder.id;
    this.user = builder.user;
    this.meal = builder.meal;
    this.mealType = builder.mealType;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.targetDays = builder.targetDays;
  }

  public static MealPlanBuilder builder() {
    return new MealPlanBuilder();
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public Meal getMeal() { return meal; }
  public void setMeal(Meal meal) { this.meal = meal;}

  public MealType getMealType() { return mealType;}
  public void setMealType(MealType mealType) { this.mealType = mealType;}

  public LocalDate getStartDate() { return startDate;}
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getEndDate() { return endDate; }
  public void setEndDate(LocalDate endDate) {this.endDate = endDate;}

  public List<DayOfWeek> getTargetDays() { return targetDays; }
  public void setTargetDays(List<DayOfWeek> targetDays) { this.targetDays = targetDays;}

  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }

  public static class MealPlanBuilder {
    private Long id;
    private User user;
    private Meal meal;
    private MealType mealType;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> targetDays;

    public MealPlanBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public MealPlanBuilder user(User user) {
      this.user = user;
      return this;
    }

    public MealPlanBuilder meal(Meal meal) {
      this.meal = meal;
      return this;
    }

    public MealPlanBuilder mealType(MealType mealType) {
      this.mealType = mealType;
      return this;
    }

    public MealPlanBuilder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public MealPlanBuilder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public MealPlanBuilder targetDays(List<DayOfWeek> targetDays) {
      this.targetDays = targetDays;
      return this;
    }

    public MealPlan build() {
      return new MealPlan(this);
    }
  }
}
