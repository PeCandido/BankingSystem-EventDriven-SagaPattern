package com.banking.merchantservice.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MerchantDto(
        UUID merchantId,
        String name,
        String email,
        String phone,
        BigDecimal initialBalance,
        String currency
) {}
