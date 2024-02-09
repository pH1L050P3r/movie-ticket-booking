package com.booking.user.service;

import com.booking.user.enums.Action;

public interface IWalletClientService {
    public String updateUserWalletMoney(Long amount, Long userId, Action action);
    public String deleteWalletById(Long walletId);
    public String deleteAllWallets();
}
