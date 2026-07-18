package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String phone
) {}
