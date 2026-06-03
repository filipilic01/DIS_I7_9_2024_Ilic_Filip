package com.bank.user.service;

import com.bank.user.dto.CreateUserRequestDTO;
import com.bank.user.dto.UpdateUserRequestDTO;
import com.bank.user.dto.UserResponseDTO;
import com.bank.user.exception.UserAlreadyExistsException;
import com.bank.user.exception.UserNotFoundException;
import com.bank.user.repository.UserRepository;
import com.bank.user.repository.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        UserEntity saved = userRepository.save(user);
        log.info("Created user with id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        return toResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username)));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id={}", id);
    }

    private UserResponseDTO toResponse(UserEntity user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
