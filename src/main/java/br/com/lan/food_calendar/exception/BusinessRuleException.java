package br.com.lan.food_calendar.exception;

public class BusinessRuleException extends RuntimeException {
  public BusinessRuleException(String message) {
    super((message));
  }
}
