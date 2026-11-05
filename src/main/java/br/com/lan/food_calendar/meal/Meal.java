package br.com.lan.food_calendar.meal;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meals")
public class Meal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Meal() {}

  private Meal(MealBuilder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.description = builder.description;
  }

  public Long getId() {return id;}
  public void setId(Long id) {this.id = id;}

  public String getName() {return name;}
  public void setName(String name) {this.name = name;}

  public String getDescription() {return description;}
  public void setDescription(String description) {this.description = description;}

  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }

  public static MealBuilder builder() {
    return new MealBuilder();
  }

  public static class MealBuilder {
    private Long id;
    private String name;
    private String description;

    public MealBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public MealBuilder name(String name) {
      this.name = name;
      return this;
    }

    public MealBuilder description(String description) {
      this.description = description;
      return this;
    }

    public Meal build() {
      return new Meal(this);
    }
  }
}
