package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserResponse implements Serializable {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private User.Role role;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
