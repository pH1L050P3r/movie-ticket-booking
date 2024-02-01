package com.booking.wallet.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Wallet {
    
    @Id
    private Long userId;

    private Long balance;

    protected Wallet() {}

    public Wallet(Long userId, Long balance){
        this.userId = userId;
        this.balance = balance;
    }

    public Long getUserId(){
        return this.userId;
    }

    public void serUserId(Long userId){
        this.userId = userId;
    }

    public Long getBalance(){
        return this.balance;
    }

    public void setBalance(Long balance){
        this.balance = balance;
    }

}
