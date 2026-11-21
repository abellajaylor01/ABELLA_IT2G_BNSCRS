package com.scc.model;

import java.time.LocalDate;

public record SeniorId(
        int idCardId,
        int seniorId,
        LocalDate issueDate,
        LocalDate expiryDate,
        String idStatus,
        String qrBarcodeCode
) {}