package com.banking.merchantservice.mapper;

import com.banking.merchantservice.model.Merchant;
import com.banking.merchantservice.model.MerchantEntity;

public class MerchantMapper {

    public static MerchantEntity toEntity(Merchant merchant) {
        return MerchantEntity.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .balance(merchant.getBalance())
                .currency(merchant.getCurrency())
                .build();
    }

    public static Merchant toDomain(MerchantEntity entity) {
        if (entity == null) return null;

        return new Merchant(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getBalance(),
                entity.getCurrency()
        );
    }
}
