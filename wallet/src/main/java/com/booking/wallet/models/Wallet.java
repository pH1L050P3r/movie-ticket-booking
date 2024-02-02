package com.booking.wallet.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet")
public class Wallet {
    
    @Id
    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    @Column(name = "balance")
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
