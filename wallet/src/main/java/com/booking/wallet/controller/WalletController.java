package com.booking.wallet.controller;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import com.booking.wallet.enums.Action;
import com.booking.wallet.models.UpdateWalletBody;
import com.booking.wallet.models.Wallet;
import com.booking.wallet.repositories.WalletRepository;

import jakarta.transaction.Transactional;

@RestController
public class WalletController {
    @Autowired
    private WalletRepository walletRepository;
    
    @GetMapping("/wallet/{id}")
    public ResponseEntity<?> fetchWalletById(@PathVariable("id") @NonNull Long walletId){
        try{
            Wallet wallet = walletRepository.findById(walletId).get();
            return new ResponseEntity<Wallet> (wallet, HttpStatus.OK);
        } catch(NoSuchElementException e)
        {
            return new ResponseEntity<HttpStatus> (HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @PutMapping("/wallet/{id}")
    public ResponseEntity<?> updateWallet(@RequestBody UpdateWalletBody requestBody, @PathVariable("id") @NonNull Long walletId){
        Wallet wallet;
        try{
            wallet = walletRepository.findById(walletId).get();
        } catch (NoSuchElementException e){
            //TODO: API Request
            // API request to check user exist or not
            // If not exist then return from here
            wallet = new Wallet(walletId, 0L);
        }

        if(requestBody.action == Action.credit){
            wallet.setBalance(wallet.getBalance() + requestBody.amount);
        } else if(requestBody.action == Action.debit && wallet.getBalance() >= requestBody.amount) {
            wallet.setBalance(wallet.getBalance() - requestBody.amount);
        } else {
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }

        wallet = walletRepository.save(wallet);
        return new ResponseEntity<Wallet>(wallet, HttpStatus.CREATED);
    }

    @DeleteMapping("/wallet/{id}")
    public ResponseEntity<HttpStatus> deleteWalletById(@PathVariable("id") @NonNull Long walletId){
        Wallet wallet;
        try{
            wallet = walletRepository.findById(walletId).get();
            walletRepository.delete(wallet);
        } catch(NoSuchElementException e){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

    @DeleteMapping("/wallet")
    public ResponseEntity<HttpStatus> deleteAllWallet(){
        walletRepository.deleteAll();
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }
}
