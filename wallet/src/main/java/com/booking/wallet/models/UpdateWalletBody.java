package com.booking.wallet.models;

import org.springframework.lang.NonNull;

import com.booking.wallet.enums.Action;

public class UpdateWalletBody {
    @NonNull
    public Action action;

    @NonNull
    public Long amount;
}
