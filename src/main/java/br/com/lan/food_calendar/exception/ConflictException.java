package br.com.lan.food_calendar.exception;

public class ConflictException extends RuntimeException{
  public ConflictException(String message) {
    super(message);
  }
}
