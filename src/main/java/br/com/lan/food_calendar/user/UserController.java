package br.com.lan.food_calendar.user;

import br.com.lan.food_calendar.user.dto.UserCreateRequestDto;
import br.com.lan.food_calendar.user.dto.UserResponseDto;
import br.com.lan.food_calendar.user.dto.UserUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@Validated
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<UserResponseDto> findAll() {
    return userService.findAll();
  }

  @GetMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserResponseDto findById(@PathVariable Long userId) {
    return userService.findById(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponseDto create(@Valid @RequestBody UserCreateRequestDto userRequestDto) {
    return userService.create(userRequestDto);
  }

  @PutMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserResponseDto update(@PathVariable Long userId,
                                @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
    return userService.update(userId, userUpdateRequestDto);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long userId) {
    userService.delete(userId);
  }
}
