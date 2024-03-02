package com.booking.wallet.controller;

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

import com.booking.wallet.dto.UpdateWalletBody;
import com.booking.wallet.dto.WalletDTO;
import com.booking.wallet.services.IWalletService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
public class WalletController {
    @Autowired
    private IWalletService walletService;
    
    @GetMapping("/wallets/{id}")
    public ResponseEntity<?> fetchWalletById(@PathVariable("id") @NonNull Long walletId){
        try{
            WalletDTO wallet = walletService.getWalletById(walletId);
            return new ResponseEntity<WalletDTO> (wallet, HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus> (HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/wallets/{id}")
    public ResponseEntity<?> updateWallet(@Valid @RequestBody UpdateWalletBody requestBody, @PathVariable("id") @NonNull Long walletId){
        try{
            WalletDTO wallet = walletService.updateWallet(walletId, requestBody.amount, requestBody.action);
            return new ResponseEntity<WalletDTO>(wallet, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<HttpStatus> deleteWalletById(@PathVariable("id") @NonNull Long walletId){
        try{
            walletService.deleteWalletById(walletId);
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/wallets")
    public ResponseEntity<HttpStatus> deleteAllWallet(){
        try{
            walletService.deleteAllWallets();
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
