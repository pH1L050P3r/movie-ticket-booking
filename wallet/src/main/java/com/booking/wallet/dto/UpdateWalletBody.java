package com.booking.wallet.dto;

import org.springframework.lang.NonNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.booking.wallet.enums.Action;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWalletBody {
    @NonNull
    public Action action;

    @NonNull
    @Min(0)
    public Long amount;
}
