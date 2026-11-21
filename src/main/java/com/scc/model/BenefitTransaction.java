package com.scc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BenefitTransaction(
        int transactionId,
        int seniorId,
        String benefitType,
        BigDecimal amount,
        LocalDate releaseDate,
        String claimStatus,
        int processedByUserId
) {}