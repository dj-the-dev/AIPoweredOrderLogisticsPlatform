package com.AIPoweredOrder.LogisticsPlatform.user_service.user;

import java.util.UUID;

public record UserRequest(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone
) {}
