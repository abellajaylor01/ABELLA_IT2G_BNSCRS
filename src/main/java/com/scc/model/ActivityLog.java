package com.scc.model;

import java.time.LocalDateTime;

public record ActivityLog(
        int logId,
        int userId,
        String actionPerformed,
        String eventDetails,
        LocalDateTime timestamp,
        String ipAddress
) {}