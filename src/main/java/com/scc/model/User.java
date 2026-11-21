package com.scc.model;

import java.time.LocalDateTime;

public record User(
        int userId,
        String username,
        String passwordHash,
        String fullName,
        String userRole,
        LocalDateTime dateCreated
) {}