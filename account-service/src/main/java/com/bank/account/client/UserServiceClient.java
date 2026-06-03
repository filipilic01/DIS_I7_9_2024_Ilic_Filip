package com.bank.account.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UserServiceClient.UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserResponseDTO getUserById(@PathVariable Long id);

    @Data
    @Builder
    class UserResponseDTO {
        private Long id;
        private String username;
        private String status;
    }

    class UserServiceFallback implements UserServiceClient {
        @Override
        public UserResponseDTO getUserById(Long id) {
            return null;
        }
    }
}
