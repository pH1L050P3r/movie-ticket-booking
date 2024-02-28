package com.booking.booking.service;

import com.booking.booking.enums.Action;

public interface IWalletClientService {
    public String updateByUserId(Long amount, Long walletId, Action action);
}
