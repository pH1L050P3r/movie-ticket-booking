package com.booking.wallet.services;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booking.wallet.dto.WalletDTO;
import com.booking.wallet.enums.Action;
import com.booking.wallet.mapper.WallerMapper;
import com.booking.wallet.models.Wallet;
import com.booking.wallet.repositories.WalletRepository;

import lombok.NonNull;


@Service
public class WalletService implements IWalletService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private IUserClientService userClientService;

    @Override
    public WalletDTO getWalletById(@NonNull Long walletId){
        Wallet wallet =  walletRepository.findById(walletId).get();
        return WallerMapper.WalletToWalletDTO(wallet);
    }

    @Override
    public WalletDTO updateWallet(@NonNull Long walletId, Long amount, Action action) throws Exception {
        Wallet wallet;

        try{
            userClientService.getUserById(walletId);
        } catch(Exception e){
            // If user not exists then wallet also not exist
            // So return from here
            throw new Exception("Wallet not exists.");
        }

        try{
            wallet = walletRepository.findById(walletId).get();
        } catch (NoSuchElementException e){
            // If user exist and wallet does not exist then create wallet for user
            wallet = new Wallet(walletId, 0L);
        }

        if(action == Action.credit) 
            wallet.setBalance(wallet.getBalance() + amount);
        else if(action == Action.debit && amount <= wallet.getBalance()) 
            wallet.setBalance(wallet.getBalance() - amount);
        else 
            throw new Exception("error while parsing request data");
            
        wallet = walletRepository.save(wallet);
        return WallerMapper.WalletToWalletDTO(wallet);
    }

    @Override
    public void deleteWalletById(@NonNull Long walletId) throws Exception{
        if(!walletRepository.existsById(walletId))
            throw new Exception("User does not exists");
        walletRepository.deleteById(walletId);
    }

    @Override
    public void deleteAllWallets(){
        walletRepository.deleteAll();
    }
}
