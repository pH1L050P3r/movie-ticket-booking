package com.booking.wallet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.booking.wallet.models.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long>{
}
