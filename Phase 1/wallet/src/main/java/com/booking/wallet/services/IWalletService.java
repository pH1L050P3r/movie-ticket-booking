package com.booking.wallet.services;

import com.booking.wallet.dto.WalletDTO;
import com.booking.wallet.enums.Action;


public interface IWalletService {
    public WalletDTO getWalletById(Long walletId);
    public WalletDTO updateWallet(Long walletId, Long amount, Action action) throws Exception;
    public void deleteWalletById(Long walletId) throws Exception;
    public void deleteAllWallets();
}
