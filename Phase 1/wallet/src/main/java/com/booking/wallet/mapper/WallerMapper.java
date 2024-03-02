package com.booking.wallet.mapper;

import com.booking.wallet.dto.WalletDTO;
import com.booking.wallet.models.Wallet;

public class WallerMapper {
    public static WalletDTO WalletToWalletDTO(Wallet wallet){
        return new WalletDTO(
            wallet.getUserId(),
            wallet.getBalance()
        );
    }
}
