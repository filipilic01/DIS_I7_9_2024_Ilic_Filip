package com.bank.user.service;

import com.bank.user.dto.CreateUserRequestDTO;
import com.bank.user.dto.UpdateUserRequestDTO;
import com.bank.user.dto.UserResponseDTO;
import com.bank.user.exception.UserAlreadyExistsException;
import com.bank.user.exception.UserNotFoundException;
import com.bank.user.repository.UserRepository;
import com.bank.user.repository.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(1L)
                .username("jdoe")
                .email("jdoe@example.com")
                .password("encoded_pass")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void createUser_success() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername("jdoe");
        request.setEmail("jdoe@example.com");
        request.setPassword("secret123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pass");
        when(userRepository.save(any())).thenReturn(sampleUser);

        UserResponseDTO result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("jdoe");
        assertThat(result.getEmail()).isEqualTo("jdoe@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername("jdoe");
        request.setEmail("other@example.com");
        request.setPassword("secret123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setUsername("newuser");
        request.setEmail("jdoe@example.com");
        request.setPassword("secret123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");
    }

    @Test
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponseDTO result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("jdoe");
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserByUsername_found() {
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(sampleUser));

        UserResponseDTO result = userService.getUserByUsername("jdoe");

        assertThat(result.getUsername()).isEqualTo("jdoe");
    }

    @Test
    void getUserByUsername_notFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jdoe");
    }

    @Test
    void updateUser_success() {
        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setFirstName("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        UserResponseDTO result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(sampleUser);
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UpdateUserRequestDTO()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
