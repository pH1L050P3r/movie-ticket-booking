package com.booking.booking.service;

import com.booking.booking.enums.Action;

public interface IWalletClientService {
    public String fetchWalletByUserId(Long userId);
    public String updateUserWalletMoney(Long amount, Long userId, Action action);
    public String deleteUserById(Long userId);
    public String deleteAllUser(Long userId);
}
