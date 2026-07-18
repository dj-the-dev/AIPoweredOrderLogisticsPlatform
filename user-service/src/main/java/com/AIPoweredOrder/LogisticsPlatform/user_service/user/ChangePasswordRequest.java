package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}
