package br.com.lan.food_calendar.user;

import br.com.lan.food_calendar.exception.NotFoundException;
import br.com.lan.food_calendar.mealplan.MealPlanRepository;
import br.com.lan.food_calendar.user.dto.UserCreateRequestDto;
import br.com.lan.food_calendar.user.dto.UserResponseDto;
import br.com.lan.food_calendar.user.dto.UserUpdateRequestDto;
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
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private MealPlanRepository mealPlanRepository;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("Deve buscar usuário pelo ID")
  void shouldReturnUserById() {
    Long id = 3L;
    User user = User.builder().id(id).name("Ana").build();

    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    UserResponseDto result = userService.findById(id);

    assertThat(result.id()).isEqualTo(id);
    assertThat(result.name()).isEqualTo("Ana");
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar usuário com ID inexistente")
  void shouldThrowNotFoundExceptionWhenUserIdDoesNotExist() {
    Long id = 99L;

    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.findById(id));
  }

  @Test
  @DisplayName("Deve retornar a lista de usuários com sucesso")
  void shouldReturnAllUsers() {
    User ana = User.builder().id(1L).name("Ana").build();
    User julia = User.builder().id(2L).name("Julia").build();
    List<User> users = List.of(ana, julia);

    when(userRepository.findAll()).thenReturn(users);

    List<UserResponseDto> result = userService.findAll();

    assertThat(result).hasSize(2);

    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(0).name()).isEqualTo("Ana");

    assertThat(result.get(1).id()).isEqualTo(2L);
    assertThat(result.get(1).name()).isEqualTo("Julia");

    verify(userRepository).findAll();
  }

  @Test
  @DisplayName("Deve salvar e retornar o usuário criado")
  void shouldCreateUser() {
    UserCreateRequestDto requestDto = new UserCreateRequestDto("Gustavo");
    User savedUser = User.builder().id(1L).name("Gustavo").build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserResponseDto result = userService.create(requestDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Gustavo");

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User capturedUser = userCaptor.getValue();

    assertThat(capturedUser.getName()).isEqualTo("Gustavo");
    assertThat(capturedUser.getId()).isNull();
  }

  @Test
  @DisplayName("Deve atualizar o usuário quando o ID for encontrado")
  void shouldUpdateUserNameWhenIdExists() {
    UserUpdateRequestDto requestDto = new UserUpdateRequestDto("Manoella");
    User user = User.builder().id(1L).name("Manoela").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(same(user))).thenReturn(user);

    UserResponseDto result = userService.update(1L, requestDto);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Manoella");

    verify(userRepository, times(1)).findById(1L);
    verify(userRepository).save(same(user));
  }

  @Test
  @DisplayName("Deve lançar NotFoundException ao atualizar usuário inexistente")
  void shouldThrowNotFoundExceptionWhenUpdatingUnknownUser() {
    Long invalidId = 99L;
    UserUpdateRequestDto requestDto = new UserUpdateRequestDto("Manoella");

    when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> {
      userService.update(invalidId, requestDto);
    });

    verify(userRepository, times(1)).findById(invalidId);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve deletar o usuário e o plano alimentar vinculado a esse usuário")
  void shouldDeleteUserAndAssociatedMealPlans() {
    User user = User.builder().id(1L).name("Gustavo").build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.delete(1L);

    verify(userRepository, times(1)).findById(1L);

    InOrder deletionOrder = inOrder(mealPlanRepository, userRepository);
    deletionOrder.verify(mealPlanRepository).deleteAllByUserId(1L);
    deletionOrder.verify(userRepository).delete(user);

  }

  @Test
  @DisplayName("Deve retornar uma lista vazia quando não houver usuários")
  void shouldReturnEmptyUserList() {
    when(userRepository.findAll()).thenReturn(List.of());

    assertThat(userService.findAll()).isEmpty();

    verify(userRepository).findAll();
  }

  @Test
  @DisplayName("Deve lançar NotFoundException ao excluir usuário inexistente")
  void shouldThrowNotFoundExceptionWhenDeletingUnknownUser() {
    Long invalidId = 99L;

    when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> {
      userService.delete(invalidId);
    });

    verify(userRepository).findById(invalidId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(mealPlanRepository);
  }
}
