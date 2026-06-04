package com.bank.user.service;

import com.bank.user.dto.AuthRequestDTO;
import com.bank.user.dto.AuthResponseDTO;
import com.bank.user.dto.CreateUserRequestDTO;
import com.bank.user.dto.UserResponseDTO;
import com.bank.user.exception.UserNotFoundException;
import com.bank.user.repository.UserRepository;
import com.bank.user.repository.model.UserEntity;
import com.bank.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDTO register(CreateUserRequestDTO request) {
        UserResponseDTO created = userService.createUser(request);
        UserEntity user = userRepository.findById(created.getId()).orElseThrow();
        String token = jwtService.generateToken(user);
        log.info("Registered and issued token for userId={}", user.getId());
        return buildResponse(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.getStatus() != UserEntity.UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
        }

        String token = jwtService.generateToken(user);
        log.info("Issued token for userId={}", user.getId());
        return buildResponse(token, user);
    }

    private AuthResponseDTO buildResponse(String token, UserEntity user) {
        return AuthResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .expiresAt(jwtService.getExpiresAt())
                .build();
    }
}
