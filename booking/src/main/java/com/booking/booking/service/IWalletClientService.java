package com.booking.booking.service;

import com.booking.booking.enums.Action;

public interface IWalletClientService {
    public String fetchWalletById(Long walletId);
    public String updateUserWalletMoney(Long amount, Long walletId, Action action);
    public String deleteWalletById(Long walletId);
    public String deleteAllWallet(Long walletId);
}
