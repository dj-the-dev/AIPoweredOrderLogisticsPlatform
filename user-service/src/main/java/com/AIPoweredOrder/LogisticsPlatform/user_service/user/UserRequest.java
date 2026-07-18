package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

public record UserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone
) {}
