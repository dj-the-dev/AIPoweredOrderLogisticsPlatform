package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse implements Serializable {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
