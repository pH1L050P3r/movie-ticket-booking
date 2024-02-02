package com.booking.wallet.models;

import org.springframework.lang.NonNull;
import jakarta.validation.constraints.Min;

import com.booking.wallet.enums.Action;

public class UpdateWalletBody {
    @NonNull
    public Action action;

    @NonNull
    @Min(0)
    public Long amount;
}
