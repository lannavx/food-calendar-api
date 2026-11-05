package br.com.lan.food_calendar.user;

import br.com.lan.food_calendar.mealplan.MealPlanRepository;
import br.com.lan.food_calendar.user.dto.UserCreateRequestDto;
import br.com.lan.food_calendar.user.dto.UserResponseDto;
import br.com.lan.food_calendar.user.dto.UserUpdateRequestDto;
import br.com.lan.food_calendar.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final MealPlanRepository mealPlanRepository;

  public UserService(UserRepository userRepository, MealPlanRepository mealPlanRepository) {
    this.userRepository = userRepository;
    this.mealPlanRepository = mealPlanRepository;
  }

  public List<UserResponseDto> findAll() {
    List<User> users = userRepository.findAll();

    return users.stream().map(user -> new UserResponseDto(user.getId(), user.getName())).toList();
  }

  public UserResponseDto findById(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário " + userId + " não existe"));

    return new UserResponseDto(user.getId(), user.getName());
  }

  public UserResponseDto create(UserCreateRequestDto userRequestDto) {
    User newUser = User.builder().name(userRequestDto.name()).build();

    User savedUser = userRepository.save(newUser);

    return new UserResponseDto(savedUser.getId(), savedUser.getName());
  }

  public UserResponseDto update(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário " + userId + " não existe"));

    user.setName(userUpdateRequestDto.name());
    User updateUser =  userRepository.save(user);

    return new UserResponseDto(updateUser.getId(), updateUser.getName());
  }

  @Transactional
  public void delete(Long userId) {
    User user =  userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário " + userId + " não existe"));

    mealPlanRepository.deleteAllByUserId(userId);

    userRepository.delete(user);
  }
}
